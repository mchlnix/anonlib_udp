package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.anonudp.MixMessage.crypto.EccGroup713.SYMMETRIC_KEY_LENGTH;
import static com.anonudp.MixPacket.InitPacket.CHANNEL_KEY_ONION_SIZE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InitPacketFactoryTest extends TestCase {
    private byte[] payload;

    private PrivateKey[] privateMixKeys;

    private byte[] channelID;

    private Counter counter;

    private byte[] initPayload;

    private PacketFactory factory;

    @BeforeEach
    protected void setUp()
    {
        int payloadLength = 100;

        this.initPayload = new byte[6];

        this.payload = Util.randomBytes(payloadLength);

        int mixCount = 3;

        this.channelID = new byte[]{0x01, 0x02};

        this.counter = new Counter();

        this.privateMixKeys = new PrivateKey[mixCount];
        PublicKey[] publicMixKeys = new PublicKey[mixCount];

        for (int i = 0; i < publicMixKeys.length; ++i)
        {
            this.privateMixKeys[i] = new PrivateKey();
            publicMixKeys[i] = new PublicKey(this.privateMixKeys[i]);
        }

        this.factory = new PacketFactory(channelID, initPayload, publicMixKeys);
    }

    @DisplayName("Same data after making and processing a InitPacket")
    @Test
    void process() {
        InitPacket packet = null;

        try {
            Fragment initFragment = new Fragment(1234, 0, this.payload, Fragment.INIT_PAYLOAD_SIZE);

            assertEquals(initFragment.toBytes().length, Fragment.SIZE_INIT);

            packet = this.factory.makeInitPacket(initFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            for (int i = 0; i < this.privateMixKeys.length; ++i)
            {
                packet = this.factory.process(packet, this.privateMixKeys[i]);

                assertArrayEquals(this.factory.getRequestChannelKeys()[i], ((ProcessedInitPacket) packet).getRequestChannelKey());
                assertArrayEquals(this.factory.getResponseChannelKeys()[i], ((ProcessedInitPacket) packet).getResponseChannelKey());
            }

            byte[] initSpecific = Arrays.copyOf(packet.getPayloadOnion(), this.initPayload.length);
            byte[] fragment = Arrays.copyOfRange(packet.getPayloadOnion(), this.initPayload.length, packet.getPayloadOnion().length);

            assertArrayEquals(initPayload, initSpecific);
            assertArrayEquals(this.payload, new Fragment(fragment, Fragment.SIZE_INIT).getPayload());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Equality between InitPacket and ProcessedInitPacket")
    @Test
    void equality()
    {
        PrivateKey privateKey = new PrivateKey();
        PublicKey publicKey = new PublicKey(privateKey);

        byte[] requestChannelKey = new byte[SYMMETRIC_KEY_LENGTH];
        byte[] responseChannelKey = new byte[SYMMETRIC_KEY_LENGTH];

        byte[] channelKeyOnion = new byte[CHANNEL_KEY_ONION_SIZE];

        byte[] payloadOnion = new byte[100];

        InitPacket packet = new InitPacket(channelID, counter.asBytes(), publicKey, channelKeyOnion, payloadOnion);
        ProcessedInitPacket processedPacket =
                new ProcessedInitPacket(this.channelID, counter.asBytes(), requestChannelKey, responseChannelKey, publicKey, channelKeyOnion, payloadOnion);

        assertEquals(packet, processedPacket);
        assertEquals(processedPacket, packet);
    }

    @DisplayName("InitPacket creation throws no Exceptions")
    @Test
    void makePacket() {
        try {
            Fragment initFragment = new Fragment(1234, 0, this.payload, Fragment.INIT_PAYLOAD_SIZE);

            assertEquals(initFragment.toBytes().length, Fragment.SIZE_INIT);

            this.factory.makeInitPacket(initFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Different message counters make different packets")
    @Test
    void counterMasking() throws PacketCreationFailed, SymmetricKeyCreationFailed, DecryptionFailed {
        Fragment fragment = new Fragment(1234, 0, Util.randomBytes(10), Fragment.INIT_PAYLOAD_SIZE);

        InitPacket original = this.factory.makeInitPacket(fragment);

        // make fake packet

        byte[] fakePrefix = new byte[Counter.SIZE];

        InitPacket fake = new InitPacket(original.getChannelID(), fakePrefix, original.getPublicKey(), original.getChannelKeyOnion(), original.getPayloadOnion());

        assertNotEquals(original, fake);

        ProcessedInitPacket processedOriginal = this.factory.process(original, this.privateMixKeys[0]);
        ProcessedInitPacket processedFake = this.factory.process(fake, this.privateMixKeys[0]);

        assertNotEquals(processedOriginal, processedFake);

        assertArrayEquals(processedOriginal.getChannelID(), processedFake.getChannelID());

        assertFalse(Arrays.equals(processedOriginal.getMessageID(), processedFake.getMessageID()));

        assertFalse(Arrays.equals(processedOriginal.getChannelKeyOnion(), processedFake.getChannelKeyOnion()));
        assertFalse(Arrays.equals(processedOriginal.getPayloadOnion(), processedFake.getPayloadOnion()));

        assertNotEquals(processedOriginal.getPublicKey(), processedFake.getPublicKey());
    }
}