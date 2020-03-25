package nl.maartenbodewes.rng_bc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Testing class that tests if the implemented random number generators behave.
 * 
 * @author maartenb
 */
public class FunctionalTests {

    /**
     * No arguments required.
     * 
     * @param args ignored
     * @throws Exception any exception not caught, none expected
     */
    public static void main(String ... args) throws Exception {

        RandomBitGenerator rbg = new RandomBitGenerator(SecureRandom.getInstance("DRBG"));
        RandomNumberGenerator rngBC;
        rngBC = new RNG_BC(rbg);
        test(rngBC);
        rngBC = new RNG_BC_8(rbg);
        test(rngBC);
        rngBC = new RNGSimpleDiscard(rbg);
        test(rngBC);
    }

    /**
     * Tests if all the numbers are generated for value 0x0101, giving enough calls (0x101 ^ 2 calls in total).
     * It also prints the average, which should be centered around 0x80. 
     * 
     * @param bc the random number generator to test
     */
    private static void test(RandomNumberGenerator bc) {
        byte[] r = { 0x01, 0x01 };
        BigInteger ri = new BigInteger(1, r);

        byte[] a = new byte[r.length];
        
        Set<BigInteger> values = new HashSet<>();
        
        long tests = ri.pow(2).longValueExact();
        System.out.println(tests);
        BigInteger total = BigInteger.ZERO;
        long i;
        for (i = 0; i < tests; i++) {
            bc.next(r, a);
            BigInteger abi = new BigInteger(1, a);
            values.add(abi);
            total = total.add(abi);
        }
        BigDecimal totalD = new BigDecimal(total);
        System.out.println(totalD.divide(BigDecimal.valueOf(i), MathContext.DECIMAL32));
        System.out.println(values.size() == ri.intValueExact());
    }

}
