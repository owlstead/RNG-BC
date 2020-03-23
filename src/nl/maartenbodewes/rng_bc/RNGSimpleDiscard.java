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

	public void next(byte[] r, byte[] x) {
		if (x.length != r.length) {
			throw new IllegalArgumentException();
		}

		// TODO use this code to make the random number generators operate on numbers that are using 8 * x bits
//		int highestOneBit = Integer.highestOneBit(Byte.toUnsignedInt(r[0]));
//		if (highestOneBit == 0) {
//			throw new IllegalArgumentException();
//		}
//		
//		int highByteMask = (highestOneBit << 1) - 1;

		rbg.nextBytes(x, x.length);
//		x[0] &= highByteMask;

		for (int i = 0; i < r.length; i++) {
			int ri = (r[i] & 0xFF);
			int xi = (x[i] & 0xFF);
			
			if (xi < ri) {
				return;
			}
			
			if (xi > ri) {
				rbg.nextBytes(x, x.length);
//				x[0] &= highByteMask;

				// ugly hack
				i = -1;
			}
		}
	}
}
