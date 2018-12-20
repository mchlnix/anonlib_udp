package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.bouncycastle.math.ec.ECPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EccGroup713Test extends TestCase {

    @BeforeEach
    protected void setUp() {
    }

    @Test
    void powInGroup() {
        ECPoint base = EccGroup713.getGenerator();

        ECPoint same = EccGroup713.powInGroup(base, BigInteger.ONE);

        assertEquals(base, same);

        ECPoint powerOf2 = EccGroup713.powInGroup(base, BigInteger.TWO);

        assertEquals(base.twice(), powerOf2);

        ECPoint powerOf3 = EccGroup713.powInGroup(base, new BigInteger("3"));

        assertEquals(base.threeTimes(), powerOf3);

        ECPoint powerOf16 = EccGroup713.powInGroup(base, new BigInteger("16"));

        assertEquals(base.twice().twice().twice().twice(), powerOf16);

        assertNotEquals(base, powerOf2);
        assertNotEquals(powerOf2, powerOf3);
        assertNotEquals(powerOf3, powerOf16);
    }

    @Test
    void hb() {
        int arrayLength = EccGroup713.symmetricKeyLength;
        byte[] randomBytes = new byte[arrayLength];

        new SecureRandom().nextBytes(randomBytes);

        assertFalse(Arrays.equals(new byte[arrayLength], randomBytes));

        try {
            assertEquals(EccGroup713.hb(randomBytes), EccGroup713.hb(randomBytes));

            assertNotEquals(EccGroup713.hb(new byte[arrayLength]), EccGroup713.hb(randomBytes));
        } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            fail();
        }
    }
}