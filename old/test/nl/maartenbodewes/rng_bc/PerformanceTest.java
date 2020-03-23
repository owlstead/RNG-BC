package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import hex.Hex;

public class PerformanceTest {
    private static final byte[] MIN_256 = Hex.decode("8000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] MIN_256_PLUS_1 = Hex.decode("8000000000000000000000000000000000000000000000000000000000000001");

    private static final byte[] MID_256_PLUS_ONE = Hex.decode("C000000000000000000000000000000000000000000000000000000000000001");
    private static final byte[] MID_256 = Hex.decode("C000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] MID_256_MINUS_ONE = Hex.decode("BFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

    private static final byte[] MAX_256_MINUS_ONE = Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    
    private static final byte[] BRAINPOOLP512R1_P = Hex.decode(
            "AADD9DB8DBE9C48B3FD4E6AE33C9FC07CB308DB3B3C9D20ED6639CCA703308717D4D9B009BC66842AECDA12AE6A380E62881FF2F2D82C68528AA6056583A48F3");

    private static final byte[] MODULUS_4096 = Hex.decode(
            "99a9312fd775c94d804827b6011c9fcc8180ad142adec24c772c24709db89eb6ff4a7c8c876837920f8c49cf23ff8c9da0d8e7fbb722e0d3e6671d39debd95079d56c57c0d9db9d2cad074bc57e22f8811ba163ecb3342f24ce97df551bd3d9353796efa7f5f9c9ed5ef98a7a0ff30d1e805faa8ea8747a6ae3734cfd8046e56278d36ad2666c612270298fa87356dedfff13eac082ab7d406510d367c6ca034ddb92c297b65a233b74baae3ca2165e3fe699dbcbe82b48831c52222f10fdbd29aca5608616f09ad6fdd2bfe90ecb135f4976e93660714b2fcc0cd0a713723ff835229628480444e0dc75d3fd44aee9465ac719c0a605e4f5917a91e09c0ebe23b08e467766d6329bb83443a17eab39a310aab8b59628f90f4b40b50f2af6ff49c3307441feab0f0978177798123207b21ae8fd7a6cc5d307155843161ee4c54e3685ba402f5a2f155a52c4521b76eca6e2b9d5362341e37b8cb4dcbf7bc9caaffbd31a62ce3240f5161f43598d0f8ae5fe418f4784f8ddd444bd314e73f8c870895bde24388bb905e42ae80de34885c6d64edbab2e852053b737bf85471d5dfed98ba878e806de82aa762791dacef47895d66378b6f94256a0d3e7e77026aab58540246eb9952c8c54777faa879026498c169835df5866fe867734d21e9e921d6a6fcf64260d8b98f195d0585d445d611607008f7d15407fcc32c1a55f1f9a5");

    
    private static final HashMap<String, byte[]> PERFORMANCE_TESTS = new HashMap<>();
    private static final List<String> PERFORMANCE_TESTS_ORDER = new LinkedList<>();
    
    static {
        addPerformanceTest("Minimum 256 bit", MIN_256);
        addPerformanceTest("Minimum 256 bit + 1", MIN_256_PLUS_1);
        addPerformanceTest("Mid 256 bit - 1", MID_256_MINUS_ONE);
        addPerformanceTest("Mid 256", MID_256);
        addPerformanceTest("Mid 256 + 1", MID_256_PLUS_ONE);
        addPerformanceTest("Max 256 - 1", MAX_256_MINUS_ONE);
        addPerformanceTest("Brainpool P512r1", BRAINPOOLP512R1_P);
        addPerformanceTest("Random modulus 4096", MODULUS_4096);
    }
    
    private static void addPerformanceTest(String name, byte[] value) {
        PERFORMANCE_TESTS.put(name, value);
        PERFORMANCE_TESTS_ORDER.add(name);
    }
    
    
    private static class RandomBitCountListener {
        private String valueName;
        int expectedMinimum;
        long minBitCount;
        long maxBitCount;
        long totalBitCount;
        
        public RandomBitCountListener(String valueName) {
            this.valueName = valueName;
            this.expectedMinimum = PERFORMANCE_TESTS.get(valueName).length * Byte.SIZE;
        }
        
        public void registerBitCount(long bitCount) {
            if (bitCount > maxBitCount) {
                this.maxBitCount = bitCount;
            }
            
            if (bitCount < minBitCount) {
                this.minBitCount = bitCount;
                
                if (bitCount < expectedMinimum) {
                    throw new IllegalStateException("Smaller than expected minimum");
                }
            }
            
            totalBitCount += bitCount;
        }
    }
    
    private static class ResultBitCountListener {
        protected int aggregated;
        void registerBitCount(int bitCount) {
            aggregated ^= bitCount;
        }
    }
    
    private static abstract class TestRun {
        private String name;
        protected long randomBitCount;
        protected int resultBitCount;
        
        public TestRun(String name) {
            this.name = name;
        }
        
        abstract void run();
        
        long retrieveRandomBitCount() {
            return randomBitCount;
        }
        
        int retrieveResultBitCount() {
            return resultBitCount;
        }
        
        public String getName() {
            return name;
        }
    }
    
    private static class TestRunner {
        int testCount;
        private String algorithm;
        private RandomNumberGenerator rng;
        private TestRun retriever;
        private RandomBitCountListener randomBitCountListener;
        private ResultBitCountListener resultBitCountListener;
        
        public TestRunner(int testCount, String algorithm, TestRun testRun,
                RandomBitCountListener randomBitCountListener, ResultBitCountListener resultBitCountListener) {
            this.testCount = testCount;
            this.algorithm = algorithm;
            this.retriever = testRun;
            this.randomBitCountListener = randomBitCountListener;
            this.resultBitCountListener = resultBitCountListener;
        }
        
        public void run() {
            for (long i = 0; i < testCount; i++) {
                retriever.run();
                randomBitCountListener.registerBitCount(retriever.retrieveRandomBitCount());
                resultBitCountListener.registerBitCount(retriever.retrieveResultBitCount());
            }
        }
    }
    
    
    public static void main(String[] args) throws Exception {

        int testSize = 1000000;
        CountingSecureRandom myRNG = new CountingSecureRandom(SecureRandom.getInstance("DRBG"));
        
        ResultBitCountListener rbcl = new ResultBitCountListener();
        

        String valueName = "Brainpool P256r1";
        byte[] n = PERFORMANCE_TESTS.get(valueName);
        BigInteger nbi = Util.os2ip(n, n.length);
        
        // === actual tests

        TestRun run;
        
        run = new TestRun("Simple Discard (Java)") {
            public void run() {
                BigInteger xbi;
                do {
                    xbi = new BigInteger(n.length * Byte.SIZE, myRNG);
                } while (xbi.compareTo(nbi) > 0);

                this.randomBitCount = myRNG.getBitCount();
                this.resultBitCount = xbi.bitCount();
            }
        };
        performTest(testSize, myRNG, rbcl, valueName, run);
        

        RandomBitGenerator rbg = new RandomBitGenerator(myRNG);
        RandomNumberGenerator rngbc = new RNGSimpleDiscard(rbg);
        byte[] x = new byte[n.length];
        run = new TestRun("Simple Discard (byte array)") {
            public void run() {
                rngbc.next(n, x);
                BigInteger xbi = new BigInteger(1, x);
                this.randomBitCount = myRNG.getBitCount();
                this.resultBitCount ^= xbi.bitCount();
            }
        };
        performTest(testSize, myRNG, rbcl, valueName, run);

        run = new TestRun("Simple Discard (byte array)") {
            public void run() {
        myRNG.resetBitCount();
        long start = System.currentTimeMillis();
        int bits = n.length * Byte.SIZE + s;
        for (long i = 0; i < testSize; i++) {
            BigInteger xxbi = new BigInteger(bits, myRNG);
            BigInteger xbi = xxbi.mod(nbi);
            ignoreValueAggregator ^= xbi.bitCount();
        }
        long end = System.currentTimeMillis();
        double avgBitCount = (double) myRNG.getBitCount() / testSize;
        double avgTimeNS = (double) (end - start) * 1000 / testSize;
        
        // === bad
        
        System.out.printf("Ignore: %d", rbcl.aggregated);
    }


    private static void performTest(int testSize, CountingSecureRandom myRNG, ResultBitCountListener rbcl,
            String valueName, TestRun run) {
        RandomBitCountListener randomBitCountListener = new RandomBitCountListener(valueName);
        TestRunner runner = new TestRunner(testSize, run.getName(), run, randomBitCountListener, rbcl);

        myRNG.resetBitCount();

        long start = System.currentTimeMillis();
        runner.run();
        long end = System.currentTimeMillis();
        
        double avgBitCount = (double) randomBitCountListener.totalBitCount / testSize;
        double avgTimeNS = (double) (end - start) * 1000 / testSize;
        System.out.printf("%s using %s, avg bits: %f avg time %fμs%n", valueName, runner.algorithm, avgBitCount, avgTimeNS);
    }
}
