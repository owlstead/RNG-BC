The Optimized Simple Discard Method
===

Author: Maarten Bodewes  
Date: 2017-03-06 (initial draft)  
Date: 2018-06-01 (revised draft)  
Date: 2020-03-23 (release candidate)

Excerpt
---

This document provides a specification and analysis of a random number generator called RNG-BC which uses a scheme called the Optimized Simple Discard Method. RNG-BC is an acronym for Random Number Generator using Binary Compare. We will also introduce a derived random number generator RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>. These generators can be used for generating random numbers in in a range <img src="/tex/fdec1c793855a9ff92be481a1c6b8156.svg?invert_in_darkmode&sanitize=true" align=middle width=41.484001349999986pt height=24.65753399999998pt/>. These random number generators will use a scheme we will call the Optimized Simple Discard Method, after the Simple Discard Method standardized in NIST SP 800-90A Rev 1 [1].

First we prove by reduction to the Simple Discard Method that the implementation is not biased. Secondly we show that the Optimized method is highly efficient with regards to usage of the underlying RBG. We also show that a minimum of calculations is required to implement the algorithm.

Notation
---

Throughout this document unsigned integers are used in a bit string or octet string notation, where the most significant bit is the leftmost bit. The least significant bit has index <img src="/tex/29632a9bf827ce0200454dd32fc3be82.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/> and therefore the most significant has index <img src="/tex/60ace8ff4d114805b0b0334942a8e20f.svg?invert_in_darkmode&sanitize=true" align=middle width=42.743500799999985pt height=21.18721440000001pt/>, where <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is the number of bits required to represent the number. In other words, in this document the big endian or network order encoding of numbers is used.

Bitwise operators are used throughout this document, where <img src="/tex/45848451c711deba755da6422f9e68c6.svg?invert_in_darkmode&sanitize=true" align=middle width=12.785434199999989pt height=19.1781018pt/> is XOR and <img src="/tex/c62a31537f94bed6e2da5478cd946633.svg?invert_in_darkmode&sanitize=true" align=middle width=25.57086674999999pt height=17.723762100000005pt/> and <img src="/tex/2e15b0c09da7f13eae3d39169ef4fab8.svg?invert_in_darkmode&sanitize=true" align=middle width=25.57086674999999pt height=17.723762100000005pt/> are the shift left and shift right operators.

Terms:

 - RBG: Random Bit Generator
 - RNG: Random Number Generator (for a number in a specific range)
 - <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/>: the amount of elements in the range <img src="/tex/55f3e69887b882407ce69a32f942ec8b.svg?invert_in_darkmode&sanitize=true" align=middle width=36.35090909999999pt height=24.65753399999998pt/>
 - <img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>: the amount of bits to compare at each time
 - <img src="/tex/9b325b9e31e85137d1de765f43c0f8bc.svg?invert_in_darkmode&sanitize=true" align=middle width=12.92464304999999pt height=22.465723500000017pt/>: a constant to add to the value to create a value in the range <img src="/tex/a2a2b304269d9a0286b49dab2402431f.svg?invert_in_darkmode&sanitize=true" align=middle width=73.15892099999999pt height=24.65753399999998pt/> 

Examples use unsigned big endian numbers in hexadecimal notation.

Introduction
---

This document describes an efficient random number generator for number generation in a large range using a random bit generator. The result of the random number generation is a natural number in the range <img src="/tex/fdec1c793855a9ff92be481a1c6b8156.svg?invert_in_darkmode&sanitize=true" align=middle width=41.484001349999986pt height=24.65753399999998pt/>. It is however easy to generate a number in any range <img src="/tex/a2a2b304269d9a0286b49dab2402431f.svg?invert_in_darkmode&sanitize=true" align=middle width=73.15892099999999pt height=24.65753399999998pt/> by simply adding a constant <img src="/tex/9b325b9e31e85137d1de765f43c0f8bc.svg?invert_in_darkmode&sanitize=true" align=middle width=12.92464304999999pt height=22.465723500000017pt/> to the number. This kind of random number generation is required for cryptographic operations such as the generation of EC private keys or the generation of the master secret in RSA-KEM.

The random number generator function is called RNG-BC which stands for Random Number Generator using Binary Compare. This indicates the basic idea of the random number generator: it generates numbers in a range by directly comparing the bits or bytes with the value of <img src="/tex/f9c4988898e7f532b9f826a75014ed3c.svg?invert_in_darkmode&sanitize=true" align=middle width=14.99998994999999pt height=22.465723500000017pt/>. The method used for the random number generation will be called the Optimized Simple Discard Method as the method is an improved version of the Simple Discard Method.

The Simple Discard method
---

The simplest method of generating a random value in this range is to generate a candidate value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> in the range <img src="/tex/52514e6fa02d52b78e32c712ab1808ff.svg?invert_in_darkmode&sanitize=true" align=middle width=47.19000659999998pt height=24.65753399999998pt/> where <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is the minimum number of bits required to encode <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> [define r]. Mathematically <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is identical to <img src="/tex/2f0569e30ce3ec8cba3dd14fb84ccd5e.svg?invert_in_darkmode&sanitize=true" align=middle width=74.6575896pt height=27.94539330000001pt/>. After generation value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is compared with value <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. If the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is higher or equal then the value is discarded and regenerated. If the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is lower then the value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is accepted. The Simple Discard Method has been standardized by NIST SP 800-90A Rev 1, Appendix A, section A.5.1 [1].

The Simple Modular method
---

Another method that is often used is to generate a value <img src="/tex/efb1f850b314a66dafa69248c8088aed.svg?invert_in_darkmode&sanitize=true" align=middle width=24.14632439999999pt height=22.465723500000017pt/> which is in the range <img src="/tex/6206792dc48ce2033cf64cbc71cc6a0b.svg?invert_in_darkmode&sanitize=true" align=middle width=60.150462899999994pt height=26.17730939999998pt/>. Here <img src="/tex/deceeaf6940a8c7a5a02373728002b0f.svg?invert_in_darkmode&sanitize=true" align=middle width=8.649225749999989pt height=14.15524440000002pt/> is a number of bits that is used in addition of <img src="/tex/332cc365a4987aacce0ead01b8bdcc0b.svg?invert_in_darkmode&sanitize=true" align=middle width=9.39498779999999pt height=14.15524440000002pt/>. This larger value is then compared with <img src="/tex/f376dd085ad8fead2ac5ebfb8c78545e.svg?invert_in_darkmode&sanitize=true" align=middle width=41.305072049999985pt height=22.465723500000017pt/> where <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is the highest value so that <img src="/tex/f376dd085ad8fead2ac5ebfb8c78545e.svg?invert_in_darkmode&sanitize=true" align=middle width=41.305072049999985pt height=22.465723500000017pt/> is smaller than <img src="/tex/95c7ce4376f3faf5ba13522f39406b49.svg?invert_in_darkmode&sanitize=true" align=middle width=32.84456174999999pt height=26.17730939999998pt/>. If the <img src="/tex/efb1f850b314a66dafa69248c8088aed.svg?invert_in_darkmode&sanitize=true" align=middle width=24.14632439999999pt height=22.465723500000017pt/> is larger than <img src="/tex/f376dd085ad8fead2ac5ebfb8c78545e.svg?invert_in_darkmode&sanitize=true" align=middle width=41.305072049999985pt height=22.465723500000017pt/> then the value is discarded. If it is lower then <img src="/tex/2d58194d426b8af64ad47e31c2587035.svg?invert_in_darkmode&sanitize=true" align=middle width=57.316886549999985pt height=29.205422400000014pt/>. This way the chance of having an acceptable value is much higher than with Method 1. The value of <img src="/tex/f376dd085ad8fead2ac5ebfb8c78545e.svg?invert_in_darkmode&sanitize=true" align=middle width=41.305072049999985pt height=22.465723500000017pt/> can be calculated in advance if multiple numbers in the range are required. Unfortunately, <img src="/tex/d67fcbe27b10893e44d1a4315ef30b80.svg?invert_in_darkmode&sanitize=true" align=middle width=20.818188599999996pt height=29.205422400000014pt/> cannot be calculated in advance and the division for large numbers is inefficient, especially on embedded systems or smart cards. The Simple Modular Method has been standardized by NIST SP 800-90A Rev 1, Appendix A, section A.5.2 [1].

The Optimized Simple Discard method
===

Description
---

As the name suggests, the Optimized Simple Discard method is an enhancement of the Simple Discard method. It hinges on one single observation: only the most significant bits are actually compared during the binary comparison operation. The least significant bits are left untouched and should therefore still be considered random.

The Optimized Simple Discard method therefore does not regenerate all <img src="/tex/a5e90edb91e096daa9080a4e519585cc.svg?invert_in_darkmode&sanitize=true" align=middle width=35.546213999999985pt height=22.831056599999986pt/> to <img src="/tex/ea8adcd8cf746feb94319864409cdc74.svg?invert_in_darkmode&sanitize=true" align=middle width=13.60734374999999pt height=22.831056599999986pt/> bits, it only generates the bits <img src="/tex/a5e90edb91e096daa9080a4e519585cc.svg?invert_in_darkmode&sanitize=true" align=middle width=35.546213999999985pt height=22.831056599999986pt/> to <img src="/tex/f32fd54330a999d797b4651a30345b5d.svg?invert_in_darkmode&sanitize=true" align=middle width=13.15930604999999pt height=22.831056599999986pt/> where <img src="/tex/36b5afebdba34564d884d347484ac0c7.svg?invert_in_darkmode&sanitize=true" align=middle width=7.710416999999989pt height=21.68300969999999pt/> is the bit that made the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> higher than the value of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> in case (2) of the binary comparison.

Binary comparison
---

Bitwise the following calculations are performed if we'd use binary comparison, where index <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> goes from <img src="/tex/60ace8ff4d114805b0b0334942a8e20f.svg?invert_in_darkmode&sanitize=true" align=middle width=42.743500799999985pt height=21.18721440000001pt/> to <img src="/tex/29632a9bf827ce0200454dd32fc3be82.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/>:

 1. If <img src="/tex/edcc16007110b4cbf0fa08066b1d8fb1.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/f57d0e610d6c18030713774375d3d739.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then continue with next bit, the most significant bits are equal;
 2. If <img src="/tex/edcc16007110b4cbf0fa08066b1d8fb1.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/1aa28eca4aded955d20db44177279dbf.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is larger than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: regenerate <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/>;
 3. If <img src="/tex/480fa181cbc6eeff17cf636eca251c88.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/f57d0e610d6c18030713774375d3d739.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is smaller than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: return <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/>;
 4. If <img src="/tex/480fa181cbc6eeff17cf636eca251c88.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/1aa28eca4aded955d20db44177279dbf.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then continue with next bit, the most significant bits are equal.

Finally, if <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> is zero and all the bits have been equal then <img src="/tex/e419b7cbc237e7f8ba4408d119eb5010.svg?invert_in_darkmode&sanitize=true" align=middle width=36.90439004999999pt height=14.15524440000002pt/>, so all <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> bits need to be regenerated and <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> needs to be set to <img src="/tex/29632a9bf827ce0200454dd32fc3be82.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/>. The probability of of this happen is slightly over <img src="/tex/5a41538a4ae89c745f899d9a20e0c176.svg?invert_in_darkmode&sanitize=true" align=middle width=17.467835549999997pt height=27.77565449999998pt/> so it is unlikely to happen for large values of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>.

Proof of equivalence
---

If we look at this scheme we notice that only the leftmost <img src="/tex/acda51a07ccb074ab4b6a7cd5d9065bf.svg?invert_in_darkmode&sanitize=true" align=middle width=35.14940549999999pt height=21.68300969999999pt/> bits are actually evaluated. Any bits that are less significant will never be "seen" by the comparison algorithm. This means that they should still be considered random. 

Another way of looking at it would be that the bits at location <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> only need to be generated right before they need to be compared; the rest of the bits can be generated after <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is determined to be smaller. Any bit that has been evaluated must be considered invalid and must however be regenerated, i.e. bits <img src="/tex/7f276e7f2c7dcecbfddf277c9371bc5b.svg?invert_in_darkmode&sanitize=true" align=middle width=37.70538914999999pt height=21.18721440000001pt/> to <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/>.

This method is fully equivalent to the Simple Discard method. Since the Simple Discard Method doesn't produce biased output, it can be concluded that this method doesn't produce biased output either. Just as the Simple Discard Method is is not 

Efficiency
---

The Simple Discard method regenerates all bits when the comparison fails, it therefore has the same efficiency as the worst case scenario of the Optimized Simple Discard method where all the bits of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> are identical to the bits of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>.

The optimized algorithm will make the biggest impact if the value of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> is a power of two or slightly higher than a power of two. In that case the binary compare is more likely fail earlier, and the limitation on the amount of bits that need to be regenerated has a much more of an impact.

Note that many values - such as the prime value of the curves standardized by NIST - are deliberately slightly below a power of two (values FFFFFFFF FFFFFFFF in the most significant bits) to make sure that regeneration of the random bits only takes place incidentally, if ever at all. Other curves, such as the Brainpool curves [2], do however not include such an optimization.

Side channel attacks
---

The Optimal Simple Discard method is as vulnerable against side channel attacks as the Simple Discard method: it may be detectable which bit indicates that the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is lower than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. This may indicate to an attacker how many of the most significant bits of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> are identical to the most significant bits of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>.

It may be easier to detect how many bits are identical once comparison fails for the Optimized Simple Discard method; the number of regenerated bits are a clear indication. As this only leaks information about the *rejected* bits of a candidate this is unlikely to provide any advantage to an adversary.

Variations
===

RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>
---

RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is a generalization of the Optimized Simple Discard method for different word sizes. If <img src="/tex/de2e00c028a0878bdb39ad808e30e9a2.svg?invert_in_darkmode&sanitize=true" align=middle width=44.71834949999999pt height=14.15524440000002pt/> then RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is identical to the Simple Discard Method and no optimization takes place. If <img src="/tex/429e67fc243a92515642c63b5a91a4fc.svg?invert_in_darkmode&sanitize=true" align=middle width=38.50445939999999pt height=21.18721440000001pt/> then RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is identical to the Optimized Simple Discard method.

Depending on the speed of the PRNG is may be more useful to group <img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> bits together to perform the comparison operation. This has the disadvantage that - on average - it will require more bits to be generated. However, generally computers are optimized to operate on bytes - i.e. <img src="/tex/7d2031baf3ef5ec99b4ff8b0c292a411.svg?invert_in_darkmode&sanitize=true" align=middle width=38.50445939999999pt height=21.18721440000001pt/> - or machine specific words. This means that RNG-BC-8 is likely to be faster than RNG-BC-1 on most systems.

Skip for N is 2^x
---

When a number is generated in the range <img src="/tex/016bc74b3a126ee5c1db39da1e375fac.svg?invert_in_darkmode&sanitize=true" align=middle width=41.484001349999986pt height=24.65753399999998pt/> where <img src="/tex/f9c4988898e7f532b9f826a75014ed3c.svg?invert_in_darkmode&sanitize=true" align=middle width=14.99998994999999pt height=22.465723500000017pt/> is an exponent of <img src="/tex/76c5792347bb90ef71cfbace628572cf.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/>, i.e. <img src="/tex/3004cfc6356b19184204c8c84f3ce0e2.svg?invert_in_darkmode&sanitize=true" align=middle width=52.59118094999999pt height=22.465723500000017pt/> then it is useful to directly generate the random number using the DRBG. This optimization is also often used by regular implementations of the Simple Discard Method, usually for smaller values of <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/>. This optimization is however not applicable to RSA-KEM or EC private key generation.  

Subtraction instead of comparison
---

It is possible to find which bit makes the comparison fail by subtracting the word of the candidate random number from the word at the same position in <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. This may however cause values of random bits to leak to a possible adversary, and it requires iterating over all the bits in both values.

Implementation tricks
===

Lowering the amount of calls to the RBG
---

It might be a good idea to generate additional bits or bytes in advance if the call to retrieve random bytes has a lot of overhead. That way the amount of calls to the RBG may be lowered. This would be identical to a a sliding window approach. In case the window reaches the end of the buffer then additional bytes may of course have to be generated.

Delaying the generation of the least significant bits
---

In principle the random bits are only required when they are being compared. So it is possible to generate the bits only when required. Once the number is lower than N then the rest of the bits can simply be filled by random values. Generally however this will slow down the generation rather than speed it up.

Combined sliding window and delay
---

It could be a good idea to only request a 16, 32 or 64 bit value in a machine word. Then the binary bit compare could be performed with another word using the most significant bits of <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/>. If the comparison fails then those bits can be discarded by shifting the value to the left. Finally, those bytes left in the word can be written to a byte array, followed by a generation of the missing random bits. Of course at least 8 bits of random data need to be left in the word for this to work.


Performance tests
===

Test description
---

The following tests are implemented:

 - Simple Discard Method Java - this is simply a call to the `BigInteger(int numBits, Random rnd)` constructor, followed by a comparison with the value of N;
 - Simple discard byte array - this is a re-implementation of the Simple Discard Method using a byte array, to make sure that the Java implementation performs as expected;
 - Simple Modular Method BigInteger - the simple modular method which generates a value that is 128 bits too large, and then divides by <img src="/tex/fbbba0a188c3c619c50f36b3d0a06a7c.svg?invert_in_darkmode&sanitize=true" align=middle width=27.87685064999999pt height=26.76175259999998pt/> to get a random number with negligible bias and known amount of random bits;
 - RNG-BC-1 - the most efficient 1 bit binary compare method that exactly generates as many bits as necessary for the Optimized Simple Discard Method;
 - RNG-BC-8 - the byte oriented implementation of RNG-BC, which generates and compared each byte separately;

Test setup
---

The tests were performed using the default `"DRBG"` secure random implementation in Java, which implements the counter based DRBG as specified in NIST [2].

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
 
Test procedure
---

All methods have been preceded by a warm up round to make sure that Java's just-in-time compiler has had time to optimize the loops.

All tests have been performed over one million generated random numbers.

First the name of the size of the range <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/> is displayed, followed by the value of <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/> in hexadecimals.
Then the different methods for generating the numbers are diplayed line by line with their results.

"avg bits" is the average number of bits required to generate each of the million random numbers.
"avg time" is the average time used by each run of the test in microseconds.

An "Ignore" value is generated. This value is just included in the printout to make sure that the byte code interpreter doesn't skip the random number generation altogether because the random value is not used afterwards.


Test results
---


```
 === Minimum 256 bit === 

8000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 512.217600 avg time 0.974000μs
Simple Discard Method byte array, avg bits: 512.185856 avg time 0.920000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.825000μs
RNG-BC-1, avg bits: 258.995854 avg time 0.921000μs
RNG-BC-8, avg bits: 264.063824 avg time 0.901000μs
Ignore: 4

 === Minimum 256 bit + 1 === 

8000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 512.227072 avg time 0.988000μs
Simple Discard Method byte array, avg bits: 511.478272 avg time 0.924000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.843000μs
RNG-BC-1, avg bits: 258.996509 avg time 0.916000μs
RNG-BC-8, avg bits: 264.052920 avg time 0.900000μs
Ignore: 58

 === Mid 256 bit - 1 === 

BFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 341.530624 avg time 0.644000μs
Simple Discard Method byte array, avg bits: 341.332736 avg time 0.642000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.881000μs
RNG-BC-1, avg bits: 256.666658 avg time 0.659000μs
RNG-BC-8, avg bits: 258.671544 avg time 0.615000μs
Ignore: 7

 === Mid 256 === 

C000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 341.577216 avg time 0.647000μs
Simple Discard Method byte array, avg bits: 341.373952 avg time 0.645000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.836000μs
RNG-BC-1, avg bits: 257.333234 avg time 0.673000μs
RNG-BC-8, avg bits: 258.710064 avg time 0.617000μs
Ignore: 27

 === Mid 256 + 1 === 

C000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 341.124608 avg time 0.644000μs
Simple Discard Method byte array, avg bits: 341.540096 avg time 0.646000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.846000μs
RNG-BC-1, avg bits: 257.336537 avg time 0.673000μs
RNG-BC-8, avg bits: 258.706352 avg time 0.616000μs
Ignore: 1

 === Max 256 - 1 === 

FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 256.000000 avg time 0.484000μs
Simple Discard Method byte array, avg bits: 256.000000 avg time 0.502000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.853000μs
RNG-BC-1, avg bits: 256.000000 avg time 0.535000μs
RNG-BC-8, avg bits: 256.000000 avg time 0.473000μs
Ignore: 205

 === Brainpool P512r1 === 

AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA703308717D4D9B009BC66842AECDA12AE6A380E62881FF2F2D82C68528AA6056583A48F3

Simple Discard Method Java, avg bits: 767.179776 avg time 0.973000μs
Simple Discard Method byte array, avg bits: 767.111168 avg time 0.929000μs
Simple Modular Method BigInteger, avg bits: 640.000000 avg time 1.387000μs
RNG-BC-1, avg bits: 513.320310 avg time 0.826000μs
RNG-BC-8, avg bits: 515.987224 avg time 0.852000μs
Ignore: 272

 === Random modulus 4096 === 

99A9312FD775C94D804827B6011C9FCC8180AD142ADEC24C772C24709DB89EB6FF4A7C8C876837920F8C49CF23FF8C9DA0D8E7FBB722E0D3E6671D39DEBD95079D56C57C0D9DB9D2CAD074BC57E22F8811BA163ECB3342F24CE97DF551BD3D9353796EFA7F5F9C9ED5EF98A7A0FF30D1E805FAA8EA8747A6AE3734CFD8046E56278D36AD2666C612270298FA87356DEDFFF13EAC082AB7D406510D367C6CA034DDB92C297B65A233B74BAAE3CA2165E3FE699DBCBE82B48831C52222F10FDBD29ACA5608616F09AD6FDD2BFE90ECB135F4976E93660714B2FCC0CD0A713723FF835229628480444E0DC75D3FD44AEE9465AC719C0A605E4F5917A91E09C0EBE23B08E467766D6329BB83443A17EAB39A310AAB8B59628F90F4B40B50F2AF6FF49C3307441FEAB0F0978177798123207B21AE8FD7A6CC5D307155843161EE4C54E3685BA402F5A2F155A52C4521B76ECA6E2B9D5362341E37B8CB4DCBF7BC9CAAFFBD31A62CE3240F5161F43598D0F8AE5FE418F4784F8DDD444BD314E73F8C870895BDE24388BB905E42AE80DE34885C6D64EDBAB2E852053B737BF85471D5DFED98BA878E806DE82AA762791DACEF47895D66378B6F94256A0D3E7E77026AAB58540246EB9952C8C54777FAA879026498C169835DF5866FE867734D21E9E921D6A6FCF64260D8B98F195D0585D445D611607008F7D15407FCC32C1A55F1F9A5

Simple Discard Method Java, avg bits: 6827.323392 avg time 4.658000μs
Simple Discard Method byte array, avg bits: 6825.844736 avg time 4.106000μs
Simple Modular Method BigInteger, avg bits: 4224.000000 avg time 6.267000μs
RNG-BC-1, avg bits: 4097.732409 avg time 2.962000μs
RNG-BC-8, avg bits: 4101.349768 avg time 3.034000μs
Ignore: 3828
```

Test interpretation
---

The RNG-BC-1 implementation uses the least amount of bits from the underlying implementation. However, it requires special buffering techniques for it to retrieve bits instead of bytes from the underlying RBG. This slows down the implementation quite a bit, leaving it trailing the byte oriented RNG-BC versions. Note that it only uses about 0-3 additional bits from the RBG compared to the number of bits within the number.

The RNG-BC-8 implementation is a close second when it comes to consuming bits. However, because the implementation only acts on full bytes, it is usually somewhat faster than RNG-BC-1.

The two Simple Discard Methods will consume about the same amount of bits on average. It shows that the Simple Discard Method that compares bytes directly is slightly faster than the one that converts to `BigInteger` and then performs the comparison.  

Conclusion
===

Simply by combining the comparison and random number generation it is easy to significantly bring down the number of bits requested from the underlying Random Bit Generator. There are no apparent drawbacks, such as requiring division using large operands required by the Simple Modular Method or requiring memory beyond the storage of the value itself.

The number of additional bits does not grow with the size of the range. This is different from the Simple Discard Method where the additional number of bits grows linearly with the range. That means that the Optimized Simple Discard method is relatively more effective when the range is large such as in RSA-KEM.

The Optimized Simple Discard Method can therefore be used at any place where one of the other methods are used. However, it will have the most impact when dealing with a slow random number generator as it significantly reduces the number of bits that need to be generated.

Next steps
===

The following followup actions should be considered:

 - The implementations of RNG_BC should be made compatible for a number range of any bit size (using masking).
 - A mathematical calculation should be performed to show that the test results are indeed expected and correct.
 - The maximum number of bits used should be shown to indicate that the worse case scenario of the Optimized Simple Discard Method is much better than the Optimized Simple Discard Method.
 - The sliding window approach with delayed generation should be implemented to see if that speeds up the implementation significantly.

References
===

References:
 - [1] Elaine Barker, John Kelsey: NIST SP 800-90A Rev. 1: Recommendation for Random Number Generation Using Deterministic RBGs.
 - [2] M. Lochter, J. Merkle: RFC 5639: Elliptic Curve Cryptography (ECC) Brainpool Standard Curves and Curve Generation
 - [3] V. Shoup: A Proposal for an ISO Standard for Public Key Encryption (version 2.1)

