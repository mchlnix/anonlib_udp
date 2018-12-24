package com.anonudp.MixMessage;

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
import java.util.Arrays;

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

    ProcessedDataPacket process(DataPacket packet, byte[] channelKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Counter counter = new Counter(packet.getCTRPrefix());

        Cipher cipher = Util.createCTRCipher(channelKey, counter.asIV(), Cipher.DECRYPT_MODE);

        return new ProcessedDataPacket(packet.getChannelID(), cipher.doFinal(packet.getData()));
    }

    public static class DataPacket implements Packet
    {
        public static final byte DATA_PACKET = 0x01;

        private byte[] channelID;

        private byte[] byteArray;
        private byte[] ctrPrefix;
        private byte[] encryptedData;

        public DataPacket(byte[] channelID, byte[] fragment)
        {
            this.channelID = channelID;

            this.byteArray = fragment;
            this.ctrPrefix = null;
            this.encryptedData = null;
        }

        byte[] toBytes()
        {
            return this.byteArray;
        }

        @Override
        public byte[] getCTRPrefix()
        {
            if (this.ctrPrefix == null)
                this.ctrPrefix = Arrays.copyOf(this.byteArray, Counter.CTR_PREFIX_SIZE);

            return this.ctrPrefix;
        }

        @Override
        public byte[] getChannelID() {
            return this.channelID;
        }

        @Override
        public byte[] getData()
        {
            if (this.encryptedData == null)
                this.encryptedData = Arrays.copyOfRange(this.byteArray, Counter.CTR_PREFIX_SIZE, this.byteArray.length);

            return this.encryptedData;
        }

        @Override
        public byte getPacketType() {
            return DATA_PACKET;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DataPacket)
                return this.equals((DataPacket) obj);

            return super.equals(obj);
        }

        boolean equals(DataPacket otherPacket)
        {
            // todo can this be more performant?
            return Arrays.equals(this.byteArray, otherPacket.toBytes());
        }
    }

    static class ProcessedDataPacket extends DataPacket
    {

        ProcessedDataPacket(byte[] channelID, byte[] fragment) {
            super(channelID, fragment);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof DataPacket)
                return ((DataPacket) obj).equals(this);

            return super.equals(obj);
        }
    }
}
