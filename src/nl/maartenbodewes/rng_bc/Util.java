package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;

public final class Util {
    public static BigInteger os2ip(final byte[] data, final int size) {
        if (data.length != size) {
            throw new IllegalArgumentException("Size of the octet string should be precisely " + size);
        }

        return new BigInteger(1, data);
    }
}
