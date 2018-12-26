package com.anonudp.MixChannel;

import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import junit.framework.TestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.UnknownHostException;

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
        Channel.removeAllChannels();

        for (int i = 0; i < 1000; ++i)
        {
            new Channel(this.source, this.destination, this.mixKeys);
        }
    }

    @DisplayName("IDs throw exception when running out")
    @Test
    void noMoreExceptions() throws IOException {
        Channel.removeAllChannels();

        for (int i = 0; i < Channel.HIGHEST_ID; ++i)
        {
            new Channel(this.source, this.destination, this.mixKeys);
        }

        Assertions.assertThrows(IllegalStateException.class, () -> new Channel(this.source, this.destination, this.mixKeys));
    }
}