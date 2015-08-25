package com.hjy.encryption;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密算法<br>
 * 采用AES加密后的byte数据，是不能直接构造成String对象的，必须采用一些位补齐方式才能构造出String，一般使用16进制的形式构造成字符串
 * 
 * @author houjinyun
 *
 */
public class Aes {
	
	/**
	 * 加密
	 * 
	 * @param seed key
	 * @param cleartext 需加密的明文
	 * 
	 * @return 16进制格式的字符串
	 */
	public static String encrypt(String seed, String cleartext) {
		try {
			byte[] rawKey = getRawKey(seed.getBytes());
			byte[] result = encrypt(rawKey, cleartext.getBytes());
			return HexUtil.toHex(result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param seed key
	 * @param encrypted 已加密的字符串，16进制格式的字符串
	 * 
	 * @return
	 */
	public static String decrypt(String seed, String encrypted) {
		try {
			byte[] rawKey = getRawKey(seed.getBytes());
			byte[] enc = HexUtil.toByte(encrypted);
			byte[] result = decrypt(rawKey, enc);
			return new String(result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] getRawKey(byte[] seed) throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
//		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		//在安卓手机上使用，必须要使用"Crypto"作为provider，否则会报错，但是在非安卓手机上使用则会报错
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto"); 
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
	}

	private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

}
