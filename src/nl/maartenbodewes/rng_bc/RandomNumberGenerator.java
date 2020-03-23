package nl.maartenbodewes.rng_bc;

/**
 * is an interface for a random number generator that generates random values in a range and encodes them as
 * unsigned big endian values.
 * 
 * This random number generator acts on bytes rather than on <code>BigInteger</code>. 
 * 
 * @author maartenb
 *
 */
public interface RandomNumberGenerator {

	/**
	 * Generate the next random number in the range [0, R).
	 * @param r the maximum value (exclusive) unsigned big integer of R
	 * @param x a buffer to store the generated random number value x
	 */
	void next(byte[] r, byte[] x);
}