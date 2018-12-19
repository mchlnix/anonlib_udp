package com.anonudp.MixMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

class Message {
    private final int id;
    private final HashMap<Integer, byte[]> payloads;
    private int fragment_count;

    public Message(int id)
    {
        this.id = id;
        this.payloads = new HashMap<>();
        this.fragment_count = -1;
    }

    public void addFragment(Fragment fragment) {
        if (fragment.getMessage_id() != this.id)
            throw new MessageIdMismatchException(this.id, fragment.getMessage_id());

        if (this.payloads.containsKey(fragment.getFragment_number()))
            throw new DuplicateFragmentException(fragment.getFragment_number());
        
        this.payloads.put(fragment.getFragment_number(), fragment.getPayload());
        
        if (fragment.isLast())
            this.fragment_count = fragment.getFragment_number() + 1;
    }

    public boolean isDone()
    {
        return this.fragment_count > 0 && this.payloads.size() == this.fragment_count;
    }

    public byte[] getPayload()
    {
        if (! this.isDone())
            return new byte[0];

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            for (int i = 0; i < this.fragment_count; ++i)
            {
                bos.write(this.payloads.get(i));
            }

            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public class DuplicateFragmentException extends IllegalArgumentException
    {
        DuplicateFragmentException(int fragment_number)
        {
            super("Fragment number: " + fragment_number + " was already processed.");
        }
    }

    public class MessageIdMismatchException extends IllegalArgumentException
    {
        MessageIdMismatchException(int message_id, int fragment_id) {
            super("Id of this message: " + message_id + ". Fragment id: " + fragment_id);
        }
    }
}
