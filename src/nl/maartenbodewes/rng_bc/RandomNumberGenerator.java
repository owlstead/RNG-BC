package nl.maartenbodewes.rng_bc;

/**
 * <code>RandomNumberGenerator</code> generates random values in a range [0, r).
 * All values are should be interpreted as unsigned big endian values encoded as bytes.
 * 
 * This random number generator acts on bytes rather than on <code>BigInteger</code>. 
 * 
 * @author maartenb
 *
 */
public interface RandomNumberGenerator {

	/**
	 * Generate the next random number in the range [0, r).
	 * The r value should not have a most significant zero valued byte. 
	 * @param r the maximum value of the range in unsigned big endian encoding
	 * @param c a buffer to store the generated random number value 
	 */
	void next(byte[] r, byte[] c);
}
