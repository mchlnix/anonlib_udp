package com.anonudp.MixMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

class FragmentPool implements Iterator<byte[]> {
    private final HashMap<Integer, Message> messages;

    public FragmentPool()
    {
        this.messages = new HashMap<>();
    }

    public void addFragment(Fragment fragment)
    {
        int message_id = fragment.getMessage_id();

        if (! this.messages.containsKey(message_id))
            this.messages.put(message_id, new Message(message_id));

        this.messages.get(message_id).addFragment(fragment);
    }

    public int size()
    {
        return this.messages.size();
    }

    @Override
    public boolean hasNext() {
        for (int message_id : this.messages.keySet())
        {
            if (this.messages.get(message_id).isDone())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public byte[] next() {
        for (int message_id : this.messages.keySet())
        {
            if (this.messages.get(message_id).isDone())
            {
                return this.messages.remove(message_id).getPayload();
            }
        }

        throw new NoCompleteMessagesException();
    }

    public class NoCompleteMessagesException extends NoSuchElementException
    {
        NoCompleteMessagesException() {
            super();
        }
    }
}
