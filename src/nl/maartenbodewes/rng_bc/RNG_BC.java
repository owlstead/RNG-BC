package nl.maartenbodewes.rng_bc;

/**
 * An implementation of RNG-BC-1 that implements the Optimized Simple Discard Method over bits.
 * This uses a simplified loop and XOR to find the first bit that is too high,
 * simplifying the code and making it more efficient to boot.
 * 
 * @author maartenb
 */
public class RNG_BC implements RandomNumberGenerator {

    private final RandomBitGenerator rbg;

    public RNG_BC(RandomBitGenerator rbg) {
        this.rbg = rbg;
    }

    public void next(byte[] r, byte[] c) {
        if (c.length != r.length) {
            throw new IllegalArgumentException();
        }

        int highestOneBit = Integer.highestOneBit(r[0] & 0xFF);
        if (highestOneBit == 0) {
            // we do not allow values that start with the most significant byte set to zero bits
            throw new IllegalArgumentException();
        }

        int highByteMask = (highestOneBit << 1) - 1;

        // initially randomize all the bits
        rbg.nextBytes(c);
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
                if (i < r.length - 1) {
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

            // and then the bits (using XOR to find the first bit that differs) 
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
}
