package com.hjy.encryption;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
/**
 * 
 * RSA加解密算法<br>
 * 1.客户端采用公钥进行数据加密，服务端采用私钥进行解密<br>
 * 2.服务端采用私钥进行数据签名，客户端采用公钥进行签名验证<br>
 *
 * @author houjinyun
 *
 */
public class Rsa {

	/**
	 * 加密算法
	 */
	private static final String ALGORITHM = "RSA";
	
	/**
	 * 签名算法
	 */
	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	/**
	 * 编码
	 */
	private static final String CHARSET = "UTF-8";
	
	/**
	 * 
	 * @param algorithm 算法
	 * @param bysKey
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	private static PublicKey getPublicKeyFromX509(String algorithm, String bysKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] decodedKey = Base64.decode(bysKey, Base64.NO_WRAP);
		X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
		return keyFactory.generatePublic(x509);
	}

	/**
	 * 采用公钥进行加密
	 * 
	 * @param content 需要加密的字符串
	 * @param publicKey 公钥，64编码
	 * 
	 * @return base64编码后的加密字符串
	 */
	public static String encrypt(String content, String publicKey) {
		try {
			PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, publicKey);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubkey);
			byte plaintext[] = content.getBytes(CHARSET);
			byte[] output = cipher.doFinal(plaintext);
			String s = new String(Base64.encode(output, Base64.NO_WRAP), CHARSET);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 采用私钥进行解密
	 * 
	 * @param encryptData 加密后的数据，base64编码
	 * @param privateKey 私钥，base64编码
	 * 
	 * @return 解密后的字符串
	 */
	public static String decrypt(String encryptData, String privateKey) {
        try {
        	byte[] keyBytes = Base64.decode(privateKey, Base64.NO_WRAP);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateK);
            
            byte[] plaintext = Base64.decode(encryptData.getBytes(CHARSET), Base64.NO_WRAP);
            byte[] result = cipher.doFinal(plaintext);        
            return new String(result, CHARSET);
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return null;
    }
	
	/**
	 * 采用私钥进行签名
	 * 
	 * @param content 需要签名的内容
	 * @param privateKey 私钥，base64编码
	 * 
	 * @return base64编码后的签名字符串
	 */
	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initSign(priKey);
			signature.update(content.getBytes(CHARSET));
			byte[] signed = signature.sign();
			return new String(Base64.encode(signed, Base64.NO_WRAP), CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 验证签名
	 * 
	 * @param content 需要验证的字符串
	 * @param sign 签名字符串
	 * @param publicKey 公钥, base64编码
	 * 
	 * @return true表示签名一致
	 */
	public static boolean doCheck(String content, String sign, String publicKey) {
		try {
			KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
			byte[] encodedKey = Base64.decode(publicKey, Base64.NO_WRAP);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
			java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
			signature.initVerify(pubKey);
			signature.update(content.getBytes(CHARSET));
			boolean bverify = signature.verify(Base64.decode(sign, Base64.NO_WRAP));
			return bverify;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
