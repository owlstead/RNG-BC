package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;

final class Util {
    public static BigInteger os2ip(final byte[] data, final int size) {
        if (data.length != size) {
            throw new IllegalArgumentException("Size of the octet string should be precisely " + size);
        }

        return new BigInteger(1, data);
    }
    
    public static byte[] fromHex(String hexadecimals) {
        byte[] data = new byte[hexadecimals.length() / 2];
        for (int i = 0; i < data.length; i++) {
            data[i] = Integer.valueOf(hexadecimals.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return data;
    }
    
    public static String toHex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            hex.append(String.format("%02X", data[i] & 0xFF));
        }
        return hex.toString();
    }
}
