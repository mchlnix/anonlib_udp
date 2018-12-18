package com.anonudp.MixMessage.test;

import com.anonudp.MixMessage.Fragment;
import junit.framework.TestCase;

public class FragmentTest extends TestCase {
    public FragmentTest() {
        super();
    }

    public void testPaddingLength()
    {
        byte[] payload = new byte[20];

        int payload_limit = 272;

        Fragment f = new Fragment(1, 0, true, payload, payload_limit);

        assertEquals(f.getPadding_length(), 251);
    }
}