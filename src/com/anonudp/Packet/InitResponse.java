package com.anonudp.Packet;

import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.EccGroup713;

public class InitResponse implements Packet {
    private byte[] channelID;
    private byte[] data;

    private byte[] prefix;

    public InitResponse(byte[] channelID, byte[] data)
    {
        this.channelID = channelID;
        this.data = data;

        this.prefix = Util.randomBytes(EccGroup713.symmetricKeyLength);
    }
    @Override
    public byte getPacketType() {
        return Packet.TYPE_INIT_RESPONSE;
    }

    @Override
    public byte[] getCTRPrefix() {
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
