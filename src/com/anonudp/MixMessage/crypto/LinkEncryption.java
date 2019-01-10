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
import java.util.Arrays;

import static com.anonudp.MixMessage.Util.randomBytes;

public class LinkEncryption {
    public static final int OVERHEAD = Counter.CTR_PREFIX_SIZE + Util.GCM_BLOCK_SIZE + Util.GCM_MAC_SIZE;
    private static final int RESERVED = Util.GCM_BLOCK_SIZE - Channel.ID_SIZE - Counter.CTR_PREFIX_SIZE - IPacket.TYPE_BYTE_SIZE;

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
        gcm.update(new byte[]{packet.getPacketType()});

        bos.write(gcm.doFinal(randomBytes(RESERVED)));

        bos.write(packet.getData());

        return bos.toByteArray();
    }

    public IPacket decrypt(byte[] packetBytes) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, IOException {
        Counter linkPrefix = new Counter(packetBytes);

        Cipher gcm = Util.createGCM(this.key, linkPrefix.asIV(), Cipher.DECRYPT_MODE);

        // todo make constants
        byte[] cipherTextAndMac = Arrays.copyOfRange(packetBytes, Counter.CTR_PREFIX_SIZE, Counter.CTR_PREFIX_SIZE + gcm.getBlockSize() + Util.GCM_MAC_SIZE);

        byte[] payload = Arrays.copyOfRange(packetBytes, Counter.CTR_PREFIX_SIZE + gcm.getBlockSize() + Util.GCM_MAC_SIZE, packetBytes.length);

        byte[] plainText = gcm.doFinal(cipherTextAndMac);

        // todo make constants

        ByteArrayInputStream bis = new ByteArrayInputStream(plainText);

        byte[] channelID = new byte[Channel.ID_SIZE];
        byte[] messagePrefix = new byte[Counter.CTR_PREFIX_SIZE];
        byte messageType;

        bis.read(channelID, 0, channelID.length);
        bis.read(messagePrefix, 0, messagePrefix.length);
        messageType = (byte) bis.read();

        IPacket returnPacket = null;

        if (messageType == IPacket.TYPE_DATA)
            returnPacket = new DataPacket(channelID, messagePrefix, payload);
        else if (messageType == IPacket.TYPE_INIT_RESPONSE)
            returnPacket = new InitResponse(channelID, payload);

        return returnPacket;
    }
}
