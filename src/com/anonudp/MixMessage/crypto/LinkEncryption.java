package com.anonudp.MixMessage.crypto;

import com.anonudp.MixChannel.Channel;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.EncryptionFailed;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.InitResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
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

        Cipher gcm = Util.createGCM(this.key, this.counter.asIV(), Cipher.ENCRYPT_MODE);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bos.write(this.counter.asBytes());

            gcm.update(packet.getChannelID());
            gcm.update(packet.getMessageID());

            bos.write(gcm.doFinal(new byte[]{packet.getPacketType()}));

            bos.write(packet.getData());

            return bos.toByteArray();
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptionFailed("Couldn't link-encrypt the mix packet " + this.counter.asInt() + ".", e);
        }

    }

    public IPacket decrypt(byte[] packetBytes) throws DecryptionFailed {
        Counter linkPrefix = new Counter(packetBytes);

        Cipher gcm = Util.createGCM(this.key, linkPrefix.asIV(), Cipher.DECRYPT_MODE);

        ByteArrayInputStream bis = new ByteArrayInputStream(packetBytes);

        byte[] linkCounter = new byte[Counter.SIZE];
        byte[] cipherTextAndMac = new byte[HEADER_SIZE + GCM_MAC_SIZE];
        byte[] payload = new byte[packetBytes.length - OVERHEAD];

        try {
            assert bis.read(linkCounter) == linkCounter.length;
            assert bis.read(cipherTextAndMac) == cipherTextAndMac.length;
            assert bis.read(payload) == payload.length;

            bis.close();

            byte[] plainLinkHeader = gcm.doFinal(cipherTextAndMac);

            bis = new ByteArrayInputStream(plainLinkHeader);

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
        } catch (BadPaddingException | IllegalBlockSizeException | IOException e) {
            throw new DecryptionFailed("Couldn't link-decrypt the mix packet " + linkPrefix.asInt() + ".", e);
        }
    }
}
