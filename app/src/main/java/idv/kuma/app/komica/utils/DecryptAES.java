package idv.kuma.app.komica.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptAES {
    private static String TAG = DecryptAES.class.getSimpleName();

    private final static byte[] IvAES = {0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00};

    /**
     * AES256 Decrypt with String function
     *
     * @author takumalee
     */
    public static String decryptAesWithResult(String encryptStr, String key) {

        String aes256Decrypted = null;
        try {
            aes256Decrypted = DecryptAES.decrypt(DecryptAES.IvAES, DecryptAES.to32Bytes(key.getBytes("UTF-8")), Base64.decode(encryptStr.getBytes("UTF-8"), Base64.DEFAULT));
            Log.v(TAG, aes256Decrypted);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return aes256Decrypted;
    }

    @SuppressWarnings("unused")
    private static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    private static String decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        System.out.println("Key Length: " + newKey.getEncoded().length);
        System.out.println("Key Algorithm: " + newKey.getAlgorithm());
        System.out.println("Key Format: " + newKey.getFormat());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        byte[] clearByte = cipher.doFinal(textBytes);
        return new String(clearByte, "UTF-8");
    }

    /**
     * change key to 32 bytes for decrypt
     *
     * @param ASE-KEY.getBytes("UTF-8");
     * @author takumalee
     */
    private static byte[] to32Bytes(byte[] aesKey) {
        byte[] result = new byte[32];
        for (int i = 0; i < result.length; i++) {
            if (i < aesKey.length)
                result[i] = aesKey[i];
            else
                result[i] = 0;
        }
        Log.v(TAG, new String(result));
        return result;
    }
}
