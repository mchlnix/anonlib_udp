package com.anonudp.MixMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

class Message {
    private int id;
    private HashMap<Integer, byte[]> payloads;
    private int fragment_count;

    Message(int id)
    {
        this.id = id;
        this.payloads = new HashMap<>();
        this.fragment_count = 0;
    }

    void addFragment(Fragment fragment) {
        assert fragment.getMessage_id() == this.id;

        assert ! this.payloads.containsKey(fragment.getFragment_number());
        
        this.payloads.put(fragment.getFragment_number(), fragment.getPayload());
        
        if (fragment.isLast())
            this.fragment_count = fragment.getFragment_number() + 1;
    }

    boolean isDone()
    {
        return this.fragment_count > 0 && this.payloads.size() == this.fragment_count;
    }

    byte[] getPayload()
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
}
