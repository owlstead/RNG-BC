The Optimized Simple Discard Method
===

Author: Maarten Bodewes  
Date: 2017-03-06 (initial draft)  
Date: 2018-06-01 (revised draft)  
Date: 2020-03-23 (release candidate)  
Date: 2020-03-23 (release candidate 2)
 
Excerpt
---

This document provides a specification and analysis of the Optimized Simple Discard Method. As the name implies the Optimized Simple Discard Method is an optimization of the Simple Discard Method that is specified by NIST in NIST SP 800-90A Rev 1. The Simple Discard Method is used to generate well distributed random numbers in a range <img src="/tex/5edd9fe437a36f729249e68619b408bf.svg?invert_in_darkmode&sanitize=true" align=middle width=34.35698804999999pt height=24.65753399999998pt/> given a well distributed random bit generator or DRBG. The Simple Discard Method is colloquially known as *rejection sampling*.  

The Optimized Simple Discard Method is implemented by an algorithm called RNG-BC. RNG-BC is an acronym for Random Number Generator using Binary Compare. We will also introduce a derived random number generator RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>, which operates on words with bit size <img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>. For instance, RNG-BC-8 operates on bytes instead of bits.

First we prove by reduction that the implementation is not biased. Secondly we show that the Optimized method is highly efficient with regards to usage of the underlying RBG by creating a performance analysis that shows the number of bits retrieved from the DRBG.

Notation
---

This document follows the notation of NIST SP 800-90A Rev 1, Appendix A, section A.5. 

Terms:

 - RBG: Random Bit Generator
 - RNG: Random Number Generator (for a number in a specific range)
 - <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: the amount of elements in the range <img src="/tex/5edd9fe437a36f729249e68619b408bf.svg?invert_in_darkmode&sanitize=true" align=middle width=34.35698804999999pt height=24.65753399999998pt/>
 - <img src="/tex/de3e4ddbaf93c2db6b330ad1998cc995.svg?invert_in_darkmode&sanitize=true" align=middle width=14.517775799999992pt height=14.15524440000002pt/>: the bit value of the bit at position <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> within <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>
 - <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/>: the minimum number of bit required to encode <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>
 - <img src="/tex/d3aa71141bc89a24937c86ec1d350a7c.svg?invert_in_darkmode&sanitize=true" align=middle width=11.705695649999988pt height=22.831056599999986pt/>: the bit value of the bit at position <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> within the candidate
 - <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/>: a candidate value of <img src="/tex/2f2322dff5bde89c37bcae4116fe20a8.svg?invert_in_darkmode&sanitize=true" align=middle width=5.2283516999999895pt height=22.831056599999986pt/> bits in the range <img src="/tex/3fee4d560002b7ddcc15f4500229c5cb.svg?invert_in_darkmode&sanitize=true" align=middle width=47.19000659999998pt height=24.65753399999998pt/> in case of the (Optimized) Simple Discard Method
 - <img src="/tex/44bc9d542a92714cac84e01cbbb7fd61.svg?invert_in_darkmode&sanitize=true" align=middle width=8.68915409999999pt height=14.15524440000002pt/>: the final value in the range <img src="/tex/5edd9fe437a36f729249e68619b408bf.svg?invert_in_darkmode&sanitize=true" align=middle width=34.35698804999999pt height=24.65753399999998pt/>
 - <img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>: the amount of bits to compare at the same time within the binary compare (in RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>) 
 - <img src="/tex/9b325b9e31e85137d1de765f43c0f8bc.svg?invert_in_darkmode&sanitize=true" align=middle width=12.92464304999999pt height=22.465723500000017pt/>: a constant to add to the value to create a value in the range <img src="/tex/1d720c949b8a0056debc585f42aba362.svg?invert_in_darkmode&sanitize=true" align=middle width=71.16499995pt height=24.65753399999998pt/> 

Numbers have a value <img src="/tex/9e80b5935c9432f3f74c8b868a2f5148.svg?invert_in_darkmode&sanitize=true" align=middle width=74.80231604999999pt height=31.36100879999999pt/> where <img src="/tex/0d7db8b1c4836b9c5a03e4e9628a50a7.svg?invert_in_darkmode&sanitize=true" align=middle width=74.63278019999998pt height=22.831056599999986pt/> is the little endian bit representation of the value.

Introduction
---

This document describes an efficient random number generator for number generation in a large range using a random bit generator. The result of the random number generation is a natural number <img src="/tex/44bc9d542a92714cac84e01cbbb7fd61.svg?invert_in_darkmode&sanitize=true" align=middle width=8.68915409999999pt height=14.15524440000002pt/> in the range <img src="/tex/5edd9fe437a36f729249e68619b408bf.svg?invert_in_darkmode&sanitize=true" align=middle width=34.35698804999999pt height=24.65753399999998pt/>.

It is easy to generate a number in any range <img src="/tex/9e233fec5a4f5238feb0a47e4d50a8cf.svg?invert_in_darkmode&sanitize=true" align=middle width=32.96428244999999pt height=24.65753399999998pt/> by setting <img src="/tex/a4093aa53811121a968ce526f5504c1a.svg?invert_in_darkmode&sanitize=true" align=middle width=69.19311794999999pt height=24.7161288pt/> and generating <img src="/tex/4fc63d27626433f23e36eca761bac52b.svg?invert_in_darkmode&sanitize=true" align=middle width=12.47911664999999pt height=24.7161288pt/> in range <img src="/tex/e265cc01a3ee30b742e0ebda637052a0.svg?invert_in_darkmode&sanitize=true" align=middle width=38.96886344999999pt height=24.7161288pt/>, finally adjusting the value <img src="/tex/6f9c26f5178d1b3f0a7fb1c2b371db42.svg?invert_in_darkmode&sanitize=true" align=middle width=69.22735544999999pt height=24.7161288pt/>. This kind of random number generation is required for cryptographic operations such as the generation of EC private keys or the generation of the master secret in RSA-KEM [3].

NIST has described three such methods in NIST SP 800-90A Rev 1. We will shortly introduce these before specifying the Optimized Simple Discard Method. 

The Simple Discard method
---

The simplest method of generating a random value in this range is to generate a candidate value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> in the range <img src="/tex/52514e6fa02d52b78e32c712ab1808ff.svg?invert_in_darkmode&sanitize=true" align=middle width=47.19000659999998pt height=24.65753399999998pt/> where <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is the minimum number of bits required to encode <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. Mathematically <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> is identical to <img src="/tex/9e014f10acf7264e72a6ebbdbabd2ca7.svg?invert_in_darkmode&sanitize=true" align=middle width=67.53057629999998pt height=27.94539330000001pt/>. After generation value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is compared with value <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. If the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is higher or equal then the value is discarded and regenerated. If the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is lower then the value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is accepted; <img src="/tex/dd51d197cbfa4b1e69f223993273f48c.svg?invert_in_darkmode&sanitize=true" align=middle width=37.720588949999986pt height=14.15524440000002pt/>. The Simple Discard Method has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.1 [1].

The Complex Discard Method
---

The complex discard method will not be evaluated; it has been specified to generate many random numbers in a specific range and does not seem efficient for embedded systems if only due to the memory requirements of generating multiple random numbers at once. It has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.2 [1]. 

The Simple Modular method
---

The simple modular method uses a security parameter <img src="/tex/6f9bad7347b91ceebebd3ad7e6f6f2d1.svg?invert_in_darkmode&sanitize=true" align=middle width=7.7054801999999905pt height=14.15524440000002pt/> which will allow a constant time generation of numbers using <img src="/tex/3e0932bef26a1d7c55923260ace151ef.svg?invert_in_darkmode&sanitize=true" align=middle width=42.22977164999999pt height=19.1781018pt/> bits, where <img src="/tex/6f9bad7347b91ceebebd3ad7e6f6f2d1.svg?invert_in_darkmode&sanitize=true" align=middle width=7.7054801999999905pt height=14.15524440000002pt/> is a predetermined security parameter. It generates a candidate value <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> using <img src="/tex/3e0932bef26a1d7c55923260ace151ef.svg?invert_in_darkmode&sanitize=true" align=middle width=42.22977164999999pt height=19.1781018pt/> bits, where <img src="/tex/43a16c26a4cfeae3ae89c7f3d08e55f9.svg?invert_in_darkmode&sanitize=true" align=middle width=86.23269929999998pt height=22.831056599999986pt/>. This introduces a certain bias that is largely dependent on the size of <img src="/tex/6f9bad7347b91ceebebd3ad7e6f6f2d1.svg?invert_in_darkmode&sanitize=true" align=middle width=7.7054801999999905pt height=14.15524440000002pt/>. 

Unfortunately, <img src="/tex/337ff6cbf64be9acf1b61502668200b8.svg?invert_in_darkmode&sanitize=true" align=middle width=62.18605964999999pt height=22.831056599999986pt/> cannot be calculated in advance and taking the modulus using large operands is inefficient, especially on embedded systems or smart cards. The Simple Modular Method has been standardized in NIST SP 800-90A Rev 1, Appendix A, section A.5.3 [1].

The Optimized Simple Discard method
===

Description
---

As the name suggests, the Optimized Simple Discard method is an enhancement of the Simple Discard method. It hinges on one single observation: only the most significant bits are actually compared during a binary comparison operation. The least significant bits are left untouched and should therefore still be considered random.

The Optimized Simple Discard method therefore does not regenerate all <img src="/tex/a5e90edb91e096daa9080a4e519585cc.svg?invert_in_darkmode&sanitize=true" align=middle width=35.546213999999985pt height=22.831056599999986pt/> to <img src="/tex/ea8adcd8cf746feb94319864409cdc74.svg?invert_in_darkmode&sanitize=true" align=middle width=13.60734374999999pt height=22.831056599999986pt/> bits, it only regenerates the bits at position <img src="/tex/a5e90edb91e096daa9080a4e519585cc.svg?invert_in_darkmode&sanitize=true" align=middle width=35.546213999999985pt height=22.831056599999986pt/> to <img src="/tex/d3aa71141bc89a24937c86ec1d350a7c.svg?invert_in_darkmode&sanitize=true" align=middle width=11.705695649999988pt height=22.831056599999986pt/> inclusive where <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> is the bit that made the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> higher than the value of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>.

Binary comparison
---

The following is a description of binary comparison, which is performed using within a processor to determine if a value is lower than, equal to or greater than another value. It is hidden away in the operation <img src="/tex/9ada1beba45fac9acba26d7fa821df7a.svg?invert_in_darkmode&sanitize=true" align=middle width=36.90439004999999pt height=17.723762100000005pt/> within the Simple Discard Method and made explicit in the Optimized Simple Discard Method.

Bitwise the following calculations are performed when binary comparison is used, where index <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> decrements from <img src="/tex/aac03c299a5a829c1f94d55c54791cc4.svg?invert_in_darkmode&sanitize=true" align=middle width=42.743500799999985pt height=21.18721440000001pt/> to <img src="/tex/29632a9bf827ce0200454dd32fc3be82.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/>:

 1. If <img src="/tex/edcc16007110b4cbf0fa08066b1d8fb1.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/f57d0e610d6c18030713774375d3d739.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then continue with next bit, the most significant bits are equal;
 2. If <img src="/tex/edcc16007110b4cbf0fa08066b1d8fb1.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/1aa28eca4aded955d20db44177279dbf.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is larger than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: regenerate <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> and start over;
 3. If <img src="/tex/480fa181cbc6eeff17cf636eca251c88.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/f57d0e610d6c18030713774375d3d739.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is smaller than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: return <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/>;
 4. If <img src="/tex/480fa181cbc6eeff17cf636eca251c88.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/1aa28eca4aded955d20db44177279dbf.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then continue with next bit, the most significant bits are equal.

The Optimized Simple Discard Method simply changes option 2 to:

If <img src="/tex/edcc16007110b4cbf0fa08066b1d8fb1.svg?invert_in_darkmode&sanitize=true" align=middle width=45.476512949999986pt height=21.18721440000001pt/> and <img src="/tex/1aa28eca4aded955d20db44177279dbf.svg?invert_in_darkmode&sanitize=true" align=middle width=42.66443279999999pt height=22.831056599999986pt/> then the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is larger than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>: **regenerate bits at position <img src="/tex/51cca662bd7a14f18a33336cbe87b266.svg?invert_in_darkmode&sanitize=true" align=middle width=72.73110569999999pt height=22.831056599999986pt/>** and start over;

Finally, if all the bits have equal values then <img src="/tex/e419b7cbc237e7f8ba4408d119eb5010.svg?invert_in_darkmode&sanitize=true" align=middle width=36.90439004999999pt height=14.15524440000002pt/>, so all <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> bits need to be regenerated. The probability of of this happen is over <img src="/tex/5a41538a4ae89c745f899d9a20e0c176.svg?invert_in_darkmode&sanitize=true" align=middle width=17.467835549999997pt height=27.77565449999998pt/> so it is unlikely to happen for large values of <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/> and thus <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>.

Proof of equivalence
---

If we look at this scheme we notice that only the leftmost <img src="/tex/7eb0c12c4968fb243ade57cecbbbf84c.svg?invert_in_darkmode&sanitize=true" align=middle width=40.187518799999985pt height=21.68300969999999pt/> bits are actually evaluated in the Simple Discard Method. Any bits that are less significant will never be compared or otherwise operated on. This means that they should still be considered random. 

Another way of looking at it would be that the bits at location <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/> only need to be generated *right before they need to be compared*; the rest of the bits can be generated after <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is determined to be smaller. Any bit that has been evaluated must be considered invalid and must however be regenerated, i.e. bits <img src="/tex/aac03c299a5a829c1f94d55c54791cc4.svg?invert_in_darkmode&sanitize=true" align=middle width=42.743500799999985pt height=21.18721440000001pt/> to <img src="/tex/77a3b857d53fb44e33b53e4c8b68351a.svg?invert_in_darkmode&sanitize=true" align=middle width=5.663225699999989pt height=21.68300969999999pt/>.

The Optimized Simple Discard Method is functionality equivalent to the Simple Discard method. Since the Simple Discard Method doesn't produce biased output, it can be concluded that this method doesn't produce biased output either. 

Side channel attacks
---

The Optimal Simple Discard method is as vulnerable against side channel attacks as the Simple Discard method: it may be detectable which bit indicates that the value of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> is lower than <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. This may indicate to an attacker how many of the most significant bits of <img src="/tex/3e18a4a28fdee1744e5e3f79d13b9ff6.svg?invert_in_darkmode&sanitize=true" align=middle width=7.11380504999999pt height=14.15524440000002pt/> are identical to the most significant bits of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. Care should be taken to ensure that the comparison operations leak as little information as possible.

It may be easier to detect how many bits are identical once comparison fails for the Optimized Simple Discard method; the number of regenerated bits are a clear indication. As this only leaks information about the *rejected* bits of a candidate this is unlikely to provide any advantage to an adversary.

Variations
===

RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/>
---

RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is a generalization of the Optimized Simple Discard method for different word sizes. If <img src="/tex/9286d5c1919e045d6b78487ee17f64ac.svg?invert_in_darkmode&sanitize=true" align=middle width=44.71834949999999pt height=14.15524440000002pt/> then RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is identical to the Simple Discard Method and no optimization takes place. If <img src="/tex/429e67fc243a92515642c63b5a91a4fc.svg?invert_in_darkmode&sanitize=true" align=middle width=38.50445939999999pt height=21.18721440000001pt/> then RNG-BC-<img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> is identical to the Optimized Simple Discard method.

Depending on the speed of the PRNG is may be more useful to group <img src="/tex/f93ce33e511096ed626b4719d50f17d2.svg?invert_in_darkmode&sanitize=true" align=middle width=8.367621899999993pt height=14.15524440000002pt/> bits together to perform the comparison operation. This has the disadvantage that - on average - it will require more bits to be generated. However, generally computers are optimized to operate on bytes - i.e. <img src="/tex/7d2031baf3ef5ec99b4ff8b0c292a411.svg?invert_in_darkmode&sanitize=true" align=middle width=38.50445939999999pt height=21.18721440000001pt/> - or machine specific words. This means that RNG-BC-8 is likely to be faster than RNG-BC-1 on most systems. It also greatly simplifies the code and reduces the code size.

Skip when r is 2^x
---

When a number is generated in the range <img src="/tex/7af579f25b0d26f76a0a404b137bec0d.svg?invert_in_darkmode&sanitize=true" align=middle width=34.35698804999999pt height=24.65753399999998pt/> where <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> is an exponent of <img src="/tex/76c5792347bb90ef71cfbace628572cf.svg?invert_in_darkmode&sanitize=true" align=middle width=8.219209349999991pt height=21.18721440000001pt/>, i.e. <img src="/tex/ab5afa8215406d2cedbeb874e528efff.svg?invert_in_darkmode&sanitize=true" align=middle width=45.46416764999999pt height=21.839370299999988pt/> then it is useful to directly generate the random number using the DRBG. This optimization is also often used by regular implementations of the Simple Discard Method, usually for smaller values of <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/>. This optimization is however not applicable to RSA-KEM or EC private key generation.  

Subtraction instead of comparison
---

It is possible to find which bit makes the comparison fail by subtracting the word of the candidate random number from the word at the same position in <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. This may however cause values of random bits to leak to a possible adversary, and it requires iterating over all the bits in both values. It is therefore not recommended to choose subtraction over comparison.

Performance tests
===

Test description
---

The following tests are implemented:

 - Simple Discard Method Java - this is simply a call to the `BigInteger(int numBits, Random rnd)` constructor, followed by a comparison with the value of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>;
 - Simple discard byte array - this is a re-implementation of the Simple Discard Method using a byte array, to make sure that the Java implementation performs as expected and to make sure that the performance comparison is fair;
 - Simple Modular Method BigInteger - the simple modular method with <img src="/tex/6f9bad7347b91ceebebd3ad7e6f6f2d1.svg?invert_in_darkmode&sanitize=true" align=middle width=7.7054801999999905pt height=14.15524440000002pt/> set to 128;
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

First the name of the size of the range <img src="/tex/55a049b8f161ae7cfeb0197d75aff967.svg?invert_in_darkmode&sanitize=true" align=middle width=9.86687624999999pt height=14.15524440000002pt/> is displayed, followed by the value of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> in hexadecimals.
Then the different methods for generating the numbers are displayed line by line with their results.

"avg bits" is the average number of bits required to generate each of the million random numbers.
"avg time" is the average time used by each run of the test in microseconds.

An "Ignore" value is generated. This value is just included in the printout to make sure that the byte code interpreter doesn't skip the random number generation altogether because the random value is not used afterwards. It needs to be ignored; it has no meaningful value.

Test results
---

```

 === Minimum 256 bit === 

8000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 511.585792 avg time 0.996000μs
Simple Discard Method byte array, avg bits: 512.218368 avg time 0.937000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.831000μs
RNG-BC-1, avg bits: 258.990135 avg time 0.589000μs
RNG-BC-8, avg bits: 264.057368 avg time 0.948000μs
Ignore: 14

 === Minimum 256 bit + 1 === 

8000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 511.357440 avg time 1.027000μs
Simple Discard Method byte array, avg bits: 512.349440 avg time 0.941000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.856000μs
RNG-BC-1, avg bits: 258.993455 avg time 0.565000μs
RNG-BC-8, avg bits: 264.061944 avg time 0.956000μs
Ignore: 58

 === Mid 256 bit - 1 === 

BFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 341.540608 avg time 0.677000μs
Simple Discard Method byte array, avg bits: 341.365760 avg time 0.644000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.896000μs
RNG-BC-1, avg bits: 256.666582 avg time 0.505000μs
RNG-BC-8, avg bits: 258.667352 avg time 0.654000μs
Ignore: 50

 === Mid 256 === 

C000000000000000000000000000000000000000000000000000000000000000

Simple Discard Method Java, avg bits: 341.157120 avg time 0.674000μs
Simple Discard Method byte array, avg bits: 341.605376 avg time 0.642000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.841000μs
RNG-BC-1, avg bits: 257.335601 avg time 0.518000μs
RNG-BC-8, avg bits: 258.704960 avg time 0.653000μs
Ignore: 61

 === Mid 256 + 1 === 

C000000000000000000000000000000000000000000000000000000000000001

Simple Discard Method Java, avg bits: 341.302272 avg time 0.673000μs
Simple Discard Method byte array, avg bits: 341.072384 avg time 0.643000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.861000μs
RNG-BC-1, avg bits: 257.331965 avg time 0.518000μs
RNG-BC-8, avg bits: 258.705736 avg time 0.654000μs
Ignore: 247

 === Max 256 - 1 === 

FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF

Simple Discard Method Java, avg bits: 256.000000 avg time 0.504000μs
Simple Discard Method byte array, avg bits: 256.000000 avg time 0.495000μs
Simple Modular Method BigInteger, avg bits: 384.000000 avg time 0.863000μs
RNG-BC-1, avg bits: 256.000000 avg time 0.485000μs
RNG-BC-8, avg bits: 256.000000 avg time 0.507000μs
Ignore: 230

 === Brainpool P512r1 === 

AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA703308717D4D9B009BC66842AECDA12AE6A380E62881FF2F2D82C68528AA6056583A48F3

Simple Discard Method Java, avg bits: 767.660544 avg time 0.980000μs
Simple Discard Method byte array, avg bits: 766.079488 avg time 0.897000μs
Simple Modular Method BigInteger, avg bits: 640.000000 avg time 1.387000μs
RNG-BC-1, avg bits: 513.321300 avg time 0.670000μs
RNG-BC-8, avg bits: 515.990880 avg time 0.856000μs
Ignore: 292

 === Random modulus 4096 === 

99A9312FD775C94D804827B6011C9FCC8180AD142ADEC24C772C24709DB89EB6FF4A7C8C876837920F8C49CF23FF8C9DA0D8E7FBB722E0D3E6671D39DEBD95079D56C57C0D9DB9D2CAD074BC57E22F8811BA163ECB3342F24CE97DF551BD3D9353796EFA7F5F9C9ED5EF98A7A0FF30D1E805FAA8EA8747A6AE3734CFD8046E56278D36AD2666C612270298FA87356DEDFFF13EAC082AB7D406510D367C6CA034DDB92C297B65A233B74BAAE3CA2165E3FE699DBCBE82B48831C52222F10FDBD29ACA5608616F09AD6FDD2BFE90ECB135F4976E93660714B2FCC0CD0A713723FF835229628480444E0DC75D3FD44AEE9465AC719C0A605E4F5917A91E09C0EBE23B08E467766D6329BB83443A17EAB39A310AAB8B59628F90F4B40B50F2AF6FF49C3307441FEAB0F0978177798123207B21AE8FD7A6CC5D307155843161EE4C54E3685BA402F5A2F155A52C4521B76ECA6E2B9D5362341E37B8CB4DCBF7BC9CAAFFBD31A62CE3240F5161F43598D0F8AE5FE418F4784F8DDD444BD314E73F8C870895BDE24388BB905E42AE80DE34885C6D64EDBAB2E852053B737BF85471D5DFED98BA878E806DE82AA762791DACEF47895D66378B6F94256A0D3E7E77026AAB58540246EB9952C8C54777FAA879026498C169835DF5866FE867734D21E9E921D6A6FCF64260D8B98F195D0585D445D611607008F7D15407FCC32C1A55F1F9A5

Simple Discard Method Java, avg bits: 6829.109248 avg time 4.443000μs
Simple Discard Method byte array, avg bits: 6827.999232 avg time 3.834000μs
Simple Modular Method BigInteger, avg bits: 4224.000000 avg time 6.129000μs
RNG-BC-1, avg bits: 4097.731496 avg time 2.616000μs
RNG-BC-8, avg bits: 4101.349936 avg time 2.938000μs
Ignore: 3685
```

Test interpretation
---

The RNG-BC-1 implementation uses the least amount of bits from the underlying implementation. However, it requires special buffering techniques for it to retrieve bits instead of bytes from the underlying RBG. This slows down the implementation quite a bit, which means it is sometimes slower than the byte oriented RNG-BC-8. Note that it only uses about 0-3 additional bits over <img src="/tex/0e51a2dede42189d77627c4d742822c3.svg?invert_in_darkmode&sanitize=true" align=middle width=14.433101099999991pt height=14.15524440000002pt/>, the number of bits in <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/>. The RNG-BC-8 implementation is a close second when it comes to consuming bits.

The two Simple Discard Methods will consume about the same amount of bits on average. It shows that the Simple Discard Method that compares bytes directly is slightly faster than the one that converts to `BigInteger` and then performs the comparison. For small values it may be faster than RNG-BC-1 or RNG-BC-8, but it loses ground for an <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> larger than 256 bits.

Note that the `RandomBitGenerator` caches some bits to be able to supply them to the RNG-BC-1. The fewer calls to the `SecureRandom` implementation will likely explain the advantage that it seems to have over RNG-BC-8. The code of RNG-BC-1 is otherwise slightly more complex, so it should be slower. 

Conclusion
===

Simply by combining the comparison and random number generation it is easy to significantly bring down the number of bits requested from the underlying Random Bit Generator. There are no apparent drawbacks, such as requiring division using large operands required by the Simple Modular Method or requiring memory beyond the storage of the value itself.

The number of additional bits does not grow with the size of the range. This is different from the Simple Discard Method where the additional number of bits grows linearly with the range. That means that the Optimized Simple Discard method is relatively more effective when the range is large such as in RSA-KEM.

The Optimized Simple Discard Method can therefore be used at any place where one of the other methods are used. However, it will have the most impact when dealing with a relatively slow random number generator.

Next steps
===

The following next steps should be considered:

 - The implementations of RNG_BC should be made compatible for a number range of any bit size (using masking); currently the implementation only allow for <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> where <img src="/tex/ecac220fd1eb6013d621242315cd5484.svg?invert_in_darkmode&sanitize=true" align=middle width=93.42830474999998pt height=22.831056599999986pt/>, i.e. the most significant bit of <img src="/tex/89f2e0d2d24bcf44db73aab8fc03252c.svg?invert_in_darkmode&sanitize=true" align=middle width=7.87295519999999pt height=14.15524440000002pt/> is the most significant bit of a byte.
 - A mathematical calculation should be performed to show that the test results are indeed expected and correct.
 - The maximum number of bits used should be shown to indicate that the Optimized Simple Discard Method is fares much better than the Simple Discard Method in scenarios where the many candidates need to be tested.
 
References
===

References:
 - [1] Elaine Barker, John Kelsey: NIST SP 800-90A Rev. 1: Recommendation for Random Number Generation Using Deterministic RBGs.
 - [2] M. Lochter, J. Merkle: RFC 5639: Elliptic Curve Cryptography (ECC) Brainpool Standard Curves and Curve Generation
 - [3] V. Shoup: A Proposal for an ISO Standard for Public Key Encryption (version 2.1)
