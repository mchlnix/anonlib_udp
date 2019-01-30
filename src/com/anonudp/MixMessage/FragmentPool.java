package com.anonudp.MixMessage;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FragmentPool implements Iterator<byte[]> {
    private final Hashtable messages;
    private Message nextMessage = null;

    public FragmentPool()
    {
        this.messages = new Hashtable();
    }

    public void addFragment(Fragment fragment)
    {
        int message_id = fragment.getMessage_id();

        Message message;

        if (! this.messages.containsKey(message_id))
        {
            message = new Message(message_id);
            this.messages.put(message_id, message);
        }
        else
        {
            message = (Message) this.messages.get(message_id);
        }

        message.addFragment(fragment);
    }

    int size()
    {
        return this.messages.size();
    }

    @Override
    public boolean hasNext() {
        if (this.nextMessage != null)
            return true;

        Enumeration tableElements = this.messages.elements();

        while (tableElements.hasMoreElements())
        {
            Message message = (Message) tableElements.nextElement();

            if (message.isDone())
            {
                this.nextMessage = message;

                return true;
            }
        }

        return false;
    }

    @Override
    public byte[] next() {
        if (this.nextMessage == null && !this.hasNext())
        {
            throw new NoCompleteMessagesException();
        }
        else
        {
            byte[] payload = this.nextMessage.getPayload();
            this.messages.remove(this.nextMessage.getId());

            this.nextMessage = null;

            return payload;
        }
    }

    class NoCompleteMessagesException extends NoSuchElementException
    {
        NoCompleteMessagesException() {
            super();
        }
    }
}
