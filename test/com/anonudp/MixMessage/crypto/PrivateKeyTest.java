package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyTest extends TestCase {

    @DisplayName("Random Key-Generation")
    @Test
    void randomKeys()
    {
        int iterations = 100;

        PrivateKey previousKey = new PrivateKey();
        PrivateKey thisKey;

        for (int i = 0; i < iterations; ++i)
        {
            thisKey = new PrivateKey();

            assertNotEquals(previousKey, thisKey);

            previousKey = thisKey;
        }
    }
}