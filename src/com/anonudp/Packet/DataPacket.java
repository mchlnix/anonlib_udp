package com.anonudp.Packet;

import com.anonudp.MixMessage.crypto.Counter;

import java.util.Arrays;

public class DataPacket implements Packet
{
    private final byte[] channelID;

    private final byte[] byteArray;
    private byte[] ctrPrefix;
    private byte[] encryptedData;

    public DataPacket(byte[] channelID, byte[] fragment)
    {
        this.channelID = channelID;

        this.byteArray = fragment;
        this.ctrPrefix = null;
        this.encryptedData = null;
    }

    public byte[] toBytes()
    {
        return this.byteArray;
    }

    @Override
    public byte[] getCTRPrefix()
    {
        if (this.ctrPrefix == null)
            this.ctrPrefix = Arrays.copyOf(this.byteArray, Counter.CTR_PREFIX_SIZE);

        return this.ctrPrefix;
    }

    @Override
    public byte[] getChannelID() {
        return this.channelID;
    }

    @Override
    public byte[] getData()
    {
        if (this.encryptedData == null)
            this.encryptedData = Arrays.copyOfRange(this.byteArray, Counter.CTR_PREFIX_SIZE, this.byteArray.length);

        return this.encryptedData;
    }

    @Override
    public byte getPacketType() {
        return Packet.TYPE_DATA;
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
        return Arrays.equals(this.byteArray, otherPacket.toBytes());
    }
}