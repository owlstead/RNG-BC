package nl.maartenbodewes.rng_bc;

import java.security.SecureRandom;

public class BitCalculator {

    public static void main(String ... args) throws Exception {
//        byte[] rs = { (byte) 0b1000_0000, (byte) 0b0000_0001 };
        byte[] rs = { (byte) 0b1111_0001, (byte) 0b1111_1110 };
        
        // just to verify that calculations are OK
        System.out.println(chanceOfSuccessBackwards(rs));
        System.out.println(chanceOfFailureBackwards(rs));
        
        // the actual calculations
        System.out.println(chanceOfFailure(rs));
        System.out.println(averageAdditionalBitsPerRun(rs));
        
        // if failure we both add bits *and* we continue trying, every time with the same chance
        int significanceParameter = 128;
        double totalAdditionalBits = averageAdditionalBits(rs, significanceParameter);
        System.out.println(totalAdditionalBits);
        
        // perform the calculations
        int tests = 1_000_000;
        RandomBitGenerator rbg = new RandomBitGenerator(SecureRandom.getInstance("DRBG"));
        RNG_BC bc = new RNG_BC(rbg);
        byte[] c = new byte[rs.length];
        for (int i = 0; i < tests; i++) {
            bc.next(rs, c);
        }
        // so this should be very near totalAditionalBits
        double actualAdditionalBits = (double) rbg.getBitCount() / tests - rs.length * Byte.SIZE;
        System.out.println(actualAdditionalBits);
        // absolute difference
        System.out.println(Math.abs(totalAdditionalBits - actualAdditionalBits));
        
        
        // test maximum likely addition
        System.out.println(chanceOfMaximum(rs, 8));
    }

    private static double averageAdditionalBits(byte[] rs, int significanceParameter) {
        double insignificant = 1D / Math.pow(2, significanceParameter);
        double failure = chanceOfFailure(rs);
        double additionalBits;
        double totalFailure = 1;
        double totalAdditionalBits = 0;
        do {
            additionalBits = averageAdditionalBitsPerRun(rs) * totalFailure;
            totalAdditionalBits += additionalBits;
            totalFailure *= failure;
        } while (additionalBits > insignificant);
        return totalAdditionalBits;
    }

    static double chanceOfSuccessBackwards(byte[] r) {
        
        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        double success = 1;
        for (int i = r.length - 1; i >= 0; i--) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = 0; j < bits; j++) {
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    success = 0.5D * success;
                } else {
                    success = 0.5D + 0.5D * success;
                }
            }
        }
        return success;
    }

    static double chanceOfFailureBackwards(byte[] r) {
        
        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        double failure = 0;
        for (int i = r.length - 1; i >= 0; i--) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = 0; j < bits; j++) {
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    failure = 0.5D + 0.5D * failure;
                } else {
                    failure = 0.5D * failure;
                }
            }
        }
        return failure;
    }
    
    static double chanceOfFailure(byte[] r) {
        
        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        double continueue = 1;
        double totalChanceOfFailure = 0;
        for (int i = 0; i <  r.length; i++) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = bits - 1; j >= 0; j--) {
                continueue *= 0.5;
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    totalChanceOfFailure += continueue;
                }
            }
        }
        return totalChanceOfFailure;
    }
    
    static double maximumLikelyAddition(byte[] r, int significanceParameter) {
        double insignificant = 1D / Math.pow(2, significanceParameter);

        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        int pos = 0;
        int lastPos = 0;
        double continueue = 1;
        for (int i = 0; i <  r.length; i++) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = bits - 1; j >= 0; j--) {
                pos++;
                continueue *= 0.5;
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    if (continueue < insignificant) {
                        return lastPos;
                    }
                    lastPos = pos;
                }
            }
        }
        return lastPos;
    }
    
    static double chanceOfMaximum(byte[] r, int amount) {
        // this one is rather stupid, as the chance is exactly 0.5 times smaller for each bit
        // the chance of continuing is 0.5 no matter if the bit is 0 or 1 
        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        int pos = 0;
        double continueue = 1;
        for (int i = 0; i <  r.length; i++) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = bits - 1; j >= 0; j--) {
                pos++;
                continueue *= 0.5;
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    if (pos >= amount) {
                        return continueue;
                    }
                }
            }
        }
        return 0;
    }
    
    
    
    
    static double averageAdditionalBitsPerRun(byte[] r) {
        
        int initialSignificantBits = Integer.SIZE - Integer.numberOfLeadingZeros(r[0] & 0xFF);
        
        double additionalBits = 0;
        int pos = 0;
        double continueue = 1;
        for (int i = 0; i <  r.length; i++) {
            int bite = r[i] & 0xFF;
            int bits;
            if (i == 0) {
                bits = initialSignificantBits;
            } else {
                bits = Byte.SIZE;
            }
            
            for (int j = bits - 1; j >= 0; j--) {
                pos += 1;
                continueue *= 0.5;
                int bit = (bite >> j) & 1;
                if (bit == 0) {
                    additionalBits += continueue * pos;  
                }
            }
        }
        return additionalBits;
    }
    
    
//    101
//    
//  bit 1
//  0.5 ok, 0.5 on
//  bit 0
//          0.5 on, 0.5 fail
//  bit 1
//  0.5 ok, 0.5 on
//
//  1.0 ok    
}
