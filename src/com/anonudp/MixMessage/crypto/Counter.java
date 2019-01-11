package com.anonudp.MixMessage.crypto;

import java.nio.ByteBuffer;

public class Counter {
    public static final int SIZE = Integer.BYTES;

    private int currentValue;

    public Counter()
    {
        this(0);
    }

    public Counter(int startValue)
    {
        this.currentValue = startValue;
    }

    public Counter(byte[] prefix)
    {
        this(ByteBuffer.wrap(prefix).getInt());
    }

    public int asInt()
    {
        return this.currentValue;
    }

    public void count()
    {
        ++this.currentValue;
    }

    public byte[] asBytes()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.putInt(this.currentValue);

        return buffer.array();
    }

    public byte[] asIV()
    {
        ByteBuffer buffer = ByteBuffer.allocate(Util.IV_SIZE);
        buffer.putInt(this.currentValue);

        return buffer.array();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Counter)
        {
            return this.currentValue == ((Counter) obj).asInt();
        }

        return super.equals(obj);
    }
}
