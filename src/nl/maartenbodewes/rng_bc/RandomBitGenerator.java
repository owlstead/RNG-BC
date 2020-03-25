package nl.maartenbodewes.rng_bc;

import java.security.SecureRandom;

/**
 * A random bit generator (RBG) that allows the user to retrieve bits rather than bytes in a
 * relatively efficient manner (by keeping a cache).
 *  
 * @author maartenb
 */
final class RandomBitGenerator implements BitCounter {

	private final SecureRandom rng;
	
	private int bitBuffer;
	private int bitsBuffered = 0;
	
	private long bitCount = 0L;

	/**
	 * 
	 * @param rng
	 */
	public RandomBitGenerator(SecureRandom rng) {
		this.rng = rng;
	}
	
	/**
	 * Retrieves as many bits as requested, putting the bits into the least significant bits of the return value.
	 * 
	 * @param amount the amount of bits to retrieve
	 * @return the random bits in the least significant bits; the other bits will be set to zero 
	 */
	public int nextBits(int amount) {
		// fill return value with as many bits as possible (possibly 0 bits)
		int bitsToRetrieve = Math.min(bitsBuffered, amount);
		int r = takeFromBuffer(bitsToRetrieve);
		
		// check if all required bits are retrieved
		int bitsLeft = amount - bitsToRetrieve;
		if (bitsLeft != 0) {
			// otherwise make amends
			fillBitBuffer();
			r = (r << bitsLeft) | takeFromBuffer(bitsLeft);
		}
		
		bitCount += amount;
		return r;
	}
	
	/**
	 * Returns the next random boolean by retrieving a bit and then turning it into a boolean.
	 * 
	 * @return the random boolean value
	 */
	public boolean nextBoolean() {
		return nextBits(1) == 1;
	}
	
	/**
	 * Returns the next random bit as integer with value 0 or 1.
	 * 
	 * @return the random bit
	 */
	public int nextBit() {
		return nextBits(1); 
	}
	
	/**
	 * Returns the next random byte in the range 0x80 (-128) to 0x7F (127) inclusive.
	 * 
	 * If more bytes are required then calling one of the <code>nextBytes</code> is likely more efficient.
	 * 
	 * @return the random byte
	 */
	public byte nextByte() {
	    byte[] ba = new byte[1];
	    rng.nextBytes(ba);
	    bitCount += Byte.SIZE;
		return ba[0];
	}
	
	/**
	 * Fills the given byte array with random bytes.
	 * 
	 * This method may be more efficient than calling {@link RandomBitGenerator#nextBytes(byte[], int)}.
	 * 
	 * @param x the byte array to be filled with random values
	 */
	public void nextBytes(byte[] x) {
	    rng.nextBytes(x);
	    bitCount += x.length * Byte.SIZE;
	}

	/**
     * Fills the given byte array with the given amount of random bytes, starting at the left.
     * 
	 * @param x the byte array to partially fill with random values
	 * @param amount the amount of random bytes 
	 * @throws {@link IndexOutOfBoundsException} if the amount is larger than the byte array
	 */
	public void nextBytes(byte[] x, int amount) {
	    byte[] bytes = new byte[amount];
	    rng.nextBytes(bytes);
	    System.arraycopy(bytes, 0, x, 0, amount);
	    bitCount += amount * Byte.SIZE;
	}
	
	/*
	 * Fills the bitBuffer and adjusts the bitsBuffered field. 
	 */
	private void fillBitBuffer() {
		bitBuffer = rng.nextInt();
		bitsBuffered = Integer.SIZE;
	}

    /*
     * Takes a number of bits from the number, simply assuming that those bits are available.  
     */
	private int takeFromBuffer(int bits) {
	    if (bits == 0) {
	        return 0;
	    }
	    
		if (bits == Integer.SIZE) {
			return bitBuffer;
		}
		
		int mask = (1 << bits) - 1;
	    int r = bitBuffer & mask;
	    
	    bitBuffer >>>= bits;
	    bitsBuffered -= bits;
		
	    return r; 
	}
	
	@Override
	public long getBitCount() {
		return bitCount;
	}
	
	@Override
	public void resetBitCount() {
		bitCount = 0L;
	}
	
	@Override
	public String toString() {
		long x = (1L << Integer.SIZE) | bitBuffer &0xFFFFFFFFL;
		String bin = Long.toBinaryString(x).substring(1 + Integer.SIZE - bitsBuffered, 1 + Integer.SIZE);
		return String.format("Count: %d, buffer: %s (%d left)", bitCount, bin, bitsBuffered);
	}
	
	
	public static void main(String[] args) {
		RandomBitGenerator rbg = new RandomBitGenerator(new SecureRandom());
		for (int i = 0; i < 33; i++) {
			int bit = rbg.nextBit();
			System.out.println(bit);
			System.out.println(rbg);
		}
	}
}
