            
//            // test RNG-BC-8
//            {
//                myRNG.resetBitCount();
//                RandomBitGenerator rbg = new RandomBitGenerator(myRNG);
//                RandomNumberGenerator rngbc = new RNGBC8(rbg);
//                long start = System.currentTimeMillis();
//                for (long i = 0; i < testSize; i++) {
//                    // let's play fair and create the byte array in the loop
//                    byte[] x = new byte[n.length];
//                    rngbc.next(n, x);
//                    BigInteger xbi = new BigInteger(1, x);
//                    ignoreValueAggregator ^= xbi.bitCount();
//                }
//                long end = System.currentTimeMillis();
//                double avgBitCount = (double) myRNG.getBitCount() / testSize;
//                double avgTimeNS = (double) (end - start) * 1000 / testSize;
//                System.out.printf("RNG-BC-8, avg bits: %f avg time %fμs%n", avgBitCount, avgTimeNS);
//            }