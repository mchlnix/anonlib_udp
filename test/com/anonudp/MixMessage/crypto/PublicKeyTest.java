package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PublicKeyTest extends TestCase {

    @DisplayName("Import and export are compatible")
    @Test
    void toBytes() {
        PrivateKey random = new PrivateKey();

        PublicKey key = new PublicKey(random);

        assertEquals(key, PublicKey.fromBytes(key.toBytes()));
    }

    @DisplayName("Tests equality")
    @Test
    void equals()
    {
        PrivateKey random1 = new PrivateKey();
        PrivateKey random2 = new PrivateKey();

        PublicKey pub1 = new PublicKey(random1);
        PublicKey pub2 = new PublicKey(random2);

        assertNotEquals(pub1, pub2);
        assertEquals(pub1, pub1);
        assertEquals(pub2, pub2);

        PublicKey pub3 = new PublicKey(random2);
        assertNotEquals(pub1, pub3);
        assertEquals(pub2, pub3);
    }
}