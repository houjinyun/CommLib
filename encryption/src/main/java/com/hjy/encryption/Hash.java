package com.hjy.encryption;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/**
 * MD5算法
 *
 * @author houjinyun
 *
 */
public class Hash {

	public static int FILE_READ_BUFFER_SIZE = 1048576;

	/**
	 * 获取32位MD5加密数据
	 * 
	 * @param content
	 * @return
	 */
	public static String getMD5(String content) {
		if(content == null) {
			return null;
		}
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(content.getBytes());
			byte tmp[] = md.digest();
			return HexUtil.toHex(tmp);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * 获取文件的MD5值
     *
     * @param file 文件
     * @return
     */
	public static String getMd5(File file) {
		if(file != null && file.exists()) {
            try {
                return getMd5(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
	}

    public static String getMd5(InputStream is) {
        if(is == null)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[FILE_READ_BUFFER_SIZE];
            int len;
            while((len = is.read(buffer, 0, FILE_READ_BUFFER_SIZE)) > 0) {
                md.update(buffer, 0, len);
            }
            is.close();
            return HexUtil.toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件的CRC32校验值
     *
     * @param file 文件
     * @return
     */
    public static String getCRC32String(File file){
        if(file != null && file.exists()) {
            try {
                return getCRC32String(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getCRC32String(byte[] content) {
        if(content != null) {
            return getCRC32String(new ByteArrayInputStream(content));
        } else {
            return null;
        }
    }

    public static String getCRC32String(InputStream is) {
        if(is == null)
            return null;
        String strDes = null;
        try {
            byte[] buffer = new byte[FILE_READ_BUFFER_SIZE];
            CRC32 c = new CRC32();
            CheckedInputStream cis = new CheckedInputStream(is, c);
            while(cis.read(buffer, 0, FILE_READ_BUFFER_SIZE) > 0) {
            }
            strDes = Long.toHexString(c.getValue());
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strDes;
    }

}
