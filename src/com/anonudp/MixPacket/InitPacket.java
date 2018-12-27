package com.anonudp.MixPacket;

import com.anonudp.MixChannel.IPv4AndPort;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.anonudp.Constants.MIX_SERVER_COUNT;

public class InitPacket implements IPacket
{
    public static final int PAYLOAD_SIZE = IPv4AndPort.SIZE;
    private final byte[] channelID;

    private final PublicKey publicKey;
    private final byte[] channelKeyOnion;
    private final byte[] payloadOnion;

    public InitPacket(byte[] channelID, PublicKey publicKey, byte[] channelKeyOnion, byte[] payloadOnion)
    {
        this.channelID = channelID;

        this.publicKey = publicKey;
        this.channelKeyOnion = channelKeyOnion;
        this.payloadOnion = payloadOnion;
    }

    public InitPacket(byte[] channelID, byte[] data)
    {
        this.channelID = channelID;

        int offset = 0;

        this.publicKey = PublicKey.fromBytes(Arrays.copyOf(data, PublicKey.SIZE));

        offset += PublicKey.SIZE;

        this.channelKeyOnion = Arrays.copyOfRange(data, offset, offset + EccGroup713.SYMMETRIC_KEY_LENGTH * MIX_SERVER_COUNT);

        offset += EccGroup713.SYMMETRIC_KEY_LENGTH * MIX_SERVER_COUNT;

        this.payloadOnion = Arrays.copyOfRange(data, offset, data.length);
    }

    PublicKey getPublicKey() {
        return publicKey;
    }

    byte[] getChannelKeyOnion() {
        return channelKeyOnion;
    }

    public byte[] getPayloadOnion() {
        return payloadOnion;
    }

    private byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(publicKey.toBytes());

        bos.write(channelKeyOnion);

        bos.write(payloadOnion);

        return bos.toByteArray();
    }

    @Override
    public byte getPacketType() {
        return IPacket.TYPE_INIT;
    }

    @Override
    public byte[] getCTRPrefix() {
        return Util.randomBytes(Counter.CTR_PREFIX_SIZE);
    }

    @Override
    public byte[] getChannelID() {
        return this.channelID;
    }

    @Override
    public byte[] getData() throws IOException {
        return this.toBytes();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessedInitPacket)
            return this.equals((ProcessedInitPacket) obj);

        return super.equals(obj);
    }

    boolean equals(ProcessedInitPacket otherPacket)
    {
        boolean is_equal = this.publicKey == otherPacket.getPublicKey();
        is_equal = is_equal && this.channelKeyOnion == otherPacket.getChannelKeyOnion();
        is_equal = is_equal &&  this.payloadOnion == otherPacket.getPayloadOnion();

        return is_equal;
    }
}