package nl.maartenbodewes.rng_bc;

import java.security.SecureRandom;

public class RandomBitGeneratorLeft {

	private final SecureRandom rng;
	
	private int bitBuffer;
	private int bitsBuffered = 0;
	
	private long bitCount = 0L;

	public RandomBitGeneratorLeft(SecureRandom rng) {
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
			r |= takeFromBuffer(bitsLeft) >>> (amount - bitsLeft);
		}
		
		bitCount += amount;
		return r;
	}
	
	public int nextBit() {
		return nextBits(1) >>> (Integer.SIZE - 1); 
	}
	
	public byte nextByte() {
		return (byte) (nextBits(Byte.SIZE) >>> (Integer.SIZE - Byte.SIZE));
	}


	private void fillBitBuffer() {
		bitBuffer = rng.nextInt();
		bitsBuffered = Integer.SIZE;
	}
	
	private int takeFromBuffer(int bits) {
		int mask = ~((1 << (Integer.SIZE - bits)) - 1);
	    int r = bitBuffer & mask;
	    
	    bitBuffer <<= bits;
	    bitsBuffered -= bits;
		
	    return r; 
	}
	
	public long getBitCount() {
		return bitCount;
	}
	
	@Override
	public String toString() {
		long x = (1L << Integer.SIZE) | bitBuffer &0xFFFFFFFFL;
		String bin = Long.toBinaryString(x).substring(1, 1 + bitsBuffered);
		return String.format("Count: %d, buffer: %s (%d left)", bitCount, bin, bitsBuffered);
	}
	
	
	
	
	public static void main(String[] args) {
		RandomBitGeneratorLeft rbg = new RandomBitGeneratorLeft(new SecureRandom());
		for (int i = 0; i < 33; i++) {
			int bit = rbg.nextBit();
			System.out.println(bit);
			System.out.println(rbg);
		}

	}

}
