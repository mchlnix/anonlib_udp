package com.anonudp.MixMessage.crypto;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReplayDetectionTest extends TestCase {
    private ReplayDetection rd;

    @BeforeEach
    protected void setUp()
    {
        this.rd = new ReplayDetection();
    }

    @DisplayName("Is initialized with zeros")
    @Test
    void initializedWithZeros()
    {
        ReplayDetection rd = new ReplayDetection();

        assertFalse(rd.isValid(0));
        assertTrue(rd.isValid(1));
    }

    @DisplayName("Adding new elements works only once")
    @Test
    void addingElements()
    {
        assertTrue(rd.isValid(1));
        assertFalse(rd.isValid(1));

        assertTrue(rd.isValid(2));
        assertFalse(rd.isValid(2));

        assertTrue(rd.isValid(200));
        assertFalse(rd.isValid(200));
    }

    @DisplayName("Size stays constant after checking elements")
    @Test
    void sizeIsConstant()
    {
        int rounds = 100;

        assertEquals(ReplayDetection.SIZE, rd.size());

        // successful
        for (int i = 1; i < rounds; ++i)
        {
            assertTrue(rd.isValid(i));

            assertEquals(ReplayDetection.SIZE, rd.size());
        }

        // unsuccessful
        for (int i = 0; i < rounds - ReplayDetection.SIZE; ++i)
        {
            assertFalse(rd.isValid(i));

            assertEquals(ReplayDetection.SIZE, rd.size());
        }
    }

    @DisplayName("Values that are too low are false")
    @Test
    void lowerBoundary()
    {
        int upperBound = 100;

        int lowerBound = upperBound - ReplayDetection.SIZE;

        for (int i = lowerBound; i < upperBound; ++i)
            assertTrue(rd.isValid(i));

        for (int i = 0; i < lowerBound; ++i)
            assertFalse(rd.isValid(i));
    }
}