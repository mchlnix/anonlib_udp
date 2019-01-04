package com.anonudp.MixMessage.crypto;

import java.nio.ByteBuffer;

public class Counter implements Comparable<Counter> {
    public static final int CTR_PREFIX_SIZE = Integer.BYTES;
    private static final int IV_LENGTH = 16;

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
        ByteBuffer buffer = ByteBuffer.allocate(CTR_PREFIX_SIZE);
        buffer.putInt(this.currentValue);

        return buffer.array();
    }

    public byte[] asIV()
    {
        ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH);
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

    @Override
    public int compareTo(Counter counter) {
        return 0;
    }
}
