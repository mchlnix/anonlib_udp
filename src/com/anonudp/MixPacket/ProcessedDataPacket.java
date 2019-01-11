package com.anonudp.MixPacket;

public class ProcessedDataPacket extends DataPacket
{

    public ProcessedDataPacket(byte[] channelID, byte[] messageID, byte[] fragment) {
        super(channelID, messageID, fragment);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataPacket)
            return ((DataPacket) obj).equals(this);

        return super.equals(obj);
    }
}
