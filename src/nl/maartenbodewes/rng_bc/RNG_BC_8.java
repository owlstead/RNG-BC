package nl.maartenbodewes.rng_bc;

/**
 * An implementation of RNG-BC-8 that implements the Optimized Simple Discard Method over bytes.
 * 
 * @author maartenb
 */
public class RNG_BC_8 implements RandomNumberGenerator {

	private final RandomBitGenerator rbg;
	    
	public RNG_BC_8(RandomBitGenerator rbg) {
		this.rbg = rbg;
	}

	@Override
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
		rbg.nextBytes(c);
        c[0] &= highByteMask;

        // loop over all the bytes
        int i = 0;
        while (true) {
			int ri = (r[i] & 0xFF);
			int ci = (c[i] & 0xFF);

            // if c is lower the candidate is valid
			if (ci < ri) {
				return;
			}

            // if c is higher then only regenerate the compared bytes
			if (ci > ri) {
				rbg.nextBytes(c, i + 1);
                c[0] &= highByteMask;
                
                // continue the loop to restart
				i = 0;
				continue;
			}
			
            // all bits and bytes are equal so we need to start over
            if (i == r.length - 1) {
                rbg.nextBytes(c);
                c[0] &= highByteMask;
                i = 0;
                continue;
            }
            
			i++;
		}
	}
}
