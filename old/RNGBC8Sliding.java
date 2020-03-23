package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * A fast implementation of RNGBC8 which directly uses {@link SecureRandom} rather than
 * {@link RandomNumberGenerator} to achieve a speedup.
 *  
 * @author maartenb
 */
public class RNGBC8Sliding implements RandomNumberGenerator {

	private final SecureRandom rbg;
	    
	public RNGBC8Sliding(SecureRandom rbg) {
		this.rbg = rbg;
	}

	@Override
	public void next(byte[] r, byte[] x) {
		if (x.length != r.length) {
			throw new IllegalArgumentException();
		}

		byte[] y = new byte[4 + x.length];
		
		rbg.nextBytes(y);

		int i = 0;
		int ro = 0;
		while (i < y.length) {
			int ri = (r[i] & 0xFF);
			int yi = (y[i] & 0xFF);
			int diff = yi - ri;

			if (diff < 0) {
				return;
			}

			if (diff == 0) {
				i++;
				continue;
			}

			if (diff > 0) {
				rbg.nextBytes(buf[i]);
				System.arraycopy(buf[i], 0, x, 0, i + 1);
				i = 0;
			}
		}
	}
	
	public static void main(String[] args) {
        BigInteger b = new 
    }
}
