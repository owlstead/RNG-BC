package nl.maartenbodewes.rng_bc;

/**
 * Implementation of The Simple Discard Method as specified in NIST 90A Revision 1: A.5.1.
 *  
 * @author maartenb
 */
public class RNGSimpleDiscard implements RandomNumberGenerator {

	private final RandomBitGenerator rbg;

	public RNGSimpleDiscard(RandomBitGenerator rbg) {
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

		rbg.nextBytes(c);
		c[0] &= highByteMask;

		int i = 0;
		while (true) {
			int ri = (r[i] & 0xFF);
			int xi = (c[i] & 0xFF);
			
			if (xi < ri) {
				return;
			}
			
			if (xi > ri) {
				rbg.nextBytes(c);
				c[0] &= highByteMask;
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
