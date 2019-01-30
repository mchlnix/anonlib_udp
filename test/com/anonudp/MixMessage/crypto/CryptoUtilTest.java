package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Disabled("Used to figure out how this is supposed to be used.")
class CryptoUtilTest extends TestCase
{
    @DisplayName("Basic encryption functionality")
    @Test
    void testEncryption() throws InvalidCipherTextException
    {
        byte[] key = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];
        byte[] iv = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];

        PaddedBufferedBlockCipher cipher = Util.createCTRCipher(key, iv, true);

        int plainLength = 42;
        int cipherLength = 48; // 3 * block length

        assertEquals(cipherLength, cipher.getOutputSize(plainLength));

        byte[] plainText = new byte[plainLength];
        byte[] cipherText = new byte[cipherLength];

        int alreadyEncrypted = cipher.processBytes(plainText, 0, plainLength, cipherText, 0);

        assertTrue(alreadyEncrypted < plainLength);

        alreadyEncrypted += cipher.doFinal(cipherText, alreadyEncrypted);

        assertEquals(cipherLength, cipherText.length);
        assertEquals(alreadyEncrypted, cipherText.length);
    }

    @DisplayName("Basic decryption functionality")
    @Test
    void testDecryption() throws InvalidCipherTextException
    {
        byte[] key = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];
        byte[] iv = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];

        PaddedBufferedBlockCipher cipher = Util.createCTRCipher(key, iv, true);

        int plainLength = 42;
        int cipherLength = 48; // 3 * block length

        byte[] plainText = new byte[plainLength];
        byte[] cipherText = new byte[cipherLength];

        int alreadyEncrypted = cipher.processBytes(plainText, 0, plainLength, cipherText, 0);

        cipher.doFinal(cipherText, alreadyEncrypted);

        byte[] shortendCipherText = new byte[plainLength];
        System.arraycopy(cipherText, 0, shortendCipherText, 0, shortendCipherText.length);

        assertEquals(plainLength, shortendCipherText.length);

        cipher = Util.createCTRCipher(key, iv, false);

        byte[] decipheredText = new byte[shortendCipherText.length];

        int alreadyDeciphered = cipher.processBytes(shortendCipherText, 0, shortendCipherText.length, decipheredText, 0);

        alreadyDeciphered += cipher.doFinal(shortendCipherText, alreadyDeciphered);
    }
}