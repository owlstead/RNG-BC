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

	public RandomBitGenerator(SecureRandom rng) {
		this.rng = rng;
	}
	
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
	
	public boolean nextBoolean() {
		return nextBits(1) == 1;
	}
	
	public int nextBit() {
		return nextBits(1); 
	}
	
	public byte nextByte() {
	    byte[] ba = new byte[1];
	    rng.nextBytes(ba);
	    bitCount += Byte.SIZE;
		return ba[0];
	}
	
	public void nextBytes(byte[] x) {
	    rng.nextBytes(x);
	    bitCount += x.length * Byte.SIZE;
	}

	public void nextBytes(byte[] x, int amount) {
	    byte[] bytes = new byte[amount];
	    rng.nextBytes(bytes);
	    System.arraycopy(bytes, 0, x, 0, amount);
	    bitCount += amount * Byte.SIZE;
	}
	
	private void fillBitBuffer() {
		bitBuffer = rng.nextInt();
		bitsBuffered = Integer.SIZE;
	}

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
	
	public long getBitCount() {
		return bitCount;
	}
	
	
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
