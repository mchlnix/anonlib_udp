package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;

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

import static com.anonudp.MixMessage.crypto.EccGroup713.SYMMETRIC_KEY_LENGTH;
import static com.anonudp.MixMessage.crypto.Util.createCTRCipher;
import static com.anonudp.MixPacket.InitPacket.CHANNEL_KEY_ONION_SIZE;

public class PacketFactory {
    private final byte[] channelID;
    private final byte[] initPayload;

    private PublicKey[] publicKeys;
    private byte[][] requestChannelKeys;
    private byte[][] responseChannelKeys;

    private int mixCount;
    private Counter requestCounter;


    public PacketFactory(byte[] channelID, byte[] initPayload, PublicKey[] publicKeys)
    {
        this.requestChannelKeys = new byte[publicKeys.length][SYMMETRIC_KEY_LENGTH];
        this.responseChannelKeys = new byte[publicKeys.length][SYMMETRIC_KEY_LENGTH];


        for(int i = 0; i < publicKeys.length; ++i) {
            this.requestChannelKeys[i] = Util.randomBytes(SYMMETRIC_KEY_LENGTH);
            this.responseChannelKeys[i] = Util.randomBytes(SYMMETRIC_KEY_LENGTH);
        }

        this.channelID = channelID;
        this.initPayload = initPayload;

        if (publicKeys.length < 1)
            throw new IllegalArgumentException("Was given empty public key list.");

        this.publicKeys = publicKeys;

        this.mixCount = this.publicKeys.length;
        this.requestCounter = new Counter();
        this.requestCounter.count();
    }

    public ProcessedInitPacket process(InitPacket packet, PrivateKey privateKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {

        /* create shared key, originally used to encrypt */
        PublicKey disposableKey = packet.getPublicKey().blind(privateKey).blind(packet.getCTRPrefix());

        byte[] symmetricDisposableKey = disposableKey.toSymmetricKey();

        /* decrypt channel key and payload */

        Cipher cipher = createCTRCipher(symmetricDisposableKey, new Counter(packet.getCTRPrefix()).asIV(), Cipher.DECRYPT_MODE);

        byte[] processedChannelOnion = cipher.update(packet.getChannelKeyOnion());

        byte[] requestChannelKey = new byte[SYMMETRIC_KEY_LENGTH];
        byte[] responseChannelKey = new byte[SYMMETRIC_KEY_LENGTH];
        byte[] encryptedChannelOnion = new byte[CHANNEL_KEY_ONION_SIZE - 2 * SYMMETRIC_KEY_LENGTH];

        ByteArrayInputStream bis = new ByteArrayInputStream(processedChannelOnion);

        bis.read(requestChannelKey);
        bis.read(responseChannelKey);
        bis.read(encryptedChannelOnion);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(encryptedChannelOnion);
        bos.write(Util.randomBytes(2 * SYMMETRIC_KEY_LENGTH));

        processedChannelOnion = bos.toByteArray();

        byte[] processedPayloadOnion = cipher.doFinal(packet.getPayloadOnion());

        /* generate next public key */

        PublicKey newElement = packet.getPublicKey().blind(disposableKey);

        return new ProcessedInitPacket(this.channelID, packet.getCTRPrefix(), requestChannelKey, responseChannelKey, newElement, processedChannelOnion, processedPayloadOnion);
    }

    public ProcessedDataPacket process(DataPacket packet, byte[] channelKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        Counter counter = new Counter(packet.getCTRPrefix());

        Cipher cipher = createCTRCipher(channelKey, counter.asIV(), Cipher.DECRYPT_MODE);

        return new ProcessedDataPacket(packet.getChannelID(), packet.getCTRPrefix(), cipher.doFinal(packet.getData()));
    }

    public InitPacket makeInitPacket(Fragment fragment) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        this.requestCounter.count();

        PublicKey[] disposableKeys = new PublicKey[this.publicKeys.length];

        PrivateKey privateMessageKey = new PrivateKey();
        PublicKey publicMessageKey = new PublicKey(privateMessageKey);

        /* create shared disposable keys */

        disposableKeys[0] = this.publicKeys[0].blind(privateMessageKey).blind(this.requestCounter.asBytes());

        for (int i = 1; i < this.publicKeys.length; ++i)
        {
            privateMessageKey = privateMessageKey.blind(disposableKeys[i-1]);

            disposableKeys[i] = this.publicKeys[i].blind(privateMessageKey).blind(this.requestCounter.asBytes());
        }

        /* preparing "onions" */

        byte[] channelOnion = new byte[CHANNEL_KEY_ONION_SIZE];

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
            cipher = createCTRCipher(disposableKeys[i].toSymmetricKey(), this.requestCounter.asIV(), Cipher.ENCRYPT_MODE);

            bos.write(this.requestChannelKeys[i]);
            bos.write(this.responseChannelKeys[i]);
            bos.write(Arrays.copyOf(channelOnion, channelOnion.length - 2 * SYMMETRIC_KEY_LENGTH));

            channelOnion = cipher.update(bos.toByteArray());

            payloadOnion = cipher.doFinal(payloadOnion);

            bos.reset();
        }

        return new InitPacket(channelID, this.requestCounter.asBytes(), publicMessageKey, channelOnion, payloadOnion);
    }

    public DataPacket makeDataPacket(Fragment fragment) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedFragment = fragment.toBytes();

        this.requestCounter.count();

        for(int i = this.mixCount - 1; i >= 0 ; --i)
        {
            Cipher cipher = createCTRCipher(this.requestChannelKeys[i], this.requestCounter.asIV(), Cipher.ENCRYPT_MODE);

            encryptedFragment = cipher.doFinal(encryptedFragment);
        }

        return new DataPacket(this.channelID, this.requestCounter.asBytes(), encryptedFragment);
    }

    public byte[][] getRequestChannelKeys() {
        return requestChannelKeys;
    }

    public byte[][] getResponseChannelKeys() {
        return responseChannelKeys;
    }

    public byte[] getChannelID() {
        return channelID;
    }
}
