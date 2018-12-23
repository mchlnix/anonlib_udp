package com.anonudp.MixMessage.crypto;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class Util {
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
}
