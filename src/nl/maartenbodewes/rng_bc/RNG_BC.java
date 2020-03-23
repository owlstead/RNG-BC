package nl.maartenbodewes.rng_bc;

/**
 * An implementation of RNG-BC-1 which uses a random bit generator.
 * 
 * Currently only allows values that are encoded in 8 * x bytes.
 *  
 * @author maartenb
 */

public class RNG_BC implements RandomNumberGenerator {

	private final RandomBitGenerator rbg;

	public RNG_BC(RandomBitGenerator rbg) {
		this.rbg = rbg;
	}

	public void next(byte[] r, byte[] x) {
		if (x.length != r.length) {
			throw new IllegalArgumentException();
		}

		rbg.nextBytes(x, x.length);

		for (int i = 0; i < r.length; i++) {
			int ri = (r[i] & 0xFF);
			int xi = (x[i] & 0xFF);
			
			for (int b = 7; b >= 0; b--) {
				int mask = 1 << b;

				int xb = xi & mask;
				int rb = ri & mask;
				
				int diff = rb - xb;

				if (diff > 0) {
					return;
				}

				if (diff < 0) {
					rbg.nextBytes(x, i);
					for (int bo = 7; bo >= b; bo--) {
						int newBit = rbg.nextBit();
						xi = setBit(xi, bo, newBit);
					}
					x[i] = (byte) xi;
					
					// ugly hack
					i = -1;
					break;
				}
			}
		}
	}

	private static int setBit(int xi, int bo, int newBit) {
		int clearMask = ~(1 << bo);
		int xiWithBitCleared = xi & clearMask;
		int bitMask = newBit << bo;
		int xiWithNewBitSet = xiWithBitCleared | bitMask;
		return xiWithNewBitSet;
	}
}
