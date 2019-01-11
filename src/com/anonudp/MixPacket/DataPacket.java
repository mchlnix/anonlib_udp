package com.anonudp.MixPacket;

import java.util.Arrays;

import static com.anonudp.MixMessage.Fragment.DATA_OVERHEAD;
import static com.anonudp.MixMessage.Fragment.SIZE_DATA;

public class DataPacket implements IPacket
{
    public static final int SIZE = DATA_OVERHEAD + SIZE_DATA;
    private final byte[] channelID;

    private final byte[] messageID;
    private final byte[] encryptedData;

    public DataPacket(byte[] channelID, byte[] messageID, byte[] payload) {
        this.channelID = channelID;

        this.messageID = messageID;
        this.encryptedData = payload;
    }

    @Override
    public byte[] getMessageID()
    {
        return this.messageID;
    }

    @Override
    public byte[] getChannelID() {
        return this.channelID;
    }

    @Override
    public byte[] getData()
    {
        return this.encryptedData;
    }

    @Override
    public byte getPacketType() {
        return IPacket.TYPE_DATA;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataPacket)
            return this.equals((DataPacket) obj);

        return super.equals(obj);
    }

    boolean equals(DataPacket otherPacket)
    {
        // todo can this be more performant?
        return Arrays.equals(this.encryptedData, otherPacket.getData());
    }
}