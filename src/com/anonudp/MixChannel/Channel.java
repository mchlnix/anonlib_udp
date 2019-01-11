package com.anonudp.MixChannel;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.FragmentPool;
import com.anonudp.MixMessage.crypto.*;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.PacketFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

/*
TODO: implement Channel Timeout
TODO: Use Short for Channel-ID?
 */
public class Channel implements Iterator<byte[]> {
    public static final int ID_SIZE = 2; // byte
    static final int HIGHEST_ID = Double.valueOf(Math.pow(2, Byte.SIZE * ID_SIZE) - 1).intValue();
    private static final HashMap<Integer, Channel> table = new HashMap<>();

    private PacketFactory packetFactory;

    private LinkEncryption linkCrypt;
    private Counter requestCounter;
    private ReplayDetection responseReplay;

    private boolean initialized;

    private FragmentPool fragmentPool;

    public Channel(IPv4AndPort source, IPv4AndPort destination, PublicKey[] mixPublicKeys) throws IOException {
        int id = Channel.randomID();
        byte[] idBytes = new byte[2];

        idBytes[0] = (byte) (id & 0xFF00);
        idBytes[1] = (byte) (id & 0x00FF);

        this.packetFactory = new PacketFactory(idBytes, destination.toBytes(), mixPublicKeys);

        this.linkCrypt = new LinkEncryption(new byte[EccGroup713.SYMMETRIC_KEY_LENGTH]);
        this.requestCounter = new Counter();
        this.responseReplay = new ReplayDetection();

        this.initialized = false;

        this.fragmentPool = new FragmentPool();

        Channel.table.put(id, this);
    }

    public byte[][] request(byte[] udpPayload) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        ArrayList<byte[]> returnPackets = new ArrayList<>();

        this.requestCounter.count();
        int fragmentNumber = 0;

        while (udpPayload.length > 0)
        {
            Fragment fragment;

            IPacket packet;

            if (this.initialized) {
                fragment = new Fragment(this.requestCounter.asInt(), fragmentNumber, udpPayload, Fragment.DATA_PAYLOAD_SIZE);

                packet = this.packetFactory.makeDataPacket(fragment);
            }
            else
            {
                fragment = new Fragment(this.requestCounter.asInt(), fragmentNumber, udpPayload, Fragment.INIT_PAYLOAD_SIZE);

                packet = this.packetFactory.makeInitPacket(fragment);
            }

            returnPackets.add(this.linkCrypt.encrypt(packet));

            udpPayload = Arrays.copyOfRange(udpPayload, fragment.getPayload().length, udpPayload.length);

            ++fragmentNumber;
        }

        return returnPackets.toArray(new byte[0][]);
    }

    public void response(byte[] data) throws IOException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        IPacket plainText = this.linkCrypt.decrypt(data);

        if (! this.responseReplay.isValid(plainText.getCTRPrefix()))
            throw new IllegalStateException("Response Replay detected.");

        if (plainText.getPacketType() == IPacket.TYPE_INIT_RESPONSE) {
            this.initialized = true;
        }
        else {
            DataPacket packet = (DataPacket) plainText;

            for (byte[] channelKey: this.packetFactory.getResponseChannelKeys())
            {
                packet = packetFactory.process(packet, channelKey);
            }

            Fragment fragment = new Fragment(packet.getData());

            this.fragmentPool.addFragment(fragment);
        }
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

    static void _removeAllChannels()
    {
        Channel.table.clear();
    }

    void _setInitialized()
    {
        this.initialized = true;
    }

    /* Iterator methods */

    @Override
    public boolean hasNext() {
        return this.fragmentPool.hasNext();
    }

    @Override
    public byte[] next() {
        return this.fragmentPool.next();
    }
}
