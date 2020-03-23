package nl.maartenbodewes.rng_bc;

import java.security.SecureRandom;

/**
 * A fast implementation of RNG-BC-8 which directly uses {@link SecureRandom} rather than
 * {@link RandomNumberGenerator} to achieve a speedup.
 * 
 * Currently only allows values that are encoded in 8 * x bytes.
 *  
 * @author maartenb
 */
public class RNG_BC_8 implements RandomNumberGenerator {

	private final SecureRandom rbg;
    private final byte[][] buf = new byte[128 / 8][];
	    
    {
        // initialize an array of correctly sized buffers
    	for (int i = 0; i < buf.length; i++) {
    		buf[i] = new byte[i + 1];
    	}
    }
    
    
	public RNG_BC_8(SecureRandom rbg) {
		this.rbg = rbg;
	}

	@Override
	public void next(byte[] r, byte[] x) {
		if (x.length != r.length) {
			throw new IllegalArgumentException();
		}

		rbg.nextBytes(x);

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
				rbg.nextBytes(buf[i]);
				System.arraycopy(buf[i], 0, x, 0, i + 1);
				i = 0;
			}
		}
	}
}
