package com.anonudp.MixMessage.crypto;

import java.nio.ByteBuffer;

/*
TODO: Only use 4 Bytes as CTR_PREFIX_SIZE, or switch to BigInteger
 */

public class Counter {
    public static final int CTR_PREFIX_SIZE = 8;
    private static final int CTR_PREFIX_OFFSET = CTR_PREFIX_SIZE - Integer.BYTES;
    private static final int IV_LENGTH = 16;
    private static final int IV_OFFSET = CTR_PREFIX_OFFSET;

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
        // todo this only works for 4 Byte numbers?
        this(ByteBuffer.wrap(prefix).getInt(CTR_PREFIX_OFFSET));
    }

    public int asInt()
    {
        return this.currentValue;
    }

    public void count()
    {
        ++this.currentValue;
    }

    public byte[] asPrefix()
    {
        ByteBuffer buffer = ByteBuffer.allocate(CTR_PREFIX_SIZE);
        buffer.putInt(CTR_PREFIX_OFFSET, this.currentValue);

        return buffer.array();
    }

    public byte[] asIV()
    {
        ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH);
        buffer.putInt(IV_OFFSET, this.currentValue);

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
