package com.hjy.encryption;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密算法<br>
 * 
 * @author houjinyun
 *
 */
public class Aes {

	public static final String CIPHER_ALGORITHM_ECB = "AES/ECB/PKCS5Padding";

	public static final String CIPHER_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";

	public static final String CIPHER_ALGORITHM_CBC_NoPadding = "AES/CBC/NoPadding";

	/**
	 * AES/ECB模式
	 * 
	 * @param cleartext 需加密的明文
	 * @param seed key
	 *
	 * @return 16进制格式的字符串
	 */
	public static String encryptECB(String cleartext, String seed) {
		try {
			byte[] rawKey = getRawKeyECB(seed.getBytes());
			byte[] result = encryptECB(rawKey, cleartext.getBytes());
			return HexUtil.toHex(result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES/ECB模式
	 * 
	 * @param encrypted 已加密的字符串，16进制格式的字符串
	 * @param seed key
	 *
	 * @return
	 */
	public static String decryptECB(String encrypted, String seed) {
		try {
			byte[] rawKey = getRawKeyECB(seed.getBytes());
			byte[] enc = HexUtil.toByte(encrypted);
			byte[] result = decryptECB(rawKey, enc);
			return new String(result);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] getRawKeyECB(byte[] seed) throws Exception {
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

	private static byte[] encryptECB(byte[] raw, byte[] clear) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decryptECB(byte[] raw, byte[] encrypted) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}


	/**
	 * AES/CBC/NoPadding 要求：<br/>
	 * 1.密钥必须是16位的；
	 * 2.Initialization vector (IV) 必须是16位<br/>
	 * 3.待加密内容的长度必须是16的倍数，如果不是16的倍数，就会出如下异常：
	 * javax.crypto.IllegalBlockSizeException: Input length not multiple of 16 bytes
	 * <br/><br/>
	 * 由于固定了位数，所以对于被加密数据有中文的, 加、解密不完整
	 *
	 * @param content 明文字符串，长度必须为16的倍数
	 * @param password key 16位
	 * @param iv Initialization vector，一般为长度16的字符串
	 *
	 * @return 16进制字符串
	 */
	public static String encryptCBCNoPadding(String content, String password, String iv) {
		byte[] data = null;
		try {
			data = content.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = encryptCBCNoPadding(data, password, iv);
		String result = HexUtil.toHex(data);
		return result;
	}

	public static byte[] encryptCBCNoPadding(byte[] content, String password, String iv) {
		try {
			SecretKeySpec key = createKey(password);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);
			cipher.init(Cipher.ENCRYPT_MODE, key, createIV(iv));
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES/CBC/NoPadding解密
	 *
	 * @param content 16进制字符串
	 * @param password key 16位
	 * @param iv Initialization vector
	 *
	 * @return 解密后明文字符串
	 */
	public static String decryptCBCNoPadding(String content, String password, String iv) {
		byte[] data = null;
		try {
			data = HexUtil.toByte(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = decryptCBCNoPadding(data, password, iv);
		if (data == null)
			return null;
		String result = null;
		try {
			result = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] decryptCBCNoPadding(byte[] content, String password, String iv) {
		try {
			SecretKeySpec key = createKey(password);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC_NoPadding);
			cipher.init(Cipher.DECRYPT_MODE, key, createIV(iv));
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * AES/CBC/PKCS5Padding <br/>
	 * 对要加密的文本没有像NoPadding时有长度限制，但如果包含中文，一般需要对文本进行base64编码
	 *
	 * @param content
	 * @param password
	 * @param iv
	 * @return
	 */
	public static String encryptCBCPKCS5Padding(String content, String password, String iv) {
		byte[] data = null;
		try {
			data = content.getBytes("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = encryptCBCPKCS5Padding(data, password, iv);
		String result = HexUtil.toHex(data);
		return result;
	}

	public static byte[] encryptCBCPKCS5Padding(byte[] content, String password, String iv) {
		try {
			SecretKeySpec key = createKey(password);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
			cipher.init(Cipher.ENCRYPT_MODE, key, createIV(iv));
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * AES/CBC/PKCS5Padding
	 *
	 * @param content
	 * @param password
	 * @param iv
	 * @return
	 */
	public static String decryptCBCPKCS5Padding(String content, String password, String iv) {
		byte[] data = null;
		try {
			data = HexUtil.toByte(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		data = decryptCBCNoPadding(data, password, iv);
		if (data == null)
			return null;
		String result = null;
		try {
			result = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] decryptCBCPKCS5Padding(byte[] content, String password, String iv) {
		try {
			SecretKeySpec key = createKey(password);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
			cipher.init(Cipher.DECRYPT_MODE, key, createIV(iv));
			byte[] result = cipher.doFinal(content);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	private static SecretKeySpec createKey(String key) {
		byte[] data = null;
		if (key == null) {
			key = "";
		}
		StringBuffer sb = new StringBuffer(16);
		sb.append(key);
		while (sb.length() < 16) {
			sb.append("0");
		}
		if (sb.length() > 16) {
			sb.setLength(16);
		}

		try {
			data = sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new SecretKeySpec(data, "AES");
	}

	private static IvParameterSpec createIV(String password) {
		byte[] data = null;
		if (password == null) {
			password = "";
		}
		StringBuffer sb = new StringBuffer(16);
		sb.append(password);
		while (sb.length() < 16) {
			sb.append("0");
		}
		if (sb.length() > 16) {
			sb.setLength(16);
		}

		try {
			data = sb.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new IvParameterSpec(data);
	}

}