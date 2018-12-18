package com.anonudp.MixMessage.test;

import com.anonudp.MixMessage.Fragment;
import junit.framework.TestCase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class SingleFragmentTest extends TestCase {
    private Fragment fragment;

    private byte[] message_id_bytes = {0x00, 0x03};
    private byte[] payload;
    private byte[] padding_size_bytes;

    public SingleFragmentTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        int payload_length = 20;

        this.payload = new byte[payload_length];
        Arrays.fill(this.payload, (byte) 0x01);

        this.padding_size_bytes = new byte[]{0x01, (byte) 0xFB};

        this.fragment = new Fragment(1234, 0, true, this.payload, Fragment.FRAGMENT_DATA_PAYLOAD);
    }

    public void testMessageId() {
        try {
            assertEquals(Fragment.SINGLE_FRAGMENT_MESSAGE_ID, this.fragment.getMessage_id());
            assertArrayEquals(this.message_id_bytes, Arrays.copyOf(fragment.toBytes(), this.message_id_bytes.length));
        } catch (IOException io) {
            fail();
        }
    }

    public void testFragmentNumber() {
        assertEquals(Fragment.SINGLE_FRAGMENT_FRAGMENT_NUMBER, this.fragment.getFragment_number());
    }

    public void testPayloadLength() {
        assertEquals(this.payload.length, this.fragment.getPayload().length);

        assertArrayEquals(this.payload, this.fragment.getPayload());
    }

    public void testPaddingBytes() {
        assertArrayEquals(this.padding_size_bytes, this.fragment.getPadding_bytes());
    }

    public void testPaddingLength() {
        int expected = Fragment.FRAGMENT_LENGTH - Fragment.FRAGMENT_ID_SIZE -
                this.fragment.getPadding_bytes().length - this.fragment.getPayload().length;

        assertEquals(expected, this.fragment.getPadding_length());
    }

    public void testToBytes() {
        try (FileOutputStream fos = new FileOutputStream("/home/michael/Schreibtisch/packet2.bytes")) {
            assertEquals(Fragment.FRAGMENT_LENGTH, this.fragment.toBytes().length);

            fos.write(this.fragment.toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testByteArrayConstructor() {
        try {
            Fragment copy = new Fragment(this.fragment.toBytes());

            assertArrayEquals(this.fragment.toBytes(), copy.toBytes());
        } catch (IOException e) {
            fail();
        }

    }
}