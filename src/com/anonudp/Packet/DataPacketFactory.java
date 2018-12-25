package com.anonudp.Packet;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.Util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DataPacketFactory {
    private final Counter counter;

    private byte[] channelID;

    private final byte[][] channelKeys;
    private final int mixCount;

    public DataPacketFactory(byte[] channelID, byte[][] channelKeys)
    {
        this.channelID = channelID;

        this.channelKeys = channelKeys;
        this.mixCount = this.channelKeys.length;
        this.counter = new Counter();
    }

    public DataPacket makePacket(Fragment fragment) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedData = new byte[this.mixCount * Counter.CTR_PREFIX_SIZE + Fragment.DATA_FRAGMENT_SIZE];

        System.arraycopy(fragment.toBytes(), 0, encryptedData, this.mixCount * Counter.CTR_PREFIX_SIZE, fragment.toBytes().length);

        this.counter.count();

        for(int i = this.mixCount - 1; i >= 0 ; --i)
        {
            Cipher cipher = Util.createCTRCipher(this.channelKeys[i], this.counter.asIV(), Cipher.ENCRYPT_MODE);

            int dataOffset = (i+1) * Counter.CTR_PREFIX_SIZE;
            int dataSize = encryptedData.length - dataOffset;

            byte[] tmpEncrypted = cipher.doFinal(encryptedData, dataOffset, dataSize);

            System.arraycopy(tmpEncrypted,0, encryptedData, dataOffset, dataSize);

            // prepend the counter prefix to the payload
            System.arraycopy(counter.asPrefix(), 0, encryptedData, i * Counter.CTR_PREFIX_SIZE, Counter.CTR_PREFIX_SIZE);
        }

        return new DataPacket(this.channelID, encryptedData);
    }

    public ProcessedDataPacket process(DataPacket packet, byte[] channelKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Counter counter = new Counter(packet.getCTRPrefix());

        Cipher cipher = Util.createCTRCipher(channelKey, counter.asIV(), Cipher.DECRYPT_MODE);

        return new ProcessedDataPacket(packet.getChannelID(), cipher.doFinal(packet.getData()));
    }
}
