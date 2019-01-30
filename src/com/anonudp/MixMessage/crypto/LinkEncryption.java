package com.anonudp.MixMessage.crypto;

import com.anonudp.MixChannel.Channel;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.EncryptionFailed;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.InitResponse;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.AEADBlockCipher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.anonudp.MixMessage.crypto.Util.GCM_MAC_SIZE;

public class LinkEncryption {
    private static final int HEADER_SIZE = Channel.ID_SIZE + Counter.SIZE + IPacket.TYPE_BYTE_SIZE;
    public static final int OVERHEAD = Counter.SIZE + HEADER_SIZE + GCM_MAC_SIZE;

    private final byte[] key;
    private final Counter counter;

    public LinkEncryption(byte[] linkKey)
    {
        this.key = linkKey;
        this.counter = new Counter();
    }

    public byte[] encrypt(IPacket packet) throws EncryptionFailed {
        this.counter.count();

        AEADBlockCipher gcm = Util.createGCM(this.key, this.counter.asIV(), true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            bos.write(this.counter.asBytes());

            byte[] out = new byte[DataPacket.SIZE];
            byte[] in = packet.getChannelID();

            int outOffset = gcm.processBytes(in, 0, in.length, out, 0);

            in = packet.getMessageID();

            outOffset += gcm.processBytes(in, 0, in.length, out, outOffset);
            outOffset += gcm.processByte(packet.getPacketType(), out, outOffset);
            outOffset += gcm.doFinal(out, outOffset);

            bos.write(out, 0, outOffset);

            bos.write(packet.getData());
        }
        catch (IOException | InvalidCipherTextException e)
        {
            throw new EncryptionFailed("Couldn't link-encrypt the mix packet " + this.counter.asInt() + ".", e);
        }

        return bos.toByteArray();

    }

    public IPacket decrypt(byte[] packetBytes) throws DecryptionFailed {
        Counter linkPrefix = new Counter(packetBytes);

        AEADBlockCipher gcm = Util.createGCM(this.key, linkPrefix.asIV(), false);

        ByteArrayInputStream bis = new ByteArrayInputStream(packetBytes);

        byte[] linkCounter = new byte[Counter.SIZE];
        byte[] cipherTextAndMac = new byte[HEADER_SIZE + GCM_MAC_SIZE];
        byte[] payload = new byte[packetBytes.length - OVERHEAD];

        try {
            assert bis.read(linkCounter) == linkCounter.length;
            assert bis.read(cipherTextAndMac) == cipherTextAndMac.length;
            assert bis.read(payload) == payload.length;

            bis.close();

            byte[] plainLinkHeader = new byte[cipherTextAndMac.length];
            int outOffset = gcm.processBytes(cipherTextAndMac, 0, cipherTextAndMac.length, plainLinkHeader, 0);

            outOffset += gcm.doFinal(plainLinkHeader, outOffset);

            bis = new ByteArrayInputStream(plainLinkHeader, 0, outOffset);

            byte[] channelID = new byte[Channel.ID_SIZE];
            byte[] messagePrefix = new byte[Counter.SIZE];
            byte messageType;

            assert bis.read(channelID) == channelID.length;
            assert bis.read(messagePrefix) == messagePrefix.length;

            messageType = (byte) bis.read();

            IPacket returnPacket = null;

            if (messageType == IPacket.TYPE_DATA)
                returnPacket = new DataPacket(channelID, messagePrefix, payload);
            else if (messageType == IPacket.TYPE_INIT_RESPONSE)
                returnPacket = new InitResponse(channelID, payload);

            return returnPacket;
        }
        catch (IOException | InvalidCipherTextException e)
        {
            throw new DecryptionFailed("Couldn't link-decrypt the mix packet " + linkPrefix.asInt() + ".", e);
        }
    }
}
