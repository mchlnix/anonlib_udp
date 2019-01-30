package com.anonudp.MixMessage.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class CTRCipher extends BufferedBlockCipher
{
    public static final boolean ENCRYPT_MODE = true;
    public static final boolean DECRYPT_MODE = false;

    static final int KEY_SIZE = 16;
    static final int IV_SIZE = 16;

    public static CTRCipher getCipher(byte[] symmetricKey, byte[] iv, boolean cryptMode)
    {
        CTRCipher cipher = new CTRCipher(new SICBlockCipher(new AESFastEngine()));

        cipher.init(cryptMode, new ParametersWithIV(new KeyParameter(symmetricKey), iv));

        return cipher;
    }


    private CTRCipher(BlockCipher blockCipher)
    {
        super(blockCipher); //TODO use crypto padding
    }

    public byte[] encryptBuffer(byte[] plainText) throws InvalidCipherTextException
    {
        byte[] cipherText = new byte[this.getOutputSize(plainText.length)];
        byte[] shortenedText = new byte[plainText.length];

        System.arraycopy(plainText, 0, cipherText, 0, plainText.length);

        int alreadyProcessed = this.processBytes(plainText, 0, plainText.length, cipherText, 0);

        this.doFinal(cipherText, alreadyProcessed);

        System.arraycopy(cipherText, 0, shortenedText, 0, plainText.length);

        return shortenedText;
    }


    public byte[] decryptBuffer(byte[] cipherText) throws InvalidCipherTextException
    {
        byte[] paddedCipherText = new byte[this.getOutputSize(cipherText.length)];
        byte[] shortenedText = new byte[cipherText.length];

        System.arraycopy(cipherText, 0, paddedCipherText, 0, cipherText.length);

        int alreadyProcessed = this.processBytes(paddedCipherText, 0, paddedCipherText.length, paddedCipherText, 0);

        this.doFinal(paddedCipherText, alreadyProcessed);

        System.arraycopy(paddedCipherText, 0, shortenedText, 0, cipherText.length);

        return shortenedText;
    }
}
