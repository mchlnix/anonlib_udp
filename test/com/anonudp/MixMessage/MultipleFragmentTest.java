package com.anonudp.MixMessage;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class MultipleFragmentTest extends TestCase {
    private Fragment fragment1;
    private Fragment fragment2;

    private int message_id;
    private byte[] message_id_bytes1;
    private byte[] message_id_bytes2;
    private byte[] payload;
    private byte[] padding_size_bytes_frag1;
    private byte[] padding_size_bytes_frag2;

    @BeforeEach
    protected void setUp() {
        this.message_id = 1;
        this.message_id_bytes1 = new byte[]{0x00, 0x04};
        this.message_id_bytes2 = new byte[]{0x00, 0x07};

        int payload_length = 500;

        this.payload = new byte[payload_length];

        Arrays.fill(this.payload, (byte) 0x01);

        this.padding_size_bytes_frag1 = new Padding(0).getLengthAsBytes();

        this.padding_size_bytes_frag2 = new Padding(44).getLengthAsBytes();

        this.fragment1 = new Fragment(this.message_id, 0,
                this.payload, Fragment.DATA_PAYLOAD_SIZE);

        this.fragment2 = new Fragment(this.message_id, 1,
                Arrays.copyOfRange(this.payload, this.fragment1.getPayload().length, this.payload.length),
                Fragment.DATA_PAYLOAD_SIZE);
    }

    @DisplayName("Message id is correctly encoded")
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

    @DisplayName("Fragment number is correctly counted up")
    @Test
    void getFragment_number() {
        assertEquals(0, this.fragment1.getFragment_number());
        assertEquals(1, this.fragment2.getFragment_number());
    }

    @DisplayName("Last fragment is correctly identified")
    @Test
    void isLast_fragment() {
        assertFalse(this.fragment1.isLast());
        assertTrue(this.fragment2.isLast());
    }

    @DisplayName("Payload is correctly split up and returned")
    @Test
    void getPayload() {
        assertEquals(Fragment.DATA_PAYLOAD_SIZE, this.fragment1.getPayload().length);

        assertArrayEquals(Arrays.copyOf(this.payload, Fragment.DATA_PAYLOAD_SIZE), this.fragment1.getPayload());

        assertEquals(this.payload.length - this.fragment1.getPayload().length, this.fragment2.getPayload().length);

        assertArrayEquals(Arrays.copyOfRange(this.payload, this.fragment1.getPayload().length, this.payload.length), this.fragment2.getPayload());
    }

    @DisplayName("Padding length is correctly calculated")
    @Test
    void getPadding_length() {
        assertEquals(0, this.fragment1.getPadding_length());

        int expected_padding_length = Fragment.DATA_PAYLOAD_SIZE - (this.payload.length % Fragment.DATA_PAYLOAD_SIZE) - this.padding_size_bytes_frag2.length;

        assertEquals(expected_padding_length, this.fragment2.getPadding_length());
    }

    @DisplayName("Padding size bytes are correctly encoded")
    @Test
    void getPadding_bytes() {
        assertArrayEquals(this.padding_size_bytes_frag1, this.fragment1.getPadding_bytes());
        assertArrayEquals(this.padding_size_bytes_frag2, this.fragment2.getPadding_bytes());
    }

    @DisplayName("From/ToBytes conversion")
    @Test
    void toBytes() {
        try {
            assertEquals(Fragment.SIZE_DATA, this.fragment1.toBytes().length);

            Fragment copy1 = new Fragment(this.fragment1.toBytes());

            assertArrayEquals(this.fragment1.toBytes(), copy1.toBytes());

            assertEquals(Fragment.SIZE_DATA, this.fragment2.toBytes().length);

            Fragment copy2 = new Fragment(this.fragment2.toBytes());

            assertArrayEquals(this.fragment2.toBytes(), copy2.toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }    }
}