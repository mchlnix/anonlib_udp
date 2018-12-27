package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.DataPacketFactory;
import com.anonudp.MixPacket.IPacket;
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

        byte[][] channelKeys = new byte[3][EccGroup713.SYMMETRIC_KEY_LENGTH];

        for (int i = 0; i < 3; ++i)
        {
            channelKeys[i] = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);
        }

        byte[] payload = Util.randomBytes(500);

        LinkEncryption linkCrypt = new LinkEncryption(symmetricKey);

        DataPacketFactory factory = new DataPacketFactory(channelID, channelKeys);

        Fragment fragment = new Fragment(100, 0, payload, Fragment.DATA_PAYLOAD_SIZE);

        DataPacket packet = factory.makePacket(fragment);

        byte[] linkEncryptedPacket = linkCrypt.encrypt(packet);

        IPacket decryptedPacket = linkCrypt.decrypt(linkEncryptedPacket);

        assertEquals(packet, decryptedPacket);
    }
}