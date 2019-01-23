package com.anonudp.MixChannel;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.FragmentPool;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixMessage.crypto.ReplayDetection;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.PacketFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/*
TODO: Detect the response message ids running out
TODO: implement Channel Timeout
TODO: Use Short for Channel-ID?
 */
public class Channel implements Iterator<byte[]> {
    public static final int ID_SIZE = 2; // byte
    static final int HIGHEST_ID = Double.valueOf(Math.pow(2, Byte.SIZE * ID_SIZE) - 1).intValue();
    public static final HashMap<Integer, Channel> table = new HashMap<>();

    private PacketFactory packetFactory;

    private Counter requestCounter;
    private ReplayDetection responseReplay;

    private boolean initialized;

    private FragmentPool fragmentPool;

    public Channel(IPv4AndPort destination, PublicKey[] mixPublicKeys) throws IOException {
        int id = Channel.randomID();
        byte[] idBytes = new byte[2];

        idBytes[0] = (byte) ((id & 0xFF00) >> 8);
        idBytes[1] = (byte) (id & 0x00FF);

        this.packetFactory = new PacketFactory(idBytes, destination.toBytes(), mixPublicKeys);

        this.requestCounter = new Counter();
        this.responseReplay = new ReplayDetection();

        this.initialized = false;

        this.fragmentPool = new FragmentPool();

        Channel.table.put(id, this);
    }

    public IPacket[] request(byte[] udpPayload) throws PacketCreationFailed {
        ArrayList<IPacket> returnPackets = new ArrayList<>();

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

            returnPackets.add(packet);

            udpPayload = Arrays.copyOfRange(udpPayload, fragment.getPayload().length, udpPayload.length);

            ++fragmentNumber;
        }

        return returnPackets.toArray(new IPacket[0]);
    }

    public void response(IPacket response) throws DecryptionFailed {
        if (! this.responseReplay.isValid(response.getMessageID()))
            throw new IllegalStateException("Response Replay detected.");

        if (response.getPacketType() == IPacket.TYPE_INIT_RESPONSE) {
            this.initialized = true;
        }
        else {
            DataPacket packet = (DataPacket) response;

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
