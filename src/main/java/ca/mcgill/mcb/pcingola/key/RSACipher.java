package ca.mcgill.mcb.pcingola.key;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Encryption / decryption using RSA
 *
 * Reference: http://www.reindel.com/asymmetric-public-key-encryption-using-rsa-java-openssl/
 *
 * @author pcingola
 */
public class RSACipher {
	public static final String INSTANCE_NAME = "RSA";
	public static final int DEFAULT_KEY_LEN = 4 * 1024;

	public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	int keyLength = DEFAULT_KEY_LEN;
	String privateKeyPath;
	String publicKeyPath;
	String transformation = "RSA/ECB/PKCS1Padding";
	String encoding = "UTF-8";
	private PrivateKey privateKey;

	private PublicKey publicKey;

	public static void main(String[] args) {

		String pubKey = Gpr.HOME + "/zzz/key_public.key";
		String privKey = Gpr.HOME + "/zzz/key_private.key";

		// Create keys
		RSACipher rsa = new RSACipher(DEFAULT_KEY_LEN);
		rsa.createKeys();
		Gpr.debug(rsa);
		rsa.saveKeys(privKey, pubKey);
		rsa.testCrypt();
		rsa.testSign();

		// Load keys from file
		System.out.println("\n\n\n\n");
		rsa = new RSACipher(privKey, pubKey);
		rsa.loadKeys();
		Gpr.debug(rsa);
		rsa.testCrypt();
		rsa.testSign();

	}

	public RSACipher(int keyLength) {
		this.keyLength = keyLength;
	}

	public RSACipher(String publicKeyPath) {
		privateKeyPath = null;
		this.publicKeyPath = publicKeyPath;
	}

	public RSACipher(String privateKeyPath, String publicKeyPath) {
		this.privateKeyPath = privateKeyPath;
		this.publicKeyPath = publicKeyPath;
	}

	public void createKeys() {
		System.out.println("Creating Public/Private key pairs, length: " + keyLength);
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance(INSTANCE_NAME);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		keyPairGenerator.initialize(keyLength);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		privateKey = keyPair.getPrivate();
		publicKey = keyPair.getPublic();
	}

	/**
	 * Decrypt text
	 */
	public String decrypt(String cipherText) {
		try {
			PrivateKey privateKey = getPrivateKey();
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return new String(cipher.doFinal(Base64.decodeBase64(cipherText)), encoding);
		} catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new RuntimeException("Error when decrypting text with public key '" + privateKeyPath + "'", e);
		}
	}

	/**
	 * Encrypt text
	 */
	public String encrypt(String rawText) {
		try {
			PublicKey publicKey = getPublicKey();
			Cipher cipher = Cipher.getInstance(transformation);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return Base64.encodeBase64String(cipher.doFinal(rawText.getBytes(encoding)));
		} catch (Exception e) {
			throw new RuntimeException("Error when decrypting text with public key '" + publicKeyPath + "'", e);
		}
	}

	public final PrivateKey getPrivateKey() {
		return privateKey;
	}

	public final PublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Load keys
	 */
	public void loadKeys() {
		if (privateKeyPath != null) privateKey = loadPrivateKey(privateKeyPath);
		if (publicKeyPath != null) publicKey = loadPublicKey(publicKeyPath);
	}

	/**
	 * Read private key from file
	 */
	PrivateKey loadPrivateKey(String fileName) {
		try {
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(IOUtils.toByteArray(new FileInputStream(privateKeyPath)));
			return KeyFactory.getInstance(INSTANCE_NAME).generatePrivate(pkcs8EncodedKeySpec);
		} catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException("Error reading private key from file '" + fileName + "'", e);
		}
	}

	/**
	 * Read public key from a file
	 */
	PublicKey loadPublicKey(String fileName) {
		try {
			X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(IOUtils.toByteArray(new FileInputStream(fileName)));
			return KeyFactory.getInstance(INSTANCE_NAME).generatePublic(x509EncodedKeySpec);
		} catch (Exception e) {
			throw new RuntimeException("Error reading public key from file '" + fileName + "'", e);
		}
	}

	/**
	 * Save public and private keys
	 */
	public void saveKeys(String privateKeyPath, String publicKeyPath) {
		this.privateKeyPath = privateKeyPath;
		this.publicKeyPath = publicKeyPath;

		FileOutputStream privateKeyOutputStream = null;
		FileOutputStream publicKeyOutputStream = null;

		try {
			File privateKeyFile = new File(privateKeyPath);
			File publicKeyFile = new File(publicKeyPath);

			// Write private key
			privateKeyOutputStream = new FileOutputStream(privateKeyFile);
			privateKeyOutputStream.write(privateKey.getEncoded());

			// Write public key
			publicKeyOutputStream = new FileOutputStream(publicKeyFile);
			publicKeyOutputStream.write(publicKey.getEncoded());
		} catch (IOException ioException) {
			throw new RuntimeException("Error saving keys to files:\n\t'" + privateKeyPath + "'\n\t'" + publicKeyPath + "'", ioException);
		} finally {
			try {
				if (privateKeyOutputStream != null) privateKeyOutputStream.close();
				if (publicKeyOutputStream != null) publicKeyOutputStream.close();
			} catch (IOException ioException) {
				throw new RuntimeException("Error closing files", ioException);
			}
		}
	}

	/**
	 * Sign a text
	 */
	public String sign(String text) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
			signature.initSign(getPrivateKey(), new SecureRandom());

			byte[] message = text.getBytes();
			signature.update(message);
			byte[] sigBytes = signature.sign();
			String signatureTxt = Base64.encodeBase64String(sigBytes);

			return signatureTxt;
		} catch (Exception e) {
			throw new RuntimeException("Error signing text ", e);
		}
	}

	/**
	 * Check that a signature is correct
	 */
	public boolean signCheck(String text, String base64Signature) {
		try {
			Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);

			byte[] message = text.getBytes();
			byte[] sigBytes = Base64.decodeBase64(base64Signature);

			signature.initVerify(getPublicKey());
			signature.update(message);
			return signature.verify(sigBytes);
		} catch (Exception e) {
			throw new RuntimeException("Error signing text ", e);
		}
	}

	/**
	 * Simple encryption decryption test
	 */
	void testCrypt() {
		String originalTxt = "John has a long mustache";
		System.out.println("In        : " + originalTxt);

		String encrypted = encrypt(originalTxt);
		System.out.println("Encrypted : " + encrypted);

		String decrypted = decrypt(encrypted);
		System.out.println("Out       : " + decrypted);

		if (!decrypted.equals(originalTxt)) throw new RuntimeException("Error: Encryption / decryption does not produce the original text!");
	}

	/**
	 * Simple signature test
	 */
	void testSign() {
		String originalTxt = "John has a long mustache";
		System.out.println("In           : " + originalTxt);

		String signature = sign(originalTxt);
		System.out.println("Signature    : " + signature);

		boolean ok = signCheck(originalTxt, signature);
		if (!ok) throw new RuntimeException("Signature does not match!");
		System.out.println("Signature OK : " + ok);
	}

	@Override
	public String toString() {
		return "\tPrivate key  : " + privateKey //
				+ "\n\tPublic key : " + publicKey;
	}

}
