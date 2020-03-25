package nl.maartenbodewes.rng_bc;

/**
 * An implementation of RNG-BC-1 that implements the Optimized Simple Discard Method over bits.
 * 
 * @author maartenb
 */
public class RNG_BC implements RandomNumberGenerator {

    private final RandomBitGenerator rbg;

    public RNG_BC(RandomBitGenerator rbg) {
        this.rbg = rbg;
    }

    /**
     *
     * @param r r is the maximum within the range [0, r), this array remains unchanged
     * @param c c is the selected candidate random number that is in the range [0, r), this array is overwritten
     */
    public void next(byte[] r, byte[] c) {
        // this code uses variable names taken from NIST SP 800-90A Rev. 1
        
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
        OUTER: while (true) {
            // use positive integers for each byte
            int ri = (r[i] & 0xFF);
            int ci = (c[i] & 0xFF);

            for (int b = Byte.SIZE - 1; b >= 0; b--) {
                // mask out the bit to compare
                int mask = 1 << b;

                int rb = ri & mask;
                int cb = ci & mask;

                // subtract the bits
                int diff = cb - rb;

                // if c is lower the candidate is valid
                if (diff < 0) {
                    return;
                }

                // if c is higher then only regenerate the compared bits
                if (diff > 0) {
                    // efficiently generate the more significant bytes
                    if (i != 0) {
                        rbg.nextBytes(c, i);
                    }
                        
                    // and then the bits
                    for (int bo = 7; bo >= b; bo--) {
                        int newBit = rbg.nextBit();
                        ci = setBit(ci, bo, newBit);
                    }
                    c[i] = (byte) ci;
                    
                    c[0] &= highByteMask;
                    
                    // continue the outer loop to restart
                    i = 0;
                    continue OUTER;
                }

                // c is identical, so we proceed to compare the bits in the next byte
            }
            
            // all bits and bytes are equal so we need to start over
            if (i == r.length) {
                rbg.nextBytes(c);
                c[0] &= highByteMask;
                i = 0;
                continue;
            }

            // look at next byte, previous bytes in c are equal to those in r
            i++;
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
}
