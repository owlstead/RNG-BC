The Optimized Simple Discard Method
===

Author: Maarten Bodewes  
Date: 2017-03-06 (initial draft)  
Date: 2018-06-01 (revised draft)  
Date: 2020-03-23 (release candidate)  
Date: 2020-03-23 (release candidate 2)

Excerpt
---

This document provides a specification and analysis of the Optimized Simple Discard Method. As the name implies the Optimized Simple Discard Method is an optimization of the Simple Discard Method that is specified by NIST in NIST SP 800-90A Rev 1. The Simple Discard Method is used to generate well distributed random numbers in a range $[0, r)$ given a well distributed random bit generator or DRBG. The Simple Discard Method is colloquially known as *rejection sampling*.  

The Optimized Simple Discard Method is implemented by an algorithm called RNG-BC. RNG-BC is an acronym for Random Number Generator using Binary Compare. We will also introduce a derived random number generator RNG-BC-$z$, which operates on words with bit size $z$. For instance, RNG-BC-8 operates on bytes instead of bits.

First we prove by reduction that the implementation is not biased. Secondly we show that the Optimized method is highly efficient with regards to usage of the underlying RBG by creating a performance analysis that shows the number of bits retrieved from the DRBG.

Notation
---

This document follows the notation of NIST SP 800-90A Rev 1, Appendix A, section A.5. 

Terms:

 - RBG: Random Bit Generator
 - RNG: Random Number Generator (for a number in a specific range)
 - $r$: the amount of elements in the range $[0, r)$
 - $n_i$: the bit value of the bit at position $i$ within $r$
 - $m$: the minimum number of bit required to encode $r$
 - $b_i$: the bit value of the bit at position $i$ within the candidate
 - $c$: a candidate value of $l$ bits in the range $[0, 2^m)$ in case of the (Optimized) Simple Discard Method
 - $a$: the final value in the range $[0, r)$
 - $z$: the amount of bits to compare at the same time within the binary compare (in RNG-BC-$z$) 
 - $C$: a constant to add to the value to create a value in the range $[C, C + r)$ 

Numbers have a value $\sum_{i=0}^{m-1} 2^i b_i$ where $b_0 \dots b_{m-1}$ is the little endian bit representation of the value.

Introduction
---

This document describes an efficient random number generator for number generation in a large range using a random bit generator. The result of the random number generation is a natural number $a$ in the range $[0, r)$.

It is easy to generate a number in any range $[l, h)$ by setting $r' = h - l$ and generating $a'$ in range $[0, r')$, finally adjusting the value $a = a' + l$. This kind of random number generation is required for cryptographic operations such as the generation of EC private keys or the generation of the master secret in RSA-KEM [3].

NIST has described three such methods in NIST SP 800-90A Rev 1. We will shortly introduce these before specifying the Optimized Simple Discard Method. 

The Simple Discard method
---

The simplest method of generating a random value in this range is to generate a candidate value $c$ in the range $[0,2^m)$ where $m$ is the minimum number of bits required to encode $r$. Mathematically $m$ is identical to $\big\lceil\log_2(r)\big\rceil$. After generation value $c$ is compared with value $r$. If the value of $c$ is higher or equal then the value is discarded and regenerated. If the value of $c$ is lower then the value $c$ is accepted; $a = c$. The Simple Discard Method has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.1 [1].

The Complex Discard Method
---

The complex discard method will not be evaluated; it has been specified to generate many random numbers in a specific range and does not seem efficient for embedded systems if only due to the memory requirements of generating multiple random numbers at once. It has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.2 [1]. 

The Simple Modular method
---

The simple modular method uses a security parameter $s$ which will allow a constant time generation of numbers using $m + s$ bits, where $s$ is a predetermined security parameter. It generates a candidate value $c$ using $m + s$ bits, where $a = c \bmod r$. This introduces a certain bias that is largely dependent on the size of $s$. 

Unfortunately, $c \bmod m$ cannot be calculated in advance and taking the modulus using large operands is inefficient, especially on embedded systems or smart cards. The Simple Modular Method has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.3 [1].

The Optimized Simple Discard method
===

Description
---

As the name suggests, the Optimized Simple Discard method is an enhancement of the Simple Discard method. It hinges on one single observation: only the most significant bits are actually compared during a binary comparison operation. The least significant bits are left untouched and should therefore still be considered random.

The Optimized Simple Discard method therefore does not regenerate all $b_{m-1}$ to $b_0$ bits, it only regenerates the bits at position $b_{m-1}$ to $b_i$ inclusive where $i$ is the bit that made the value of $c$ higher than the value of $r$.

Binary comparison
---

The following is a description of binary comparison, which is performed using within a processor to determine if a value is lower than, equal to or greater than another value. It is hidden away in the operation $c < r$ within the Simple Discard Method and made explicit in the Optimized Simple Discard Method.

Bitwise the following calculations are performed when binary comparison is used, where index $i$ decrements from $m-1$ to $0$:

 1. If $n_i = 0$ and $b_i = 0$ then continue with next bit, the most significant bits are equal;
 2. If $n_i = 0$ and $b_i = 1$ then the value of $c$ is larger than $r$: regenerate $c$ and start over;
 3. If $n_i = 1$ and $b_i = 0$ then the value of $c$ is smaller than $r$: return $c$;
 4. If $n_i = 1$ and $b_i = 1$ then continue with next bit, the most significant bits are equal.

The Optimized Simple Discard Method simply changes option 2 to:

If $n_i = 0$ and $b_i = 1$ then the value of $c$ is larger than $r$: **regenerate bits at position $b_{m-1} \dots b_i$** and start over;

Finally, if all the bits have equal values then $c = r$, so all $m$ bits need to be regenerated. The probability of of this happen is over $1 \over 2^m$ so it is unlikely to happen for large values of $m$ and thus $r$.

Proof of equivalence
---

If we look at this scheme we notice that only the leftmost $m - i$ bits are actually evaluated in the Simple Discard Method. Any bits that are less significant will never be compared or otherwise operated on. This means that they should still be considered random. 

Another way of looking at it would be that the bits at location $i$ only need to be generated *right before they need to be compared*; the rest of the bits can be generated after $c$ is determined to be smaller. Any bit that has been evaluated must be considered invalid and must however be regenerated, i.e. bits $m-1$ to $i$.

The Optimized Simple Discard Method is functionality equivalent to the Simple Discard method. Since the Simple Discard Method doesn't produce biased output, it can be concluded that this method doesn't produce biased output either. 

Side channel attacks
---

The Optimal Simple Discard method is as vulnerable against side channel attacks as the Simple Discard method: it may be detectable which bit indicates that the value of $c$ is lower than $r$. This may indicate to an attacker how many of the most significant bits of $c$ are identical to the most significant bits of $r$. Care should be taken to ensure that the comparison operations leak as little information as possible.

It may be easier to detect how many bits are identical once comparison fails for the Optimized Simple Discard method; the number of regenerated bits are a clear indication. As this only leaks information about the *rejected* bits of a candidate this is unlikely to provide any advantage to an adversary.

RNG-BC-Z
===

RNG-BC-$z$ is a generalization of the Optimized Simple Discard method for different word sizes. If $z = m$ then RNG-BC-$z$ is identical to the Simple Discard Method and no optimization takes place. If $z = 1$ then RNG-BC-$z$ is identical to the Optimized Simple Discard method.

Depending on the speed of the PRNG is may be more useful to group $z$ bits together to perform the comparison operation. This has the disadvantage that - on average - it will require more bits to be generated. However, generally computers are optimized to operate on bytes - i.e. $z = 8$ - or machine specific words. This means that RNG-BC-8 is likely to be faster than RNG-BC-1 on most systems. It does greatly simplify the code and reduces the code size.

Performance tests
===

Test description
---

The following tests are implemented:

 - Simple Discard Method Java - this is simply a call to the `BigInteger(int numBits, Random rnd)` constructor, followed by a comparison with the value of $r$;
 - Simple discard byte array - this is a re-implementation of the Simple Discard Method using a byte array, to make sure that the Java implementation performs as expected and to make sure that the performance comparison is fair;
 - Simple Modular Method BigInteger - the simple modular method with $s$ set to 128;
 - RNG-BC-1 - the most efficient 1 bit binary compare method that exactly generates as many bits as necessary for the Optimized Simple Discard Method;
 - RNG-BC-8 - the byte oriented implementation of RNG-BC, which generates and compared each byte separately;

Test setup
---

The tests were performed using the default `"DRBG"` secure random implementation in Java, which - by default - implements a counter based DRBG as specified in NIST [1].

System information:

 - Ubuntu 19.10
 - AMD® Ryzen 7 3700x 8-core processor × 16

Java information:

```none
java.runtime.name=OpenJDK Runtime Environment
java.runtime.version=13+33-Ubuntu-1
java.specification.vendor=Oracle Corporation
java.version.date=2019-09-17
java.vm.name=OpenJDK 64-Bit Server VM
```

Note that the Java runtime environment is using big endian encoding. That means that the highest byte is on the left hand side. The variable `i` is different from the one used in this specification: it is the index of the byte under evaluation, going from left to right, not the index of the bit going in the other direction. 

Test procedure
---

All methods have been preceded by a warm up round to make sure that Java's just-in-time compiler has had time to optimize the loops.

All tests have been performed over one million generated random numbers.

First the name of the size of the range $n$ is displayed, followed by the value of $r$ in hexadecimals.
Then the different methods for generating the numbers are displayed line by line with their results.

"avg bits" is the average number of bits required to generate each of the million random numbers.
"avg time" is the average time used by each run of the test in microseconds.

An "Ignore" value is generated. This value is just included in the printout to make sure that the byte code interpreter doesn't skip the random number generation altogether because the random value is not used afterwards. It needs to be ignored; it has no meaningful value.

Test results
---

```

 === Minimum 256 bit === 

8000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 511.886336 avg time 1.000000μs
Simple Discard Method byte array, avg bits: 511.960064 avg time 0.910000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.836000μs
RNG-BC-1, avg bits: 258.999684 avg time 0.569000μs
RNG-BC-8, avg bits: 264.065944 avg time 0.922000μs
Ignore: 213

 === Minimum 256 bit + 1 === 

8000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 511.775232 avg time 1.000000μs
Simple Discard Method byte array, avg bits: 512.193792 avg time 0.904000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.859000μs
RNG-BC-1, avg bits: 258.995740 avg time 0.561000μs
RNG-BC-8, avg bits: 264.043960 avg time 0.942000μs
Ignore: 229

 === Mid 256 bit - 1 === 

BFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 341.554688 avg time 0.645000μs
Simple Discard Method byte array, avg bits: 341.273088 avg time 0.624000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.900000μs
RNG-BC-1, avg bits: 256.662856 avg time 0.498000μs
RNG-BC-8, avg bits: 258.667016 avg time 0.648000μs
Ignore: 210

 === Mid 256 === 

C000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 341.507840 avg time 0.649000μs
Simple Discard Method byte array, avg bits: 341.479424 avg time 0.630000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.854000μs
RNG-BC-1, avg bits: 257.330163 avg time 0.518000μs
RNG-BC-8, avg bits: 258.712184 avg time 0.651000μs
Ignore: 19

 === Mid 256 + 1 === 

C000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 341.321728 avg time 0.650000μs
Simple Discard Method byte array, avg bits: 341.316096 avg time 0.627000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.864000μs
RNG-BC-1, avg bits: 257.335282 avg time 0.517000μs
RNG-BC-8, avg bits: 258.712392 avg time 0.651000μs
Ignore: 6

 === Max 256 - 1 === 

FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 256.000000 avg time 0.483000μs
Simple Discard Method byte array, avg bits: 256.000000 avg time 0.485000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.872000μs
RNG-BC-1, avg bits: 256.000000 avg time 0.479000μs
RNG-BC-8, avg bits: 256.000000 avg time 0.490000μs
Ignore: 237

 === Brainpool P512r1 Q === 

AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA70330870553E5C414CA92619418661197FAC10471DB1D381085DDADDB58796829CA90069

Simple Discard Method Java, avg bits: 767.076864 avg time 0.977000μs
Simple Discard Method byte array, avg bits: 767.051264 avg time 0.893000μs
Simple Modular Method BigInteger, avg bits: 640.000000 avg time 1.368000μs
RNG-BC-1, avg bits: 513.315474 avg time 0.649000μs
RNG-BC-8, avg bits: 515.997576 avg time 0.859000μs
Ignore: 255

 === Random modulus 4096 === 

99A9312FD775C94D804827B6011C9FCC8180AD142ADEC24C772C24709DB89EB6FF4A7C8C876837920F8C49CF23FF8C9DA0D8E7FBB722E0D3E6671D39DEBD95079D56C57C0D9DB9D2CAD074BC57E22F8811BA163ECB3342F24CE97DF551BD3D9353796EFA7F5F9C9ED5EF98A7A0FF30D1E805FAA8EA8747A6AE3734CFD8046E56278D36AD2666C612270298FA87356DEDFFF13EAC082AB7D406510D367C6CA034DDB92C297B65A233B74BAAE3CA2165E3FE699DBCBE82B48831C52222F10FDBD29ACA5608616F09AD6FDD2BFE90ECB135F4976E93660714B2FCC0CD0A713723FF835229628480444E0DC75D3FD44AEE9465AC719C0A605E4F5917A91E09C0EBE23B08E467766D6329BB83443A17EAB39A310AAB8B59628F90F4B40B50F2AF6FF49C3307441FEAB0F0978177798123207B21AE8FD7A6CC5D307155843161EE4C54E3685BA402F5A2F155A52C4521B76ECA6E2B9D5362341E37B8CB4DCBF7BC9CAAFFBD31A62CE3240F5161F43598D0F8AE5FE418F4784F8DDD444BD314E73F8C870895BDE24388BB905E42AE80DE34885C6D64EDBAB2E852053B737BF85471D5DFED98BA878E806DE82AA762791DACEF47895D66378B6F94256A0D3E7E77026AAB58540246EB9952C8C54777FAA879026498C169835DF5866FE867734D21E9E921D6A6FCF64260D8B98F195D0585D445D611607008F7D15407FCC32C1A55F1F9A5

Simple Discard Method Java, avg bits: 6819.016704 avg time 4.067000μs
Simple Discard Method byte array, avg bits: 6824.448000 avg time 3.583000μs
Simple Modular Method BigInteger, avg bits: 4224.000000 avg time 5.918000μs
RNG-BC-1, avg bits: 4097.730171 avg time 2.436000μs
RNG-BC-8, avg bits: 4101.351224 avg time 2.774000μs
Ignore: 181
```

Test interpretation
---

The RNG-BC-1 implementation uses the least amount of bits from the underlying implementation. However, it requires special buffering techniques for it to retrieve bits instead of bytes from the underlying RBG. This slows down the implementation quite a bit, which means it is sometimes slower than the byte oriented RNG-BC-8. Note that it only uses about 0-3 additional bits over $m$, the number of bits in $r$. The RNG-BC-8 implementation is a close second when it comes to consuming bits.

The two Simple Discard Methods will consume about the same amount of bits on average. It shows that the Simple Discard Method that compares bytes directly is slightly faster than the one that converts to `BigInteger` and then performs the comparison. For small values it may be faster than RNG-BC-1 or RNG-BC-8, but it loses ground for an $r$ larger than 256 bits.

Note that the `RandomBitGenerator` caches some bits to be able to supply them to the RNG-BC-1. The fewer calls to the `SecureRandom` implementation will likely explain the advantage that it seems to have over RNG-BC-8. The code of RNG-BC-1 is otherwise slightly more complex, so it should be slower. 

Conclusion
===

Simply by combining the comparison and random number generation it is easy to significantly bring down the number of bits requested from the underlying Random Bit Generator. There are no apparent drawbacks, such as requiring division using large operands required by the Simple Modular Method or requiring memory beyond the storage of the value itself.

The number of additional bits does not grow with the size of the range. This is different from the Simple Discard Method where the additional number of bits grows linearly with the range. That means that the Optimized Simple Discard method is relatively more effective when the range is large such as in RSA-KEM.

The Optimized Simple Discard Method can therefore be used at any place where one of the other methods are used. However, it will have the most impact when dealing with a relatively slow random number generator.

Next steps
===

The following next steps should be considered:

 - The implementations of RNG_BC should be made compatible for a number range of any bit size (using masking); currently the implementation only allow for $r$ where $m \bmod 8 = 0$, i.e. the most significant bit of $r$ is the most significant bit of a byte.
 - A mathematical calculation should be performed to show that the test results are indeed expected and correct.
 - The maximum number of bits used should be shown to indicate that the Optimized Simple Discard Method is fares much better than the Simple Discard Method in scenarios where the many candidates need to be tested.
 
References
===

References:
 - [1] Elaine Barker, John Kelsey: NIST SP 800-90A Rev. 1: Recommendation for Random Number Generation Using Deterministic RBGs.
 - [2] M. Lochter, J. Merkle: RFC 5639: Elliptic Curve Cryptography (ECC) Brainpool Standard Curves and Curve Generation
 - [3] V. Shoup: A Proposal for an ISO Standard for Public Key Encryption (version 2.1)
