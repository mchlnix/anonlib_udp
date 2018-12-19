package com.anonudp.MixMessage.test;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.FragmentPool;
import com.anonudp.MixMessage.Message;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
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

        this.single_fragment = new Fragment(Fragment.SINGLE_FRAGMENT_MESSAGE_ID, Fragment.SINGLE_FRAGMENT_FRAGMENT_NUMBER, this.single_fragment_payload, Fragment.FRAGMENT_DATA_PAYLOAD);

        this.double_fragment_part1 = new Fragment(message_id, 0,
                this.double_fragment_payload, Fragment.FRAGMENT_DATA_PAYLOAD);

        this.double_fragment_part2 = new Fragment(message_id, 1,
                Arrays.copyOfRange(this.double_fragment_payload, this.double_fragment_part1.getPayload().length, this.double_fragment_payload.length),
                Fragment.FRAGMENT_DATA_PAYLOAD);
    }

    @Test
    void addFragment() {
        this.pool.addFragment(this.single_fragment);

        assertEquals(1, this.pool.size());

        assertTrue(this.pool.hasNext());

        // test Duplicate Fragment Exception
        assertThrows(Message.DuplicateFragmentException.class, () -> this.pool.addFragment(this.single_fragment));

        byte[] expected_payload = this.single_fragment.getPayload();

        assertArrayEquals(expected_payload, this.pool.next());

        assertEquals(0, this.pool.size());
        assertFalse(this.pool.hasNext());

        this.pool.addFragment(this.double_fragment_part1);
        this.pool.addFragment(this.double_fragment_part2);

        assertEquals(1, this.pool.size());

        assertTrue(this.pool.hasNext());

        assertArrayEquals(this.double_fragment_payload, this.pool.next());

        assertEquals(0, this.pool.size());
        assertFalse(this.pool.hasNext());
    }

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

    @Test
    void next() {
        this.pool.addFragment(this.single_fragment);
        this.pool.addFragment(this.double_fragment_part1);
        this.pool.addFragment(this.double_fragment_part2);

        assertArrayEquals(this.single_fragment_payload, this.pool.next());
        assertArrayEquals(this.double_fragment_payload, this.pool.next());

        assertThrows(FragmentPool.NoCompleteMessagesException.class, () -> this.pool.next());
    }
}