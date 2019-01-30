package com.anonudp.MixMessage;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FragmentPoolTest extends TestCase {
    private FragmentPool pool;
    private byte[] single_fragment_payload;
    private Fragment single_fragment;

    private byte[] double_fragment_payload;
    private Fragment double_fragment_part1;
    private Fragment double_fragment_part2;

    @BeforeEach
    protected void setUp() {
        this.pool = new FragmentPool();

        this.single_fragment_payload = new byte[30];
        Arrays.fill(single_fragment_payload, (byte) 0x02);

        this.double_fragment_payload = new byte[500];
        Arrays.fill(double_fragment_payload, (byte) 0x01);

        int message_id = 10;

        this.single_fragment = new Fragment(Fragment.SINGLE_FRAGMENT_MESSAGE_ID, Fragment.SINGLE_FRAGMENT_FRAGMENT_NUMBER, this.single_fragment_payload, Fragment.DATA_PAYLOAD_SIZE);

        this.double_fragment_part1 = new Fragment(message_id, 0,
                this.double_fragment_payload, Fragment.DATA_PAYLOAD_SIZE);

        this.double_fragment_part2 = new Fragment(message_id, 1,
                Arrays.copyOfRange(this.double_fragment_payload, this.double_fragment_part1.getPayload().length, this.double_fragment_payload.length),
                Fragment.DATA_PAYLOAD_SIZE);
    }

    @DisplayName("Exception on adding duplicate fragments")
    @Test
    void addFragmentThrows()
    {
        this.pool.addFragment(this.single_fragment);

        assertEquals(1, this.pool.size());

        assertTrue(this.pool.hasNext());

        assertThrows(Message.DuplicateFragmentException.class, () -> this.pool.addFragment(this.single_fragment));
    }

    @DisplayName("Iterator - hasNext()")
    @Test
    void hasNext() {
        assertEquals(0, this.pool.size());
        assertFalse(this.pool.hasNext());

        this.pool.addFragment(this.single_fragment);

        assertEquals(1, this.pool.size());
        assertTrue(this.pool.hasNext());

        this.pool.addFragment(this.double_fragment_part1);

        assertEquals(2, this.pool.size());
        assertTrue(this.pool.hasNext());

        this.pool.addFragment(this.double_fragment_part2);

        assertEquals(2, this.pool.size());
        assertTrue(this.pool.hasNext());

        this.pool.next();

        assertEquals(1, this.pool.size());
        assertTrue(this.pool.hasNext());

        this.pool.next();

        assertEquals(0, this.pool.size());
        assertFalse(this.pool.hasNext());
    }

    @DisplayName("Iterator - hasNext() is false for incomplete payloads")
    @Test
    void hasNextIncomplete() {
        this.pool.addFragment(this.double_fragment_part1);

        assertFalse(this.pool.hasNext());

        this.pool.addFragment(this.double_fragment_part2);

        assertTrue(this.pool.hasNext());
    }

    @DisplayName("Iterator - next()")
    @Test
    void next() {
        this.pool.addFragment(this.single_fragment);

        assertTrue(this.pool.hasNext());
        assertArrayEquals(this.single_fragment_payload, this.pool.next());

        this.pool.addFragment(this.double_fragment_part1);
        this.pool.addFragment(this.double_fragment_part2);

        assertArrayEquals(this.double_fragment_payload, this.pool.next());

        assertThrows(FragmentPool.NoCompleteMessagesException.class, () -> this.pool.next());
    }
}