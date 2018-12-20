package com.anonudp.MixMessage;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PaddingIntTest extends TestCase {
    private Padding case0;
    private Padding case1;
    private Padding case2;
    private Padding case128;
    private Padding case129;
    private Padding case130;
    private Padding case257;
    private Padding case258;
    private Padding case272;

    @BeforeEach
    protected void setUp() {
        case0 = new Padding(0);
        case1 = new Padding(1);
        case2 = new Padding(2);
        case128 = new Padding(128);
        case129 = new Padding(129);
        case130 = new Padding(130);
        case257 = new Padding(257);
        case258 = new Padding(258);
        case272 = new Padding(272);
    }

    @DisplayName("Calculate correct padding length bytes")
    @Test
    void getLengthAsBytes() {
        assertArrayEquals(new byte[0], case0.getLengthAsBytes());
        assertArrayEquals(new byte[]{(byte) 0x80}, case1.getLengthAsBytes());
        assertArrayEquals(new byte[]{(byte) 0x81}, case2.getLengthAsBytes());
        assertArrayEquals(new byte[]{(byte) 0xFF}, case128.getLengthAsBytes());
        assertArrayEquals(new byte[]{0x00, (byte) 0xFF}, case129.getLengthAsBytes());
        assertArrayEquals(new byte[]{0x01, (byte) 0x80}, case130.getLengthAsBytes());
        assertArrayEquals(new byte[]{0x01, (byte) 0xFF}, case257.getLengthAsBytes());
        assertArrayEquals(new byte[]{0x02, (byte) 0x80}, case258.getLengthAsBytes());
        assertArrayEquals(new byte[]{0x02, (byte) 0x8E}, case272.getLengthAsBytes());
    }

    @DisplayName("Return the correct amount of padding bytes")
    @Test
    void getPaddingBytes() {
        assertEquals(0, case0.getPaddingBytes().length);
        assertEquals(0, case1.getPaddingBytes().length);
        assertEquals(1, case2.getPaddingBytes().length);
        assertEquals(127, case128.getPaddingBytes().length);
        assertEquals(127, case129.getPaddingBytes().length);
        assertEquals(128, case130.getPaddingBytes().length);
        assertEquals(255, case257.getPaddingBytes().length);
        assertEquals(256, case258.getPaddingBytes().length);
        assertEquals(270, case272.getPaddingBytes().length);
    }

    @DisplayName("Return the correct amount of necessary padding")
    @Test
    void getLength() {
        assertEquals(0, case0.getLength());
        assertEquals(0, case1.getLength());
        assertEquals(1, case2.getLength());
        assertEquals(127, case128.getLength());
        assertEquals(127, case129.getLength());
        assertEquals(128, case130.getLength());
        assertEquals(255, case257.getLength());
        assertEquals(256, case258.getLength());
        assertEquals(270, case272.getLength());
    }
}