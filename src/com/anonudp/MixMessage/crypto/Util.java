package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.AESCTRException;
import com.anonudp.MixMessage.crypto.Exception.AESGCMException;

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
    static final int GCM_MAC_SIZE = 16;
    static final int IV_SIZE = 16;

    public static Cipher createCTRCipher(byte[] symmetricKey, byte[] iv, int mode) throws AESCTRException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            throw new AESCTRException("Could not get instance of AES Counter Cipher.", e);
        }

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        try {
            cipher.init(mode, keySpec, ivSpec);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new AESCTRException("Could not initialize AES Counter Cipher.", e);
        }

        return cipher;
    }

    public static Cipher createGCM(byte[] symmetricKey, byte[] iv, int mode) throws AESGCMException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            throw new AESGCMException("Could not get instance of AES GCM Cipher.", e);
        }

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_MAC_SIZE * Byte.SIZE, iv);

        try {
            cipher.init(mode, keySpec, gcmSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new AESGCMException("Could not initialize AES Counter Cipher.", e);
        }

        return cipher;
    }
}
