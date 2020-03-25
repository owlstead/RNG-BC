package nl.maartenbodewes.rng_bc;

import static nl.maartenbodewes.rng_bc.Util.fromHex;
import static nl.maartenbodewes.rng_bc.Util.toHex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Performs tests on a large set of random numbers to see how many bits and micro- / nano-seconds are used
 * by the various implementations of random number generators.
 * 
 * @author maartenb
 */
public class PerformanceTests {
    private static final byte[] MIN_256 =
            fromHex("8000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] MIN_256_PLUS_1 =
            fromHex("8000000000000000000000000000000000000000000000000000000000000001");

    private static final byte[] MID_256_PLUS_ONE =
            fromHex("C000000000000000000000000000000000000000000000000000000000000001");
    private static final byte[] MID_256 =
            fromHex("C000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] MID_256_MINUS_ONE =
            fromHex("BFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

    private static final byte[] MAX_256_MINUS_ONE =
            fromHex("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

    private static final byte[] BRAINPOOLP512R1_Q =
            fromHex("AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA70330870"
                    + "553E5C414CA92619418661197FAC10471DB1D381085DDADDB58796829CA90069");

    private static final byte[] MODULUS_4096 =
            fromHex("99a9312fd775c94d804827b6011c9fcc8180ad142adec24c772c24709db89eb6"
                    + "ff4a7c8c876837920f8c49cf23ff8c9da0d8e7fbb722e0d3e6671d39debd9507"
                    + "9d56c57c0d9db9d2cad074bc57e22f8811ba163ecb3342f24ce97df551bd3d93"
                    + "53796efa7f5f9c9ed5ef98a7a0ff30d1e805faa8ea8747a6ae3734cfd8046e56"
                    + "278d36ad2666c612270298fa87356dedfff13eac082ab7d406510d367c6ca034"
                    + "ddb92c297b65a233b74baae3ca2165e3fe699dbcbe82b48831c52222f10fdbd2"
                    + "9aca5608616f09ad6fdd2bfe90ecb135f4976e93660714b2fcc0cd0a713723ff"
                    + "835229628480444e0dc75d3fd44aee9465ac719c0a605e4f5917a91e09c0ebe2"
                    + "3b08e467766d6329bb83443a17eab39a310aab8b59628f90f4b40b50f2af6ff4"
                    + "9c3307441feab0f0978177798123207b21ae8fd7a6cc5d307155843161ee4c54"
                    + "e3685ba402f5a2f155a52c4521b76eca6e2b9d5362341e37b8cb4dcbf7bc9caa"
                    + "ffbd31a62ce3240f5161f43598d0f8ae5fe418f4784f8ddd444bd314e73f8c87"
                    + "0895bde24388bb905e42ae80de34885c6d64edbab2e852053b737bf85471d5df"
                    + "ed98ba878e806de82aa762791dacef47895d66378b6f94256a0d3e7e77026aab"
                    + "58540246eb9952c8c54777faa879026498c169835df5866fe867734d21e9e921"
                    + "d6a6fcf64260d8b98f195d0585d445d611607008f7d15407fcc32c1a55f1f9a5");

    private static final HashMap<String, byte[]> PERFORMANCE_TESTS = new HashMap<>();
    private static final List<String> PERFORMANCE_TESTS_ORDER = new LinkedList<>();
    private static final int SIMPLE_MODULAR_METHOD_ADDITIONAL_BITS = 128;

    static {
        addPerformanceTest("Minimum 256 bit", MIN_256);
        addPerformanceTest("Minimum 256 bit + 1", MIN_256_PLUS_1);
        addPerformanceTest("Mid 256 bit - 1", MID_256_MINUS_ONE);
        addPerformanceTest("Mid 256", MID_256);
        addPerformanceTest("Mid 256 + 1", MID_256_PLUS_ONE);
        addPerformanceTest("Max 256 - 1", MAX_256_MINUS_ONE);
        addPerformanceTest("Brainpool P512r1 Q", BRAINPOOLP512R1_Q);
        addPerformanceTest("Random modulus 4096", MODULUS_4096);
    }

    private static void addPerformanceTest(String name, byte[] value) {
        PERFORMANCE_TESTS.put(name, value);
        PERFORMANCE_TESTS_ORDER.add(name);
    }

    public static void main(String[] args) throws Exception {
        // byte[] n = BRAINPOOLP512R1_P;
        byte[] n = MIN_256;
        BigInteger nbi = Util.os2ip(n, n.length);

        long testSize = 1000000;

        // only used to make sure that JIT doesn't remove any loops
        int ignoreValueAggregator = 0;
        SecureRandom drbg = SecureRandom.getInstance("DRBG");
        CountingSecureRandom countingRNG = new CountingSecureRandom(drbg);
        RandomBitGenerator rbg = new RandomBitGenerator(drbg);

        // === warm up JIT loops

        // startup simple discard BigInteger
        {
            for (long i = 0; i < testSize; i++) {
                BigInteger xbi;
                do {
                    xbi = new BigInteger(n.length * Byte.SIZE, countingRNG);
                } while (xbi.compareTo(nbi) >= 0);
                ignoreValueAggregator ^= xbi.bitCount();
            }
        }

        // startup simple discard byte array
        {
            RandomNumberGenerator rngbc = new RNGSimpleDiscard(rbg);
            for (long i = 0; i < testSize; i++) {
                // let's play fair and create the byte array in the loop
                byte[] x = new byte[n.length];
                rngbc.next(n, x);
                BigInteger xbi = new BigInteger(1, x);
                ignoreValueAggregator ^= xbi.bitCount();
            }
        }

        // startup simple modular
        {
            int bits = n.length * Byte.SIZE + SIMPLE_MODULAR_METHOD_ADDITIONAL_BITS;
            for (long i = 0; i < testSize; i++) {
                BigInteger xxbi = new BigInteger(bits, countingRNG);
                BigInteger xbi = xxbi.mod(nbi);
                ignoreValueAggregator ^= xbi.bitCount();
            }
        }

        // startup RNG-BC
        {
            RandomNumberGenerator rngbc = new RNG_BC(rbg);
            for (long i = 0; i < testSize; i++) {
                byte[] x = new byte[n.length];
                rngbc.next(n, x);
                BigInteger xbi = new BigInteger(1, x);
                ignoreValueAggregator ^= xbi.bitCount();
            }
        }

        // startup RNG-BC-8
        {
            RandomNumberGenerator rngbc = new RNG_BC_8(rbg);
            for (long i = 0; i < testSize; i++) {
                // let's play fair and create the byte array in the loop
                byte[] x = new byte[n.length];
                rngbc.next(n, x);
                BigInteger xbi = new BigInteger(1, x);
                ignoreValueAggregator ^= xbi.bitCount();
            }
        }

        // === actual tests

        for (String valueName : PERFORMANCE_TESTS_ORDER) {
            n = PERFORMANCE_TESTS.get(valueName);
            nbi = Util.os2ip(n, n.length);

            System.out.printf("%n === %s === %n%n%s%n%n", valueName, toHex(n));

            // test simple discard
            {
                countingRNG.resetBitCount();
                long start = System.currentTimeMillis();
                for (long i = 0; i < testSize; i++) {
                    BigInteger xbi;
                    do {
                        xbi = new BigInteger(n.length * Byte.SIZE, countingRNG);
                    } while (xbi.compareTo(nbi) > 0);
                    ignoreValueAggregator ^= xbi.bitCount();
                }
                long end = System.currentTimeMillis();
                double avgBitCount = (double) countingRNG.getBitCount() / testSize;
                double avgTimeNS = (double) (end - start) * 1000 / testSize;
                System.out.printf("Simple Discard Method Java, avg bits: %f avg time %fμs%n", avgBitCount, avgTimeNS);
            }

            // test simple discard byte array
            {
                rbg.resetBitCount();
                RandomNumberGenerator rngbc = new RNGSimpleDiscard(rbg);

                long start = System.currentTimeMillis();
                for (long i = 0; i < testSize; i++) {
                    // let's play fair and create the byte array in the loop
                    byte[] x = new byte[n.length];
                    rngbc.next(n, x);
                    BigInteger xbi = new BigInteger(1, x);
                    ignoreValueAggregator ^= xbi.bitCount();
                }
                long end = System.currentTimeMillis();
                double avgBitCount = (double) rbg.getBitCount() / testSize;
                double avgTimeNS = (double) (end - start) * 1000 / testSize;
                System.out.printf("Simple Discard Method byte array, avg bits: %f avg time %fμs%n", avgBitCount,
                        avgTimeNS);
            }

            // test simple modular
            {
                countingRNG.resetBitCount();
                long start = System.currentTimeMillis();
                int bits = n.length * Byte.SIZE + SIMPLE_MODULAR_METHOD_ADDITIONAL_BITS;
                for (long i = 0; i < testSize; i++) {
                    BigInteger xxbi = new BigInteger(bits, countingRNG);
                    BigInteger xbi = xxbi.mod(nbi);
                    ignoreValueAggregator ^= xbi.bitCount();
                }
                long end = System.currentTimeMillis();
                double avgBitCount = (double) countingRNG.getBitCount() / testSize;
                double avgTimeNS = (double) (end - start) * 1000 / testSize;
                System.out.printf("Simple Modular Method BigInteger, avg bits: %f avg time %fμs%n", avgBitCount,
                        avgTimeNS);
            }

            // test RNG-BC
            {
                rbg.resetBitCount();
                RandomNumberGenerator rngbc = new RNG_BC(rbg);

                long start = System.currentTimeMillis();
                for (long i = 0; i < testSize; i++) {
                    // let's play fair and create the byte array in the loop
                    byte[] x = new byte[n.length];
                    rngbc.next(n, x);
                    BigInteger xbi = new BigInteger(1, x);
                    ignoreValueAggregator ^= xbi.bitCount();
                }
                long end = System.currentTimeMillis();
                double avgBitCount = (double) rbg.getBitCount() / testSize;
                double avgTimeNS = (double) (end - start) * 1000 / testSize;
                System.out.printf("RNG-BC-1, avg bits: %f avg time %fμs%n", avgBitCount, avgTimeNS);
            }
            // test RNG-BC-8
            {
                rbg.resetBitCount();
                RandomNumberGenerator rngbc = new RNG_BC_8(rbg);
                long start = System.currentTimeMillis();
                for (long i = 0; i < testSize; i++) {
                    // let's play fair and create the byte array in the loop
                    byte[] x = new byte[n.length];
                    rngbc.next(n, x);
                    BigInteger xbi = new BigInteger(1, x);
                    ignoreValueAggregator ^= xbi.bitCount();
                }
                long end = System.currentTimeMillis();
                double avgBitCount = (double) rbg.getBitCount() / testSize;
                double avgTimeNS = (double) (end - start) * 1000 / testSize;
                System.out.printf("RNG-BC-8, avg bits: %f avg time %fμs%n", avgBitCount, avgTimeNS);
            }

            System.out.println("Ignore: " + ignoreValueAggregator);
        }
    }
}
