package com.anonudp.MixMessage.crypto;

import com.anonudp.Packet.DataPacket;
import com.anonudp.Packet.InitPacket;
import com.anonudp.Packet.InitResponse;
import com.anonudp.Packet.Packet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

import static com.anonudp.MixMessage.Util.randomBytes;

public class LinkEncryption {
    private final byte[] key;
    private final Counter counter;

    public LinkEncryption(byte[] linkKey)
    {
        this.key = linkKey;
        this.counter = new Counter();
    }

    public byte[] encrypt(Packet packet) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException, BadPaddingException, IllegalBlockSizeException {
        this.counter.count();

        Cipher gcm = Util.createGCM(this.key, this.counter.asIV(), Cipher.ENCRYPT_MODE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(this.counter.asPrefix());

        gcm.update(packet.getChannelID());
        gcm.update(packet.getCTRPrefix());
        gcm.update(new byte[]{packet.getPacketType()});

        bos.write(gcm.doFinal(randomBytes(5)));

        bos.write(packet.getData());

        return bos.toByteArray();
    }

    public Packet decrypt(byte[] packetBytes) throws NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, IOException {
        Counter linkPrefix = new Counter(packetBytes);

        Cipher gcm = Util.createGCM(this.key, linkPrefix.asIV(), Cipher.DECRYPT_MODE);

        // todo make constants
        byte[] cipherTextAndMac = Arrays.copyOfRange(packetBytes, Counter.CTR_PREFIX_SIZE, Counter.CTR_PREFIX_SIZE + gcm.getBlockSize() + Util.GCM_MAC_SIZE);

        byte[] payload = Arrays.copyOfRange(packetBytes, Counter.CTR_PREFIX_SIZE + gcm.getBlockSize() + Util.GCM_MAC_SIZE, packetBytes.length);

        byte[] plainText = gcm.doFinal(cipherTextAndMac);

        // todo make constants
        byte[] channelID = Arrays.copyOf(plainText, 2);
        byte[] messagePrefix = Arrays.copyOfRange(plainText, 2, 2 + Counter.CTR_PREFIX_SIZE);
        byte messageType = plainText[2 + Counter.CTR_PREFIX_SIZE];

        Packet returnPacket = null;

        if (messageType == Packet.TYPE_DATA) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bos.write(messagePrefix);
            bos.write(payload);

            returnPacket = new DataPacket(channelID, bos.toByteArray());
        }
        else if (messageType == Packet.TYPE_INIT)
            returnPacket = new InitPacket(channelID, payload);
        else if (messageType == Packet.TYPE_INIT_RESPONSE)
            returnPacket = new InitResponse(channelID, payload);

        return returnPacket;
    }
}
