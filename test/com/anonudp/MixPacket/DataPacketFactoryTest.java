package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.PublicKey;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DataPacketFactoryTest extends TestCase {
    private byte[] payload;

    private byte[] channelID;

    private PacketFactory factory;

    @BeforeEach
    protected void setUp()
    {
        int payloadLength = 100;

        this.payload = Util.randomBytes(payloadLength);

        this.channelID = new byte[]{0x01, 0x02};

        PublicKey[] publicKeys = new PublicKey[3];

        this.factory = new PacketFactory(this.channelID, new byte[6], publicKeys);
    }

    @DisplayName("DataPacket creation throws no Exceptions")
    @Test
    void makePacket() {
        try {
            Fragment dataFragment = new Fragment(1234, 0, this.payload, Fragment.DATA_PAYLOAD_SIZE);

            assertEquals(dataFragment.toBytes().length, Fragment.SIZE_DATA);

            this.factory.makeDataPacket(dataFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Encryption takes place")
    @Test
    void testEncryption() throws IOException
    {
        DataPacket packet = null;

        Fragment dataFragment = new Fragment(1234, 0, this.payload, Fragment.DATA_PAYLOAD_SIZE);
        try {
            packet = this.factory.makeDataPacket(dataFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        assertFalse(Arrays.equals(dataFragment.toBytes(), packet.getData()));
    }

    @DisplayName("Same data after making and processing a DataPacket")
    @Test
    void process() {
        DataPacket packet = null;

        Fragment dataFragment = new Fragment(1234, 0, this.payload, Fragment.DATA_PAYLOAD_SIZE);
        try {
            packet = this.factory.makeDataPacket(dataFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            for (byte[] channelKey : this.factory.getRequestChannelKeys()) {
                packet = this.factory.process(packet, channelKey);
            }

            assertArrayEquals(dataFragment.toBytes(), packet.getData());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @DisplayName("Equality between DataPacket and ProcessedDataPacket")
    @Test
    void equality()
    {
        byte[] payload = Util.randomBytes(200);

        Counter counter = new Counter();

        DataPacket packet = new DataPacket(this.channelID, counter.asBytes(), payload);
        ProcessedDataPacket processedPacket = new ProcessedDataPacket(this.channelID, counter.asBytes(), payload);

        assertEquals(packet, processedPacket);
        assertEquals(processedPacket, packet);
    }
}