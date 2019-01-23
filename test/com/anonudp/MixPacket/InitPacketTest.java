package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InitPacketTest extends TestCase {

    @DisplayName("Equals works")
    @Test
    void equals() throws PacketCreationFailed {
        byte[] channelID = new byte[]{0x0, (byte) 0xe0};
        byte[] payload = Util.randomBytes(100);

        PublicKey[] keys = new PublicKey[3];

        for (int i = 0; i < keys.length; ++i)
        {
            PrivateKey key = new PrivateKey();
            keys[i] = new PublicKey(key);
        }

        PacketFactory factory = new PacketFactory(channelID, payload, keys);

        Fragment fragment = new Fragment(1234, 0, Util.randomBytes(10), Fragment.INIT_PAYLOAD_SIZE);

        InitPacket original = factory.makeInitPacket(fragment);

        InitPacket copied = new InitPacket(original.getChannelID(), original.getMessageID(), original.getPublicKey(), original.getChannelKeyOnion(), original.getPayloadOnion());

        assertEquals(original, copied);

        ProcessedInitPacket processed = new ProcessedInitPacket(original.getChannelID(), original.getMessageID(), /* is ignored: */ new byte[16], /* is ignored: */ new byte[16], original.getPublicKey(), original.getChannelKeyOnion(), original.getPayloadOnion());

        assertEquals(original, processed);
    }

    @DisplayName("Not equals works")
    @Test
    void notEquals() throws PacketCreationFailed {
        byte[] channelID = new byte[]{0x0, (byte) 0xe0};
        byte[] payload = Util.randomBytes(100);

        PublicKey[] keys = new PublicKey[3];

        for (int i = 0; i < keys.length; ++i)
        {
            PrivateKey key = new PrivateKey();
            keys[i] = new PublicKey(key);
        }

        PacketFactory factory = new PacketFactory(channelID, payload, keys);

        Fragment fragment = new Fragment(1234, 0, Util.randomBytes(10), Fragment.INIT_PAYLOAD_SIZE);

        InitPacket original = factory.makeInitPacket(fragment);

        byte[] fakePrefix = new byte[Counter.SIZE];

        InitPacket fake = new InitPacket(original.getChannelID(), fakePrefix, original.getPublicKey(), original.getChannelKeyOnion(), original.getPayloadOnion());

        assertNotEquals(original, fake);

        ProcessedInitPacket fakeProcessed = new ProcessedInitPacket(original.getChannelID(), fakePrefix, /* is ignored: */ new byte[16], /* is ignored: */ new byte[16], original.getPublicKey(), original.getChannelKeyOnion(), original.getPayloadOnion());

        assertNotEquals(original, fakeProcessed);
    }
}