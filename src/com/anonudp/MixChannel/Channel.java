package com.anonudp.MixChannel;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.LinkEncryption;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.Packet.DataPacketFactory;
import com.anonudp.Packet.InitPacket;
import com.anonudp.Packet.InitPacketFactory;
import com.anonudp.Packet.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Channel {
    public static int HIGHEST_ID = Double.valueOf(Math.pow(2, 16) - 1).intValue();
    private static HashMap<Integer, Channel> table = new HashMap<>();

    private InitPacketFactory initFactory;
    private DataPacketFactory dataFactory;

    private IPv4AndPort source;
    private IPv4AndPort destination;

    private int id;
    private byte[] idBytes;

    private LinkEncryption linkCrypt;
    private Counter requestCounter;

    private PublicKey[] mixKeys;
    private byte[][] channelKeys;

    private boolean initialized;

    public Channel(IPv4AndPort source, IPv4AndPort destination, PublicKey[] mixPublicKeys) throws IOException {
        this.source = source;
        this.destination = destination;

        this.mixKeys = mixPublicKeys;

        this.channelKeys = new byte[this.mixKeys.length][EccGroup713.symmetricKeyLength];

        for(int i = 0; i < this.mixKeys.length; ++i)
            this.channelKeys[i] = Util.randomBytes(EccGroup713.symmetricKeyLength);

        this.id = Channel.randomID();
        this.idBytes = new byte[2];

        this.idBytes[0] = (byte) (this.id & 0xFF00);
        this.idBytes[1] = (byte) (this.id & 0x00FF);

        this.initFactory = new InitPacketFactory(this.idBytes, this.destination.toBytes(), this.mixKeys);
        this.dataFactory = new DataPacketFactory(this.idBytes, this.channelKeys);

        this.linkCrypt = new LinkEncryption(new byte[EccGroup713.symmetricKeyLength]);
        this.requestCounter = new Counter();

        this.initialized = false;

        Channel.table.put(this.id, this);
    }

    public byte[] sendRequest(byte[] udpPayload) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        this.requestCounter.count();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(this.destination.toBytes());
        bos.write(udpPayload);

        Fragment fragment = new Fragment(this.requestCounter.asInt(), 0, bos.toByteArray(), Fragment.INIT_PAYLOAD_SIZE);

        InitPacket packet = this.initFactory.makePacket(this.channelKeys, fragment);

        return this.linkCrypt.encrypt(packet);
    }

    public byte[] getResponse(byte[] data) throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        Packet plainText = this.linkCrypt.decrypt(data);

        Fragment fragment;

        if (plainText.getPacketType() == Packet.TYPE_INIT_RESPONSE) {
            this.initialized = true;
            fragment = new Fragment();
        }
        else {
            fragment = new Fragment(plainText.getData());
        }

        return fragment.getPayload();
    }

    public boolean isInitialized()
    {
        return this.initialized;
    }

    private static int randomID()
    {
        if (table.size() == HIGHEST_ID)
            throw new IllegalStateException("No free channel ids available.");

        int channelID;

        do {
            channelID = ThreadLocalRandom.current().nextInt(0, HIGHEST_ID + 1);
        }
        while (Channel.table.containsKey(channelID));

        return channelID;
    }

    public static void removeAllChannels()
    {
        Channel.table.clear();
    }
}
