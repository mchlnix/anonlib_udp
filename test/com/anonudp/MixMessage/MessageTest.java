package com.anonudp.MixMessage;

import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageTest extends TestCase {

    private Message message;
    private int message_id;

    @BeforeEach
    protected void setUp() {
        this.message_id = 10;
        this.message = new Message(this.message_id);
    }

    @Test
    void addFragment() {
        byte[] payload1 = new byte[500];
        Fragment fragment1 = new Fragment(this.message_id, 0, payload1, Fragment.FRAGMENT_DATA_PAYLOAD);

        this.message.addFragment(fragment1);
        assertThrows(Message.DuplicateFragmentException.class, () -> this.message.addFragment(fragment1));


        byte[] payload2 = Arrays.copyOfRange(payload1, fragment1.getPayload().length, payload1.length);
        Fragment fragment2 = new Fragment(this.message_id + 1, 1, payload2, Fragment.FRAGMENT_DATA_PAYLOAD);

        assertThrows(Message.MessageIdMismatchException.class, () -> this.message.addFragment(fragment2));
    }

    @Test
    void isDone() {
        Fragment fragment = new Fragment(Fragment.SINGLE_FRAGMENT_MESSAGE_ID, Fragment.SINGLE_FRAGMENT_FRAGMENT_NUMBER, new byte[0], Fragment.FRAGMENT_DATA_PAYLOAD);

        Message message = new Message(Fragment.SINGLE_FRAGMENT_MESSAGE_ID);

        message.addFragment(fragment);

        assertTrue(message.isDone());
    }

    @Test
    void getPayload() {
        byte[] payload1 = new byte[500];
        Fragment fragment1 = new Fragment(this.message_id, 0, payload1, Fragment.FRAGMENT_DATA_PAYLOAD);

        this.message.addFragment(fragment1);


        byte[] payload2 = Arrays.copyOfRange(payload1, fragment1.getPayload().length, payload1.length);
        Fragment fragment2 = new Fragment(this.message_id, 1, payload2, Fragment.FRAGMENT_DATA_PAYLOAD);

        this.message.addFragment(fragment2);

        assertArrayEquals(payload1, this.message.getPayload());

        assertTrue(this.message.isDone());
    }
}