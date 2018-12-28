package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;

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

import static com.anonudp.MixMessage.crypto.Util.createCTRCipher;

public class PacketFactory {
    private final byte[] channelID;
    private final byte[] initPayload;

    private PublicKey[] publicKeys;
    private byte[][] channelKeys;

    private int mixCount;
    private Counter requestCounter;


    public PacketFactory(byte[] channelID, byte[] initPayload, PublicKey[] publicKeys)
    {
        this.channelKeys = new byte[publicKeys.length][EccGroup713.SYMMETRIC_KEY_LENGTH];

        for(int i = 0; i < publicKeys.length; ++i)
            this.channelKeys[i] = Util.randomBytes(EccGroup713.SYMMETRIC_KEY_LENGTH);

        this.channelID = channelID;
        this.initPayload = initPayload;

        if (publicKeys.length < 1)
            throw new IllegalArgumentException("Was given empty public key list.");

        this.publicKeys = publicKeys;

        this.mixCount = this.publicKeys.length;
        this.requestCounter = new Counter();
    }

    public ProcessedInitPacket process(InitPacket packet, PrivateKey privateKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {

        /* create shared key, originally used to encrypt */

        PublicKey disposableKey = packet.getPublicKey().blind(privateKey);

        byte[] symmetricDisposableKey = disposableKey.toSymmetricKey();

        /* decrypt channel key and payload */

        Cipher cipher = createCTRCipher(symmetricDisposableKey, packet.getCTRPrefix(), Cipher.DECRYPT_MODE);

        byte[] processedChannelOnion = cipher.update(packet.getChannelKeyOnion());

        // TODO: get rid of magic numbers
        byte[] channelKey = Arrays.copyOf(processedChannelOnion, 16);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(Arrays.copyOfRange(processedChannelOnion, 16, 48));
        bos.write(new byte[16]); // todo: make random bytes

        processedChannelOnion = bos.toByteArray();

        byte[] processedPayloadOnion = cipher.doFinal(packet.getPayloadOnion());

        /* generate next public key */

        PublicKey newElement = packet.getPublicKey().blind(disposableKey);

        return new ProcessedInitPacket(this.channelID, packet.getCTRPrefix(), channelKey, newElement, processedChannelOnion, processedPayloadOnion);
    }

    public ProcessedDataPacket process(DataPacket packet, byte[] channelKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Counter counter = new Counter(packet.getCTRPrefix());

        Cipher cipher = com.anonudp.MixMessage.crypto.Util.createCTRCipher(channelKey, counter.asIV(), Cipher.DECRYPT_MODE);

        return new ProcessedDataPacket(packet.getChannelID(), cipher.doFinal(packet.getData()));
    }

    public InitPacket makeInitPacket(Fragment fragment) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        PublicKey[] disposableKeys = new PublicKey[this.publicKeys.length];

        PrivateKey privateMessageKey = new PrivateKey();
        PublicKey publicMessageKey = new PublicKey(privateMessageKey);

        /* create shared disposable keys */

        disposableKeys[0] = this.publicKeys[0].blind(privateMessageKey);

        for (int i = 1; i < this.publicKeys.length; ++i)
        {
            privateMessageKey = privateMessageKey.blind(disposableKeys[i-1]);

            disposableKeys[i] = this.publicKeys[i].blind(privateMessageKey);
        }

        /* preparing "onions" */

        byte[] channelOnion = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH * this.publicKeys.length];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(this.initPayload);
        bos.write(fragment.toBytes());

        byte[] payloadOnion = bos.toByteArray();

        bos.close();

        /* encrypt "onions" */

        Cipher cipher;
        bos = new ByteArrayOutputStream();

        for (int i = disposableKeys.length - 1; i >= 0; --i)
        {
            cipher = createCTRCipher(disposableKeys[i].toSymmetricKey(), this.requestCounter.asPrefix(), Cipher.ENCRYPT_MODE);

            bos.write(this.channelKeys[i]);
            bos.write(Arrays.copyOf(channelOnion, channelOnion.length - EccGroup713.SYMMETRIC_KEY_LENGTH));

            channelOnion = cipher.update(bos.toByteArray());

            payloadOnion = cipher.doFinal(payloadOnion);

            bos.reset();
        }

        return new InitPacket(channelID, this.requestCounter.asPrefix(), publicMessageKey, channelOnion, payloadOnion);
    }

    public DataPacket makeDataPacket(Fragment fragment) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedData = new byte[this.mixCount * Counter.CTR_PREFIX_SIZE + Fragment.DATA_FRAGMENT_SIZE];

        System.arraycopy(fragment.toBytes(), 0, encryptedData, this.mixCount * Counter.CTR_PREFIX_SIZE, fragment.toBytes().length);

        this.requestCounter.count();

        for(int i = this.mixCount - 1; i >= 0 ; --i)
        {
            Cipher cipher = createCTRCipher(this.channelKeys[i], this.requestCounter.asIV(), Cipher.ENCRYPT_MODE);

            int dataOffset = (i+1) * Counter.CTR_PREFIX_SIZE;
            int dataSize = encryptedData.length - dataOffset;

            byte[] tmpEncrypted = cipher.doFinal(encryptedData, dataOffset, dataSize);

            System.arraycopy(tmpEncrypted,0, encryptedData, dataOffset, dataSize);

            // prepend the counter prefix to the payload
            System.arraycopy(requestCounter.asPrefix(), 0, encryptedData, i * Counter.CTR_PREFIX_SIZE, Counter.CTR_PREFIX_SIZE);
        }

        return new DataPacket(this.channelID, encryptedData);
    }
}
