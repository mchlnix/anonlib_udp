package com.anonudp.MixMessage;

import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.PacketFactory;
import com.anonudp.MixPacket.ProcessedDataPacket;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
            for (byte[] channelKey : this.factory.getChannelKeys()) {
                packet = this.factory.process(packet, channelKey);
            }

            assertArrayEquals(dataFragment.toBytes(), packet.toBytes());
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

        DataPacket packet = new DataPacket(this.channelID, payload);
        ProcessedDataPacket processedPacket = new ProcessedDataPacket(this.channelID, payload);

        assertEquals(packet, processedPacket);
        assertEquals(processedPacket, packet);
    }
}