package com.anonudp.MixMessage;

import com.anonudp.MixMessage.crypto.Counter;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterTest extends TestCase {

    private int countValue;
    private byte[] countPrefix;
    private byte[] countIV;

    @BeforeEach
    protected void setUp()
    {
        countValue = 0x1010;
        countPrefix = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x10};
        countIV = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    }

    @DisplayName("Counter From Prefix")
    @Test
    void constructors()
    {
        Counter counterInt = new Counter(countValue);
        Counter counterPrefix = new Counter(countPrefix);

        assertEquals(counterInt, counterPrefix);

        counterInt.count();
        counterPrefix.count();

        assertEquals(counterInt, counterPrefix);
    }

    @DisplayName("Count up")
    @Test
    void count() {
        Counter counter = new Counter();

        for (int i = 0; i < 2000; ++i)
        {
            assertEquals(i, counter.getCurrentValue());

            counter.count();
        }
    }

    @DisplayName("To AES-CTR-Prefix")
    @Test
    void asPrefix() {
        assertArrayEquals(countPrefix, new Counter(this.countValue).asPrefix());
    }

    @DisplayName("To AES-CTR-IV")
    @Test
    void asIV() {
        assertArrayEquals(countIV, new Counter(this.countValue).asIV());
    }
}