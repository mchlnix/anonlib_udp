package com.anonudp.MixPacket;

import com.anonudp.MixMessage.Fragment;
import com.anonudp.MixMessage.Util;
import com.anonudp.MixMessage.crypto.CTRCipher;
import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.Exception.DecryptionFailed;
import com.anonudp.MixMessage.crypto.Exception.PacketCreationFailed;
import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;
import com.anonudp.MixMessage.crypto.PrivateKey;
import com.anonudp.MixMessage.crypto.PublicKey;
import org.bouncycastle.crypto.InvalidCipherTextException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.anonudp.MixMessage.crypto.EccGroup713.SYMMETRIC_KEY_LENGTH;
import static com.anonudp.MixPacket.InitPacket.CHANNEL_KEY_ONION_SIZE;

public class PacketFactory {
    private final byte[] channelID;
    private final byte[] initPayload;

    private PublicKey[] publicKeys;
    private final byte[][] requestChannelKeys;
    private final byte[][] responseChannelKeys;

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

    ProcessedInitPacket process(InitPacket packet, PrivateKey privateKey) throws SymmetricKeyCreationFailed, DecryptionFailed {

        /* create shared key, originally used to encrypt */
        PublicKey disposableKey = packet.getPublicKey().blind(privateKey).blind(packet.getMessageID());

        byte[] symmetricDisposableKey = disposableKey.toSymmetricKey();

        /* decrypt channel key and payload */

        CTRCipher cipher = CTRCipher.getCipher(symmetricDisposableKey, new Counter(packet.getMessageID()).asIV(), CTRCipher.DECRYPT_MODE);

        try
        {
            byte[] decryptedChannelOnion = cipher.decryptBuffer(packet.getChannelKeyOnion());

            byte[] requestChannelKey = new byte[SYMMETRIC_KEY_LENGTH];
            byte[] responseChannelKey = new byte[SYMMETRIC_KEY_LENGTH];
            byte[] encryptedChannelOnion = new byte[CHANNEL_KEY_ONION_SIZE - 2 * SYMMETRIC_KEY_LENGTH];

            ByteArrayInputStream bis = new ByteArrayInputStream(decryptedChannelOnion);

            assert bis.read(requestChannelKey) == requestChannelKey.length;
            assert bis.read(responseChannelKey) == responseChannelKey.length;
            assert bis.read(encryptedChannelOnion) == encryptedChannelOnion.length;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bos.write(encryptedChannelOnion);
            bos.write(Util.randomBytes(2 * SYMMETRIC_KEY_LENGTH));

            byte[] processedChannelOnion = bos.toByteArray();

            assert processedChannelOnion.length == packet.getChannelKeyOnion().length;

            byte[] processedPayloadOnion = cipher.decryptBuffer(packet.getPayloadOnion());

            /* generate next public key */

            PublicKey newElement = packet.getPublicKey().blind(disposableKey);

            return new ProcessedInitPacket(this.channelID, packet.getMessageID(), requestChannelKey, responseChannelKey, newElement, processedChannelOnion, processedPayloadOnion);
        } catch (InvalidCipherTextException | IOException e) {
            throw new DecryptionFailed("Couldn't decrypt init packet.", e);
        }
    }

    public ProcessedDataPacket process(DataPacket packet, byte[] channelKey) throws DecryptionFailed {
        Counter counter = new Counter(packet.getMessageID());

        CTRCipher cipher = CTRCipher.getCipher(channelKey, counter.asIV(), CTRCipher.DECRYPT_MODE);

        try
        {
            return new ProcessedDataPacket(packet.getChannelID(), packet.getMessageID(), cipher.decryptBuffer(packet.getData()));
        }
        catch (InvalidCipherTextException e) {
            throw new DecryptionFailed("Could not decrypt data packet.", e);
        }
    }

    public InitPacket makeInitPacket(Fragment fragment) throws PacketCreationFailed {
        this.requestCounter.count();

        PublicKey[] disposableKeys = new PublicKey[this.publicKeys.length];

        PrivateKey privateMessageKey = new PrivateKey();
        PublicKey publicMessageKey = new PublicKey(privateMessageKey);

        /* create shared disposable keys */

        try {
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

            CTRCipher cipher;
            bos = new ByteArrayOutputStream();

            for (int i = disposableKeys.length - 1; i >= 0; --i)
            {
                cipher = CTRCipher.getCipher(disposableKeys[i].toSymmetricKey(), this.requestCounter.asIV(), CTRCipher.ENCRYPT_MODE);

                bos.write(this.requestChannelKeys[i]);
                bos.write(this.responseChannelKeys[i]);
                bos.write(Arrays.copyOf(channelOnion, channelOnion.length - 2 * SYMMETRIC_KEY_LENGTH));

                channelOnion = cipher.decryptBuffer(bos.toByteArray());

                payloadOnion = cipher.decryptBuffer(payloadOnion);

                bos.reset();
            }

            return new InitPacket(channelID, this.requestCounter.asBytes(), publicMessageKey, channelOnion, payloadOnion);
        }
        catch (SymmetricKeyCreationFailed | IOException | InvalidCipherTextException e) {
            throw new PacketCreationFailed("Couldn't create channel initialization packet.", e);
        }
    }

    public DataPacket makeDataPacket(Fragment fragment) throws PacketCreationFailed {
        try
        {
            byte[] encryptedFragment;
            encryptedFragment = fragment.toBytes();

            this.requestCounter.count();

            for(int i = this.mixCount - 1; i >= 0 ; --i)
            {
                CTRCipher cipher = CTRCipher.getCipher(this.requestChannelKeys[i], this.requestCounter.asIV(), CTRCipher.ENCRYPT_MODE);

                encryptedFragment = cipher.encryptBuffer(encryptedFragment);
            }

            return new DataPacket(this.channelID, this.requestCounter.asBytes(), encryptedFragment);
        }
        catch (IOException | InvalidCipherTextException e) {
            throw new PacketCreationFailed("Couldn't create data packet.", e);
        }
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
