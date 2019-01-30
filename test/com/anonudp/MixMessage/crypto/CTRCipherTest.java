package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.anonudp.MixMessage.crypto.CTRCipher.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CTRCipherTest extends TestCase
{
    @DisplayName("Encrypt and decrypt")
    @Test
    void encryptTest() throws InvalidCipherTextException
    {
        CTRCipher encryptor = CTRCipher.getCipher(new byte[KEY_SIZE], new byte[IV_SIZE], ENCRYPT_MODE);

        int plainTextLength = 42;

        byte[] plainText = new byte[(plainTextLength)];

        byte[] cipherText = encryptor.encryptBuffer(plainText);

        assertEquals(plainTextLength, cipherText.length);

        CTRCipher decryptor = CTRCipher.getCipher(new byte[KEY_SIZE], new byte[IV_SIZE], DECRYPT_MODE);

        byte[] decryptedText = decryptor.decryptBuffer(cipherText);

        assertArrayEquals(plainText, decryptedText);
    }
}