package com.anonudp.MixMessage;

import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixPacket.InitPacket;
import com.anonudp.MixPacket.InitPacketFactory;
import com.anonudp.MixPacket.ProcessedInitPacket;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class InitPacketFactoryTest extends TestCase {
    private byte[] payload;

    private PrivateKey[] privateMixKeys;

    private byte[] channelID;

    private Counter counter;

    private byte[][] channelKeys;

    private byte[] initPayload;

    private InitPacketFactory factory;

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

        this.channelKeys = new byte[mixCount][EccGroup713.SYMMETRIC_KEY_LENGTH];

        for (int i = 0; i < publicMixKeys.length; ++i)
        {
            this.privateMixKeys[i] = new PrivateKey();
            publicMixKeys[i] = new PublicKey(this.privateMixKeys[i]);

            this.channelKeys[i] = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);
        }

        this.factory = new InitPacketFactory(channelID, initPayload, publicMixKeys);
    }

    @DisplayName("Same data after making and processing a InitPacket")
    @Test
    void process() {
        InitPacket packet = null;

        try {
            Fragment initFragment = new Fragment(1234, 0, this.payload, Fragment.INIT_PAYLOAD_SIZE);

            assertEquals(initFragment.toBytes().length, Fragment.INIT_FRAGMENT_SIZE);

            packet = this.factory.makePacket(this.channelKeys, counter.asPrefix(), initFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            for (int i = 0; i < this.privateMixKeys.length; ++i)
            {
                packet = this.factory.process(packet, this.privateMixKeys[i]);

                assertArrayEquals(this.channelKeys[i], ((ProcessedInitPacket) packet).getChannelKey());
            }

            byte[] initSpecific = Arrays.copyOf(packet.getPayloadOnion(), this.initPayload.length);
            byte[] fragment = Arrays.copyOfRange(packet.getPayloadOnion(), this.initPayload.length, packet.getPayloadOnion().length);

            assertArrayEquals(initPayload, initSpecific);
            assertArrayEquals(this.payload, new Fragment(fragment).getPayload());
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

        byte[] channelKey = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];
        byte[] channelKeyOnion = new byte[this.channelKeys.length * EccGroup713.SYMMETRIC_KEY_LENGTH];
        byte[] payloadOnion = new byte[100];

        InitPacket packet = new InitPacket(channelID, counter.asPrefix(), publicKey, channelKeyOnion, payloadOnion);
        ProcessedInitPacket processedPacket =
                new ProcessedInitPacket(this.channelID, counter.asPrefix(), channelKey, publicKey, channelKeyOnion, payloadOnion);

        assertEquals(packet, processedPacket);
        assertEquals(processedPacket, packet);
    }

    @DisplayName("InitPacket creation throws no Exceptions")
    @Test
    void makePacket() {
        try {
            Fragment initFragment = new Fragment(1234, 0, this.payload, Fragment.INIT_PAYLOAD_SIZE);

            assertEquals(initFragment.toBytes().length, Fragment.INIT_FRAGMENT_SIZE);

            this.factory.makePacket(this.channelKeys, counter.asPrefix(), initFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}