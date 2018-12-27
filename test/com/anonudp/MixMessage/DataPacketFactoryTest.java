package com.anonudp.MixMessage;

import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.DataPacketFactory;
import com.anonudp.MixPacket.ProcessedDataPacket;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class DataPacketFactoryTest extends TestCase {
    private byte[] payload;
    private byte[][] channelKeys;

    private byte[] channelID;

    private DataPacketFactory factory;

    @BeforeEach
    protected void setUp()
    {
        int payloadLength = 100;

        this.payload = Util.randomBytes(payloadLength);

        int mixCount = 3;

        this.channelID = new byte[]{0x01, 0x02};

        this.channelKeys = new byte[mixCount][EccGroup713.SYMMETRIC_KEY_LENGTH];

        for (int i = 0; i < channelKeys.length; ++i)
        {
            this.channelKeys[i] = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);
        }

        this.factory = new DataPacketFactory(this.channelID, this.channelKeys);
    }

    @DisplayName("DataPacket creation throws no Exceptions")
    @Test
    void makePacket() {
        try {
            Fragment dataFragment = new Fragment(1234, 0, this.payload, Fragment.DATA_PAYLOAD_SIZE);

            assertEquals(dataFragment.toBytes().length, Fragment.DATA_FRAGMENT_SIZE);

            this.factory.makePacket(dataFragment);
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
            packet = this.factory.makePacket(dataFragment);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try {
            for (byte[] channelKey : this.channelKeys) {
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