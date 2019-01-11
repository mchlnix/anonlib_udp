package com.anonudp.MixMessage.crypto;

import com.anonudp.MixChannel.Channel;
import com.anonudp.MixPacket.DataPacket;
import com.anonudp.MixPacket.IPacket;
import com.anonudp.MixPacket.InitResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static com.anonudp.MixMessage.crypto.Util.GCM_MAC_SIZE;

public class LinkEncryption {
    private static final int HEADER_SIZE = Channel.ID_SIZE + Counter.CTR_PREFIX_SIZE + IPacket.TYPE_BYTE_SIZE;
    public static final int OVERHEAD = Counter.CTR_PREFIX_SIZE + HEADER_SIZE + GCM_MAC_SIZE;

    private final byte[] key;
    private final Counter counter;

    public LinkEncryption(byte[] linkKey)
    {
        this.key = linkKey;
        this.counter = new Counter();
    }

    public byte[] encrypt(IPacket packet) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException, BadPaddingException, IllegalBlockSizeException {
        this.counter.count();

        Cipher gcm = Util.createGCM(this.key, this.counter.asIV(), Cipher.ENCRYPT_MODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(this.counter.asBytes());

        gcm.update(packet.getChannelID());
        gcm.update(packet.getCTRPrefix());
        bos.write(gcm.doFinal(new byte[]{packet.getPacketType()}));

        bos.write(packet.getData());

        return bos.toByteArray();
    }

    public IPacket decrypt(byte[] packetBytes) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, IOException {
        Counter linkPrefix = new Counter(packetBytes);

        Cipher gcm = Util.createGCM(this.key, linkPrefix.asIV(), Cipher.DECRYPT_MODE);

        ByteArrayInputStream bis = new ByteArrayInputStream(packetBytes);

        byte[] linkCounter = new byte[Counter.CTR_PREFIX_SIZE];
        byte[] cipherTextAndMac = new byte[HEADER_SIZE + GCM_MAC_SIZE];
        byte[] payload = new byte[packetBytes.length - OVERHEAD];

        assert bis.read(linkCounter) == linkCounter.length;
        assert bis.read(cipherTextAndMac) == cipherTextAndMac.length;
        assert bis.read(payload) == payload.length;

        bis.close();

        byte[] plainLinkHeader = gcm.doFinal(cipherTextAndMac);

        bis = new ByteArrayInputStream(plainLinkHeader);

        byte[] channelID = new byte[Channel.ID_SIZE];
        byte[] messagePrefix = new byte[Counter.CTR_PREFIX_SIZE];
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
}
