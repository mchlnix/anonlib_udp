package com.anonudp.MixMessage.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Util {
    private static int MAC_SIZE = 16;

    public static Cipher createCTRCipher(byte[] symmetricKey, byte[] iv, int mode) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }

    public static Cipher createCTRCipher(byte[] symmetricKey, int mode) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return createCTRCipher(symmetricKey, new byte[EccGroup713.symmetricKeyLength], mode);
    }

    public static Cipher createGCM(byte[] symmetricKey, byte[] iv, int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(MAC_SIZE * 8, iv);

        cipher.init(mode, keySpec, gcmSpec);

        return cipher;
    }

    public static Cipher createGCM(byte[] symmetricKey, int mode) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        return createGCM(symmetricKey, new byte[EccGroup713.symmetricKeyLength], mode);
    }
}
