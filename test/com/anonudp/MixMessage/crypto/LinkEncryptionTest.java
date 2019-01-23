package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.EncryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.PacketFactory;
import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinkEncryptionTest extends TestCase {

    @DisplayName("Checks interoperability of encrypt and decrypt")
    @Test
    void enAndDecrypt() throws EncryptionFailed, DecryptionFailed, PacketCreationFailed {
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

        assertEquals((LinkEncryption.OVERHEAD + Fragment.SIZE_DATA), linkEncryptedPacket.length);

        IPacket decryptedPacket = linkCrypt.decrypt(linkEncryptedPacket);

        assertEquals(packet, decryptedPacket);
    }
}