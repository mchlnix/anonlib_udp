package com.anonudp.MixMessage.test;

import com.anonudp.MixMessage.Fragment;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MultipleFragmentTest extends TestCase {
    private Fragment fragment1;
    private Fragment fragment2;

    private int message_id = 1;
    private byte[] message_id_bytes1 = {0x00, 0x04};
    private byte[] message_id_bytes2 = {0x00, 0x07};
    private byte[] payload;
    private byte[] padding_size_bytes_frag1;
    private byte[] padding_size_bytes_frag2;

    @BeforeEach
    protected void setUp() {
        int payload_length = 500;

        this.payload = new byte[payload_length];

        Arrays.fill(this.payload, (byte) 0x01);

        int padding_end_bit = 0x80;

        this.padding_size_bytes_frag1 = new byte[0];
        this.padding_size_bytes_frag2 = new byte[]{(byte) (43 | padding_end_bit)};

        this.fragment1 = new Fragment(this.message_id, 0, false,
                this.payload, Fragment.FRAGMENT_DATA_PAYLOAD);
        this.fragment2 = new Fragment(this.message_id, 1, true,
                Arrays.copyOfRange(this.payload, this.fragment1.getPayload().length, this.payload.length),
                Fragment.FRAGMENT_DATA_PAYLOAD);
    }

    @Test
    void getMessage_id() {
        assertEquals(this.message_id, this.fragment1.getMessage_id());
        assertEquals(this.message_id, this.fragment2.getMessage_id());

        try {
            assertArrayEquals(this.message_id_bytes1, Arrays.copyOf(this.fragment1.toBytes(),
                    this.message_id_bytes1.length));
            assertArrayEquals(this.message_id_bytes2, Arrays.copyOf(this.fragment2.toBytes(),
                    this.message_id_bytes2.length));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void getFragment_number() {
        assertEquals(0, this.fragment1.getFragment_number());
        assertEquals(1, this.fragment2.getFragment_number());
    }

    @Test
    void isLast_fragment() {
        assertFalse(this.fragment1.isLast_fragment());
        assertTrue(this.fragment2.isLast_fragment());
    }

    @Test
    void getPayload() {
        assertEquals(Fragment.FRAGMENT_DATA_PAYLOAD, this.fragment1.getPayload().length);

        assertArrayEquals(Arrays.copyOf(this.payload, Fragment.FRAGMENT_DATA_PAYLOAD), this.fragment1.getPayload());

        assertEquals(this.payload.length - this.fragment1.getPayload().length, this.fragment2.getPayload().length);

        assertArrayEquals(Arrays.copyOfRange(this.payload, this.fragment1.getPayload().length, this.payload.length), this.fragment2.getPayload());
    }

    @Test
    void getPadding_length() {
        assertEquals(0, this.fragment1.getPadding_length());

        int expected_padding_length = Fragment.FRAGMENT_DATA_PAYLOAD - (this.payload.length % Fragment.FRAGMENT_DATA_PAYLOAD) - this.padding_size_bytes_frag2.length;

        assertEquals(expected_padding_length, this.fragment2.getPadding_length());
    }

    @Test
    void getPadding_bytes() {
        assertArrayEquals(this.padding_size_bytes_frag1, this.fragment1.getPadding_bytes());
        assertArrayEquals(this.padding_size_bytes_frag2, this.fragment2.getPadding_bytes());
    }

    @Test
    void toBytes() {
        try {
            assertEquals(Fragment.FRAGMENT_LENGTH, this.fragment1.toBytes().length);

            Fragment copy1 = new Fragment(this.fragment1.toBytes());

            assertArrayEquals(this.fragment1.toBytes(), copy1.toBytes());

            assertEquals(Fragment.FRAGMENT_LENGTH, this.fragment2.toBytes().length);

            Fragment copy2 = new Fragment(this.fragment2.toBytes());

            assertArrayEquals(this.fragment2.toBytes(), copy2.toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }    }
}