package com.anonudp.MixMessage;

import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.anonudp.MixMessage.Util.bytesToUnsignedInt;

class UtilTest extends TestCase {

    @DisplayName("Check bytes are turned into real unsigned numbers.")
    @Test
    void _bytesToUnsignedInt() {
        assertEquals(0, bytesToUnsignedInt(new byte[]{0x00}));
        assertEquals(1, bytesToUnsignedInt(new byte[]{0x01}));
        assertEquals(127, bytesToUnsignedInt(new byte[]{0x7F}));
        assertEquals(128, bytesToUnsignedInt(new byte[]{(byte) 0x80}));
        assertEquals(256, bytesToUnsignedInt(new byte[]{0x01, 0x00}));
        assertEquals(65535, bytesToUnsignedInt(new byte[]{(byte) 0xFF, (byte) 0xFF}));

    }
}