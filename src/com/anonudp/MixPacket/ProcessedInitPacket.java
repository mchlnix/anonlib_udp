package com.anonudp.MixPacket;

import com.anonudp.MixMessage.crypto.PublicKey;

public class ProcessedInitPacket extends InitPacket
{
    private final byte[] requestChannelKey;
    private final byte[] responseChannelKey;

    public ProcessedInitPacket(byte[] channelID, byte[] messageID, byte[] requestChannelKey, byte[] responseChannelKey, PublicKey element, byte[] processedChannelOnion, byte[] processedPayloadOnion)
    {
        super(channelID, messageID, element, processedChannelOnion, processedPayloadOnion);

        this.requestChannelKey = requestChannelKey;
        this.responseChannelKey = responseChannelKey;
    }

    public byte[] getRequestChannelKey() {
        return requestChannelKey;
    }

    public byte[] getResponseChannelKey() {
        return responseChannelKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof InitPacket)
            return ((InitPacket) obj).equals(this);

        return super.equals(obj);
    }
}