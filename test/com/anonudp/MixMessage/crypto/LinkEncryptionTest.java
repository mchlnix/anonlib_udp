package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.PacketFactory;
import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class LinkEncryptionTest extends TestCase {

    @DisplayName("Checks interoperability of encrypt and decrypt")
    @Test
    void enAndDecrypt() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException {
        byte[] symmetricKey = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);

        byte[] channelID = new byte[]{0x01, 0x02};

        byte[] payload = Util.randomBytes(500);

        LinkEncryption linkCrypt = new LinkEncryption(symmetricKey);

        byte[] initPayload = new byte[6];

        PublicKey[] publicKeys = new PublicKey[3];

        PacketFactory factory = new PacketFactory(channelID, initPayload, publicKeys);

        Fragment fragment = new Fragment(100, 0, payload, Fragment.DATA_PAYLOAD_SIZE);

        DataPacket packet = factory.makeDataPacket(fragment);

        byte[] linkEncryptedPacket = linkCrypt.encrypt(packet);

        IPacket decryptedPacket = linkCrypt.decrypt(linkEncryptedPacket);

        assertEquals(packet, decryptedPacket);
    }
}