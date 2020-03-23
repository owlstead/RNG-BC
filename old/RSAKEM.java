package nl.maartenbodewes.rng_bc;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;

import hex.Hex;

/**
 * Partial RSA KEM class without actual encryption or key derivation.
 * This class just creates and wraps or unwraps a shared secret of the same size as the modulus.
 * 
 * @author maartenb
 */
public class RSAKEM {

	enum State {
		UNINITIALIZED, INITIALIZED, GENERATED_OR_DECRYPTED;
	}
	
	private boolean forEncryption; 

	private Key key;
	private RandomNumberGenerator rng;
	
	private byte[] sharedSecret;
	
	private State state;

	
	public RSAKEM() {
		state = State.UNINITIALIZED;
	}
	
	public void init(Key key, RandomNumberGenerator rng) {
		if (!(key instanceof RSAPublicKey) && !(key instanceof RSAPrivateKey)) {
			throw new IllegalArgumentException("Expecting RSA public or private key");
		}
		
		if (rng == null) {
		    throw new NullPointerException("rng should not be null");
		}
		
		this.forEncryption = key instanceof PublicKey;
		
		this.key = key;
		this.rng = rng;
		
		this.sharedSecret = null;
		
		this.state = State.INITIALIZED;
	}
	
	public byte[] createSharedSecret() {
	    if (sharedSecret == null) {
	        if (forEncryption) {
	            sharedSecret = generateSharedSecret((RSAPublicKey) key, rng);
	            state = State.GENERATED_OR_DECRYPTED;
	        } else {
	            throw new IllegalStateException("No secret decrypted yet");
	        }
	    }
	    return sharedSecret.clone();
	}

	public byte[] encrypt() throws GeneralSecurityException {
	    if (state == State.UNINITIALIZED) {
	        throw new IllegalStateException("Cipher not initialized");
	    }
	    
	    if (!forEncryption) {
	        throw new IllegalStateException("Invalid state, initialized for encryption");
	    }

	    byte[] sharedSecret = createSharedSecret();
	    RSAPublicKey rsaPubKey = (RSAPublicKey) key;
	    return encryptSharedSecret(rsaPubKey, sharedSecret);
	}
	
	public byte[] decrypt(byte[] encryptedSharedSecret) throws GeneralSecurityException {
        if (state == State.UNINITIALIZED) {
            throw new IllegalStateException("Cipher not initialized");
        }

        if (forEncryption) {
            throw new IllegalStateException("Invalid state, initialized for decryption");
        }
        
        RSAPrivateKey rsaPrivKey = (RSAPrivateKey) key;
        this.sharedSecret = decryptSharedSecret(rsaPrivKey, encryptedSharedSecret);
        state = State.GENERATED_OR_DECRYPTED;
        return this.sharedSecret.clone();
	}
	
	
	private static byte[] generateSharedSecret(RSAPublicKey pubKey, RandomNumberGenerator rng) {
    	BigInteger n = pubKey.getModulus();
        // we need a range of [2, n), so two smaller range [0, n - 2)
    	BigInteger range = n.subtract(BigInteger.TWO);
    	int l = (n.bitLength() + Byte.SIZE - 1) / Byte.SIZE;
    	byte[] beRange = Util.i2osp(range, l);
    	
    	byte[] x = new byte[n.bitLength() / Byte.SIZE];
    	
    	// TODO problem if n - 2 is a smaller size in bits then n (not possible for big random-like values)
    	rng.next(beRange, x);
    	// go from a number in range [0, n - 2) to [2, n)
    	Util.inc2(x);
    	return x;
    }

    static byte[] encryptSharedSecret(RSAPublicKey pubKey, byte[] sharedSecret) throws GeneralSecurityException {
		Cipher rawRSA = Cipher.getInstance("RSA/ECB/NoPadding");
		rawRSA.init(Cipher.ENCRYPT_MODE, pubKey);
		return rawRSA.doFinal(sharedSecret);
	}

	static byte[] decryptSharedSecret(RSAPrivateKey privKey, byte[] encryptedSharedSecret) throws GeneralSecurityException {
		Cipher rawRSA = Cipher.getInstance("RSA/ECB/NoPadding");
		rawRSA.init(Cipher.DECRYPT_MODE, privKey);
		return rawRSA.doFinal(encryptedSharedSecret);
	}

    public static void main(String[] args) throws Exception {
    	
    	// create the key pair
    	int bitSize = 4096;
    
    	KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(bitSize);
    	KeyPair kp = kpg.generateKeyPair();
    	
    	
    	SecureRandom rbg = new SecureRandom();
    	CountingSecureRandom crbg = new CountingSecureRandom(rbg);
    	
    	// just test the creation of the shared secret
    	var pubKey = (RSAPublicKey) kp.getPublic();
    	RNGBC8Fast rngbc = new RNGBC8Fast(crbg);
        byte[] sharedSecret = generateSharedSecret(pubKey, rngbc);
    	byte[] ct = encryptSharedSecret(pubKey, sharedSecret);
    
    	System.out.println(Hex.encode(sharedSecret));
    	System.out.println(crbg.getBitCount());
    	System.out.println(Hex.encode(ct));
    	
    	var privKey = (RSAPrivateKey) kp.getPrivate();
    	byte[] sharedSecret2 = decryptSharedSecret(privKey, ct);
    	
    	System.out.println(Arrays.compare(sharedSecret, sharedSecret2));
    	
    	RSAKEM kemEncryptor = new RSAKEM();
    	kemEncryptor.init(kp.getPublic(), rngbc);
    	byte[] secretSender = kemEncryptor.createSharedSecret();
    	byte[] kemCT = kemEncryptor.encrypt();
    	
    	RSAKEM kemDecryptor = new RSAKEM();
    	kemDecryptor.init(kp.getPrivate(), rngbc);
    	byte[] secretReceiver = kemDecryptor.decrypt(kemCT);
    	
    	if (Arrays.compare(secretSender, secretReceiver) == 0) {
    	    System.out.println("Compare succeeded after RSA KEM");
    	}
    }
}
