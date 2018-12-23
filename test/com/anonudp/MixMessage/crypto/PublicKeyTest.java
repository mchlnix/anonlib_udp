package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PublicKeyTest extends TestCase {

    @DisplayName("Import and export are compatible")
    @Test
    void toBytes() {
        PrivateKey random = new PrivateKey();

        PublicKey key = new PublicKey(random);

        assertEquals(key, PublicKey.fromBytes(key.toBytes()));
    }
}