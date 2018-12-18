package com.anonudp.MixMessage.test;

import com.anonudp.MixMessage.Fragment;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SingleFragmentTest extends TestCase {
    private Fragment fragment;

    private byte[] message_id_bytes = {0x00, 0x03};
    private byte[] payload;
    private byte[] padding_size_bytes;

    @BeforeEach
    protected void setUp() {
        int payload_length = 20;

        this.payload = new byte[payload_length];
        Arrays.fill(this.payload, (byte) 0x01);

        this.padding_size_bytes = new byte[]{0x01, (byte) 0xFB};

        this.fragment = new Fragment(1234, 0, this.payload, Fragment.FRAGMENT_DATA_PAYLOAD);
    }

    @Test
    void getMessage_id() {
        try {
            assertEquals(Fragment.SINGLE_FRAGMENT_MESSAGE_ID, this.fragment.getMessage_id());
            assertArrayEquals(this.message_id_bytes, Arrays.copyOf(fragment.toBytes(), this.message_id_bytes.length));
        } catch (IOException io) {
            fail();
        }
    }

    @Test
    void getFragment_number() {
        assertEquals(Fragment.SINGLE_FRAGMENT_FRAGMENT_NUMBER, this.fragment.getFragment_number());
    }

    @Test
    void isLast_fragment() {
        assertTrue(this.fragment.isLast());
    }

    @Test
    void getPayload() {
        assertEquals(this.payload.length, this.fragment.getPayload().length);

        assertArrayEquals(this.payload, this.fragment.getPayload());
    }

    @Test
    void getPadding_length() {
        int expected = Fragment.FRAGMENT_LENGTH - Fragment.FRAGMENT_ID_SIZE -
                this.fragment.getPadding_bytes().length - this.fragment.getPayload().length;

        assertEquals(expected, this.fragment.getPadding_length());
    }

    @Test
    void getPadding_bytes() {
        assertArrayEquals(this.padding_size_bytes, this.fragment.getPadding_bytes());
    }

    @Test
    void toBytes() {
        try {
            assertEquals(Fragment.FRAGMENT_LENGTH, this.fragment.toBytes().length);

            Fragment copy = new Fragment(this.fragment.toBytes());

            assertArrayEquals(this.fragment.toBytes(), copy.toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}