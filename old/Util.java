package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;

public final class Util {
	public static byte[] i2osp(final BigInteger i, final int size) {
	    if (i == null || i.signum() == -1) {
	        throw new IllegalArgumentException("Integer should be a positive number or 0");
	    }

	    if (size < 1) {
	        throw new IllegalArgumentException("Size of the octet string should be at least 1 but is " + size);
	    }

	    final byte[] signed = i.toByteArray();
	    if (signed.length == size) {
	        return signed;
	    }

	    final byte[] os = new byte[size];
	    if (signed.length < size) {
	        // copy to rightmost part of os variable (os initialized to 0x00 bytes)
	        System.arraycopy(signed, 0, os, size - signed.length, signed.length);
	        return os;
	    }

	    if (signed.length == size + 1 && signed[0] == 0x00) {
	        // copy to os variable, skipping initial sign byte
	        System.arraycopy(signed, 1, os, 0, size);
	        return os;
	    }

	    throw new IllegalArgumentException("Integer does not fit into an array of size " + size);
	}
	
	public static BigInteger os2ip(byte[] os) {
		return new BigInteger(1, os);
	}
	
	public static BigInteger os2ip(final byte[] data, final int size) {
	    if (data.length != size) {
	        throw new IllegalArgumentException("Size of the octet string should be precisely " + size);
	    }

	    return new BigInteger(1, data); 
	}

	public static final boolean dec(byte[] bigEndianEncodedNumber) {
		for (int i = bigEndianEncodedNumber.length - 1; i >= 0; i--) {
			if (--bigEndianEncodedNumber[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public static final boolean inc(byte[] bigEndianEncodedNumber) {
		for (int i = bigEndianEncodedNumber.length - 1; i >= 0; i--) {
			if ((++bigEndianEncodedNumber[i] & 0xFF) != 0xFF) {
				return false;
			}
		}
		return true;
	}

	public static final boolean inc2(byte[] bigEndianEncodedNumber) {
	    int lsb = bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] & 0xFF;
		bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] += 2;
		if ((bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] & 0xFF) < lsb) {
	        for (int i = bigEndianEncodedNumber.length - 2; i >= 0; i--) {
	            if (++bigEndianEncodedNumber[i] != 0) {
	                return false;
	            }
	        }
	        return true;
		}
		return false;
	}
	
    public static final boolean dec2(byte[] bigEndianEncodedNumber) {
        int lsb = bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] & 0xFF;
        bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] -= 2;
        if ((bigEndianEncodedNumber[bigEndianEncodedNumber.length - 1] & 0xFF) > lsb) {
            for (int i = bigEndianEncodedNumber.length - 2; i >= 0; i--) {
                if ((--bigEndianEncodedNumber[i] & 0xFF) != 0xFF) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
