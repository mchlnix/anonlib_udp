package com.anonudp.MixChannel;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class IPv4AndPortTest extends TestCase {
    private IPv4AndPort address1;
    private IPv4AndPort address2;
    private IPv4AndPort address3;

    private byte[] byteAddress1;
    private byte[] byteAddress2;
    private byte[] byteAddress3;

    @BeforeEach
    protected void setUp() throws UnknownHostException {
        address1 = new IPv4AndPort("127.0.0.1", 20000);
        address2 = new IPv4AndPort("0.0.0.0", 0);
        address3 = new IPv4AndPort("1.2.3.4", 1234);

        byteAddress1 = new byte[]{0x7F, 0x00, 0x00, 0x01, 0x4E, 0x20};
        byteAddress2 = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byteAddress3 = new byte[]{0x01, 0x02, 0x03, 0x04, 0x04, (byte) 0xD2};
    }

    @DisplayName("Byte export works")
    @Test
    void toBytes() throws IOException {
        assertArrayEquals(byteAddress1, address1.toBytes());
        assertArrayEquals(byteAddress2, address2.toBytes());
        assertArrayEquals(byteAddress3, address3.toBytes());
    }

    @DisplayName("Exceptions are propagated")
    @Test
    void throwsException()
    {
        assertThrows(UnknownHostException.class, () -> new IPv4AndPort("abc.def.ghi", 10000));
    }
}