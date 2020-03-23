package nl.maartenbodewes.rng_bc;

/**
 * An implementation of RNG-BC-1 that implements the Optimized Simple Discard Method over bits.
 * This uses a simplified loop and byte subtraction to find the first bit that is too high,
 * simplifying the code and making it more efficient to boot.
 * 
 * Unfortunately it fails a the moment.
 * 
 * @author maartenb
 */
public class RNG_BC_ByteSubtract implements RandomNumberGenerator {

    private final RandomBitGenerator rbg;

    public RNG_BC_ByteSubtract(RandomBitGenerator rbg) {
        this.rbg = rbg;
    }

    public void next(byte[] r, byte[] c) {
        if (c.length != r.length) {
            throw new IllegalArgumentException();
        }

        int highestOneBit = Integer.highestOneBit(Byte.toUnsignedInt(r[0]));
        if (highestOneBit == 0) {
            // we do not allow values that start with the most significant byte set to zero bits
            throw new IllegalArgumentException();
        }

        int highByteMask = (highestOneBit << 1) - 1;

        // initially randomize all the bits
        rbg.nextBytes(c, c.length);
        c[0] &= highByteMask;

        // loop over all the bytes
        int i = 0;
        while (true) {
            // use positive integers for each byte
            int ri = (r[i] & 0xFF);
            int ci = (c[i] & 0xFF);

            // subtract the bits
            int diff = ci - ri;

            // if c is lower the candidate is valid
            if (diff < 0) {
                return;
            }

            if (diff == 0) {
                if (i < r.length) {
                    i++;
                } else {
                    // all bits and bytes are equal so we need to start over
                    rbg.nextBytes(c);
                    c[0] &= highByteMask;
                    i = 0;
                }
                continue;
            }
                
            // efficiently generate the more significant bytes
            if (i != 0) {
                rbg.nextBytes(c, i);
            }

            // and then the bits
            int uncomparedBits = Integer.SIZE - (Integer.numberOfLeadingZeros(ri ^ ci) + 1);
            for (int bo = 7; bo >= uncomparedBits; bo--) {
                int newBit = rbg.nextBit();
                ci = setBit(ci, bo, newBit);
            }
            c[i] = (byte) ci;
            c[0] &= highByteMask;
            i = 0;
        }
    }

    /**
     * Set a bit in input value <code>xi</code> at bit offset <code>bo</code> with value <code>newBit</code>.
     * 
     * @param ci     the input value to change
     * @param bo     the offset in the value of the bit to change
     * @param newBit the bit value (0 or 1)
     * @return the value xi with one bit set to the specified value
     */
    private static int setBit(int ci, int bo, int newBit) {
        int clearMask = ~(1 << bo);
        int ciWithBitCleared = ci & clearMask;
        int bitMask = newBit << bo;
        int xiWithNewBitSet = ciWithBitCleared | bitMask;
        return xiWithNewBitSet;
    }
    
    public static void main(String[] args) {
        for (int ri = 0; ri <  256; ri++) {
            for (int ci = 0; ci < 256; ci++) {
                
                int resS;
                
                int diffS = ci - ri;
                if (diffS < 0) {
                    resS = 10;
                } else if (diffS == 0) {
                    resS = 9;
                } else {
                    // NOTE this works, subtraction doesn't
                    resS = Integer.SIZE - (Integer.numberOfLeadingZeros(ri ^ ci) + 1);
                }
            
                int res = 9;
                for (int b = Byte.SIZE - 1; b >= 0; b--) {
                    // mask out the bit to compare
                    int mask = 1 << b;

                    int rb = ri & mask;
                    int cb = ci & mask;

                    // subtract the bits
                    int diff = cb - rb;

                    // if c is lower the candidate is valid
                    if (diff < 0) {
                        res = 10;
                        break;
                    }
                    
                    if (diff > 0) {                        
                        res = b;
                        break;
                    }

                    // c is identical, so we proceed to compare the bits in the next byte
                }
                
                if (resS != res) {
                    System.out.printf("%s <> %s : %d & %d%n", Integer.toBinaryString(ri), Integer.toBinaryString(ci), resS, res);
                }
            }
        }
    }
}
