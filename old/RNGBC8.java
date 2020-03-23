package nl.maartenbodewes.rng_bc;

public class RNGBC8 implements RandomNumberGenerator {

	private final RandomBitGenerator rbg;

	public RNGBC8(RandomBitGenerator rbg) {
		this.rbg = rbg;
	}

	public void next(byte[] r, byte[] x) {
		if (x.length != r.length) {
			throw new IllegalArgumentException();
		}

		int highestOneBit = Integer.highestOneBit(Byte.toUnsignedInt(r[0]));
		if (highestOneBit == 0) {
			throw new IllegalArgumentException();
		}
		
		byte highByteMask = (byte) ((highestOneBit << 1) - 1);
		
		
		rbg.nextBytes(x, x.length);
		x[0] &= highByteMask; 

		int i = 0;
		while (i < r.length) {
			int ri = (r[i] & 0xFF);
			int xi = (x[i] & 0xFF);
			int diff = ri - xi;

			if (diff > 0) {
				return;
			}

			if (diff == 0) {
				i++;
				continue;
			}

			if (diff < 0) {
				rbg.nextBytes(x, i + 1);
				x[0] &= highByteMask; 
				i = 0;
			}
		}
	}
}
