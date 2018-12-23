package com.anonudp.MixMessage;

import java.nio.ByteBuffer;

class Counter {
    static final int CTR_PREFIX_SIZE = 8;
    private static final int CTR_PREFIX_OFFSET = CTR_PREFIX_SIZE - Integer.BYTES;
    private static final int IV_LENGTH = 16;
    private static final int IV_OFFSET = CTR_PREFIX_OFFSET;

    private int currentValue;

    Counter()
    {
        this(0);
    }

    Counter(int startValue)
    {
        this.currentValue = startValue;
    }

    Counter(byte[] prefix)
    {
        this(ByteBuffer.wrap(prefix).getInt(CTR_PREFIX_OFFSET));

        assert prefix.length == CTR_PREFIX_SIZE;
    }

    int getCurrentValue()
    {
        return this.currentValue;
    }

    void count()
    {
        ++this.currentValue;
    }

    byte[] asPrefix()
    {
        ByteBuffer buffer = ByteBuffer.allocate(CTR_PREFIX_SIZE);
        buffer.putInt(CTR_PREFIX_OFFSET, this.currentValue);

        return buffer.array();
    }

    byte[] asIV()
    {
        ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH);
        buffer.putInt(IV_OFFSET, this.currentValue);

        return buffer.array();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Counter)
        {
            return this.currentValue == ((Counter) obj).getCurrentValue();
        }

        return super.equals(obj);
    }
}
