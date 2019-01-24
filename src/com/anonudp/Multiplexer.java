package com.anonudp;

import com.anonudp.Exception.ChannelNotFound;
import com.anonudp.MixChannel.Channel;
import com.anonudp.MixChannel.IPv4AndPort;
import com.anonudp.MixChannel.IPv4Pair;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.EncryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.LinkEncryption;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

import static com.anonudp.MixMessage.Util.bytesToUnsignedInt;

public class Multiplexer {
    private DatagramSocket socketToMix;

    private LinkEncryption linkEncryption;

    private HashMap<IPv4Pair, Channel> channels;

    private TunDevice toUser;

    public Multiplexer(TunDevice tunDevice, IPv4AndPort firstMix) throws SocketException
    {
        this.toUser = tunDevice;

        this.socketToMix = new DatagramSocket();
        this.socketToMix.connect(firstMix.getHost(), firstMix.getPort());

        Receiver receiver = new Receiver(this.socketToMix);

        new Thread(receiver).start();

        this.linkEncryption = new LinkEncryption(new byte[EccGroup713.SYMMETRIC_KEY_LENGTH]);

        this.channels = new HashMap<>();
    }

    public void addChannel(IPv4AndPort source, IPv4AndPort destination, PublicKey[] mixKeys) throws IOException
    {
        Channel channel = new Channel(destination, mixKeys);

        this.channels.put(new IPv4Pair(source, destination), channel);
    }

    public void sendToMix(IPv4AndPort source, IPv4AndPort destination, byte[] payload) throws EncryptionFailed, PacketCreationFailed, IOException, ChannelNotFound {
        Channel channel = channels.get(new IPv4Pair(source, destination));

        if (channel == null)
            throw new ChannelNotFound("Channel for " + source + " " + destination + " not found.");

        DatagramPacket mixPacket;

        for ( IPacket packet: channel.request(payload))
        {
            byte[] bytes = this.linkEncryption.encrypt(packet);

            mixPacket = new DatagramPacket(bytes, bytes.length);

            this.socketToMix.send(mixPacket);
        }
    }

    private void receivedPacketToUser(byte[] payload)
    {
        /*
         * Prepare the UDP packet with header etc.
         */

        this.toUser.receiveResponse(payload);
    }

    private void packetToChannel(IPacket mixPacket) {
        int channelID = bytesToUnsignedInt(mixPacket.getChannelID());

        Channel channel;
        try
        {
            channel = Channel.table.get(channelID);
            channel.response(mixPacket);

            if (channel.hasNext())
                this.receivedPacketToUser(channel.next());
        }
        catch (NullPointerException npe)
        {
            System.out.println("The response mix packet is for a channel not known to the multiplexer. Invalid channel id.");
        } catch (DecryptionFailed df) {
            df.printStackTrace();
        }
    }

    class Receiver implements Runnable
    {
        final DatagramSocket fromMix;
        boolean shouldRun = true;

        Receiver(DatagramSocket socketToMix)
        {
            this.fromMix = socketToMix;
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[LinkEncryption.OVERHEAD + DataPacket.SIZE];

            while(this.shouldRun)
            {
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);

                try {
                    fromMix.receive(response);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                IPacket responseMixPacket;
                try {
                    responseMixPacket = linkEncryption.decrypt(response.getData());

                    if (responseMixPacket.getPacketType() != IPacket.TYPE_INIT_RESPONSE)
                        packetToChannel(responseMixPacket);

                }
                catch (DecryptionFailed decryptionFailed) {
                    decryptionFailed.printStackTrace();
                }
            }
        }

        public void stop()
        {
            this.shouldRun = false;
        }
    }
}
