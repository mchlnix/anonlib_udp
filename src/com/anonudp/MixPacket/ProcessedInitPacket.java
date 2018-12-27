package com.anonudp.MixPacket;

import com.anonudp.MixMessage.crypto.PublicKey;

public class ProcessedInitPacket extends InitPacket
{
    private final byte[] channelKey;

    public ProcessedInitPacket(byte[] channelID, byte[] channelKey, PublicKey element, byte[] processedChannelOnion, byte[] processedPayloadOnion)
    {
        super(channelID, element, processedChannelOnion, processedPayloadOnion);

        this.channelKey = channelKey;
    }

    public byte[] getChannelKey()
    {
        return this.channelKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof InitPacket)
            return ((InitPacket) obj).equals(this);

        return super.equals(obj);
    }
}