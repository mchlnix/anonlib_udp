package com.anonudp.MixChannel;

import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import junit.framework.TestCase;
import org.junit.jupiter.api.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static com.anonudp.MixMessage.Util.randomBytes;
import static org.junit.jupiter.api.Assertions.*;

class ChannelTest extends TestCase {
    private IPv4AndPort source, destination;
    private PublicKey[] mixKeys;

    @BeforeEach
    protected void setUp()  {
        try {
            this.source = new IPv4AndPort("127.0.0.1", 12345);
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
            new Channel(this.source, this.destination, this.mixKeys);
        }
    }

    @DisplayName("IDs throw exception when running out")
    @Test
    void noMoreExceptions() throws IOException {
        Channel._removeAllChannels();

        for (int i = 0; i < Channel.HIGHEST_ID; ++i)
        {
            new Channel(this.source, this.destination, this.mixKeys);
        }

        Assertions.assertThrows(IllegalStateException.class, () -> new Channel(this.source, this.destination, this.mixKeys));

        Channel._removeAllChannels();
    }

    @DisplayName("Split payload into multiple fragments")
    @Test
    void request() throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        byte[] udpPayload = randomBytes(500);

        Channel channel = new Channel(this.source, this.destination, this.mixKeys);

        channel._setInitialized();

        byte[][] packets = channel.request(udpPayload);

        assertTrue(packets.length > 1);
    }

    @DisplayName("Make multiple response fragments to one message")
    @Test
    @Disabled
    void response() throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        byte[] udpPayload = randomBytes(500);

        Channel channel = new Channel(this.source, this.destination, this.mixKeys);

        channel._setInitialized();

        byte[][] packets = channel.request(udpPayload);

        for (byte[] packet: packets)
            channel.response(packet);

        assertTrue(channel.hasNext());
        assertArrayEquals(udpPayload, channel.next());
    }

    @DisplayName("A duplicate response is detected")
    @Test
    void detectReplay() throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Channel channel = new Channel(this.source, this.destination, this.mixKeys);

        channel._setInitialized();

        byte[] dataPacket = channel.request(randomBytes(100))[0];

        assertDoesNotThrow(() -> channel.response(dataPacket));

        assertThrows(IllegalStateException.class, () -> channel.response(dataPacket));
    }
}