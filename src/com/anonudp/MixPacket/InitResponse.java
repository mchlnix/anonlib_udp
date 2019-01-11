package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.EccGroup713;

public class InitResponse implements IPacket {
    private final byte[] channelID;
    private final byte[] data;

    private final byte[] prefix;

    public InitResponse(byte[] channelID, byte[] data)
    {
        this.channelID = channelID;
        this.data = data;

        this.prefix = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);
    }
    @Override
    public byte getPacketType() {
        return IPacket.TYPE_INIT_RESPONSE;
    }

    @Override
    public byte[] getMessageID() {
        return this.prefix;
    }

    @Override
    public byte[] getChannelID() {
        return this.channelID;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }
}
