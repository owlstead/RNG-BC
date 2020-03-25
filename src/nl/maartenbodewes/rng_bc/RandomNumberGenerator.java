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
	 * Generate the next random number in the range [0, r) and returns it in parameter c.
	 * 
	 * Both r and c are interpreted as being unsigned big endian number values;
	 * to convert to BigInteger, use <code>new BigInteger(1, c)</code>.
	 * 
	 * Note that <code>r</code> should not start with a most significant zero valued byte at index 0.
	 * The parameter <code>c</code> may of course start with any number of zero bytes.
	 * 
	 * Value <code>r</code> remains unchanged; the contents of <code>c</code> will be overwritten.  
     *
     * @param r the maximum of the range [0, r)
     * @param c c is the selected candidate random number that is in the range [0, r)
     */
	void next(byte[] r, byte[] c);
}
