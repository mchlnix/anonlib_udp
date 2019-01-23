package com.anonudp.MixChannel;

import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixPacket.IPacket;
import junit.framework.TestCase;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.UnknownHostException;

import static com.anonudp.MixMessage.Util.randomBytes;
import static org.junit.jupiter.api.Assertions.*;

class ChannelTest extends TestCase {
    private IPv4AndPort destination;
    private PublicKey[] mixKeys;

    @BeforeEach
    protected void setUp()  {
        try {
            this.destination = new IPv4AndPort("127.0.0.1", 12345);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.mixKeys = new PublicKey[1];

        this.mixKeys[0] = new PublicKey(new PrivateKey());
    }

    @DisplayName("Random ids do not repeat")
    @Test
    void nonRepeatingIDs() throws IOException {
        Channel._removeAllChannels();

        for (int i = 0; i < 1000; ++i)
        {
            new Channel(this.destination, this.mixKeys);
        }
    }

    @DisplayName("IDs throw exception when running out")
    @Test
    void noMoreExceptions() throws IOException {
        Channel._removeAllChannels();

        for (int i = 0; i < Channel.HIGHEST_ID; ++i)
        {
            new Channel(this.destination, this.mixKeys);
        }

        Assertions.assertThrows(IllegalStateException.class, () -> new Channel(this.destination, this.mixKeys));

        Channel._removeAllChannels();
    }

    @DisplayName("Split payload into multiple fragments")
    @Test
    void request() throws IOException, PacketCreationFailed {
        byte[] udpPayload = randomBytes(500);

        Channel channel = new Channel(this.destination, this.mixKeys);

        channel._setInitialized();

        IPacket[] packets = channel.request(udpPayload);

        assertTrue(packets.length > 1);
    }

    @DisplayName("Make multiple response fragments to one message")
    @Test
    @Disabled
    void response() throws IOException, DecryptionFailed, PacketCreationFailed {
        byte[] udpPayload = randomBytes(500);

        Channel channel = new Channel(this.destination, this.mixKeys);

        channel._setInitialized();

        IPacket[] packets = channel.request(udpPayload);

        for (IPacket packet: packets)
            channel.response(packet);

        assertTrue(channel.hasNext());
        assertArrayEquals(udpPayload, channel.next());
    }

    @DisplayName("A duplicate response is detected")
    @Test
    void detectReplay() throws IOException, PacketCreationFailed {
        Channel channel = new Channel(this.destination, this.mixKeys);

        channel._setInitialized();

        IPacket dataPacket = channel.request(randomBytes(100))[0];

        assertDoesNotThrow(() -> channel.response(dataPacket));

        assertThrows(IllegalStateException.class, () -> channel.response(dataPacket));
    }
}