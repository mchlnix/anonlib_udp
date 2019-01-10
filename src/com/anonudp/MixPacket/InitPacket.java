package com.anonudp.MixPacket;

import com.anonudp.MixChannel.IPv4AndPort;
import com.anonudp.MixMessage.crypto.PublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.anonudp.Constants.MIX_SERVER_COUNT;
import static com.anonudp.MixMessage.crypto.EccGroup713.SYMMETRIC_KEY_LENGTH;

public class InitPacket implements IPacket
{
    public static final int CHANNEL_KEY_ONION_SIZE = 2 * SYMMETRIC_KEY_LENGTH * MIX_SERVER_COUNT;
    public static final int PAYLOAD_SIZE = IPv4AndPort.SIZE;

    private final byte[] channelID;
    private byte[] counterPrefix;

    private final PublicKey publicKey;
    private final byte[] channelKeyOnion;
    private final byte[] payloadOnion;

    public InitPacket(byte[] channelID, byte[] counterPrefix, PublicKey publicKey, byte[] channelKeyOnion, byte[] payloadOnion)
    {
        this.channelID = channelID;

        this.counterPrefix = counterPrefix;

        this.publicKey = publicKey;
        this.channelKeyOnion = channelKeyOnion;
        this.payloadOnion = payloadOnion;
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
        return this.counterPrefix;
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
        if (obj instanceof InitPacket)
            return this.equals((InitPacket) obj);

        return super.equals(obj);
    }

    boolean equals(InitPacket otherPacket)
    {
        boolean is_equal = Arrays.equals(this.channelID, otherPacket.getChannelID());
        is_equal = is_equal && Arrays.equals(this.counterPrefix, otherPacket.getCTRPrefix());
        is_equal = is_equal && this.publicKey == otherPacket.getPublicKey();
        is_equal = is_equal && this.channelKeyOnion == otherPacket.getChannelKeyOnion();
        is_equal = is_equal &&  this.payloadOnion == otherPacket.getPayloadOnion();

        return is_equal;
    }
}