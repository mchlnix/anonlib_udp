package com.anonudp.MixPacket;

public class ProcessedDataPacket extends DataPacket
{

    public ProcessedDataPacket(byte[] channelID, byte[] ctrPrefix, byte[] fragment) {
        super(channelID, ctrPrefix, fragment);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataPacket)
            return ((DataPacket) obj).equals(this);

        return super.equals(obj);
    }
}
