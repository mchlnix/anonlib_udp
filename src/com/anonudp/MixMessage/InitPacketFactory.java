package com.anonudp.MixMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

public class InitPacketFactory {
    private PublicKey[] publicKeys;


    InitPacketFactory(PublicKey[] publicKeys)
    {
        if (publicKeys.length < 1)
            throw new IllegalArgumentException("Was given empty public key list.");

        this.publicKeys = publicKeys;
    }

    ProcessedInitPacket process(InitPacket packet, PrivateKey privateKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {

        /* create shared key, originally used to encrypt */

        PublicKey disposableKey = packet.publicKey.blind(privateKey);

        byte[] symmetricDisposableKey = disposableKey.toSymmetricKey();

        /* decrypt channel key and payload */

        Cipher cipher = createCTRCipher(symmetricDisposableKey, Cipher.DECRYPT_MODE);

        byte[] processedChannelOnion = cipher.doFinal(packet.channelKeyOnion);

        byte[] channelKey = Arrays.copyOf(processedChannelOnion, 16);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(Arrays.copyOfRange(processedChannelOnion, 16, 48));
        bos.write(new byte[16]); // todo: make random bytes

        processedChannelOnion = bos.toByteArray();

        byte[] processedPayloadOnion = cipher.doFinal(packet.payloadOnion);

        /* generate next public key */

        PublicKey newElement = packet.publicKey.blind(disposableKey);

        return new ProcessedInitPacket(channelKey, newElement, processedChannelOnion, processedPayloadOnion);
    }

    InitPacket makePacket(byte[][] channelKeys, byte[] payload) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
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

        byte[] channelOnion = new byte[EccGroup713.symmetricKeyLength * this.publicKeys.length];
        byte[] payloadOnion = payload;

        /* encrypt "onions" */

        Cipher cipher;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (int i = disposableKeys.length - 1; i >= 0; --i)
        {
            cipher = createCTRCipher(disposableKeys[i].toSymmetricKey(), Cipher.ENCRYPT_MODE);

            bos.write(channelKeys[i]);
            bos.write(Arrays.copyOfRange(channelOnion, EccGroup713.symmetricKeyLength, channelOnion.length));

            channelOnion = cipher.doFinal(bos.toByteArray());
            payloadOnion = cipher.doFinal(payloadOnion);

            bos.reset();
        }

        return new InitPacket(publicMessageKey, channelOnion, payloadOnion);
    }

    private Cipher createCTRCipher(byte[] symmetricDisposableKey, int mode) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        SecretKeySpec keySpec = new SecretKeySpec(symmetricDisposableKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[EccGroup713.symmetricKeyLength]);

        cipher.init(mode, keySpec, ivSpec);

        return cipher;
    }

    class InitPacket
    {
        private PublicKey publicKey;
        private byte[] channelKeyOnion;
        private byte[] payloadOnion;

        InitPacket(PublicKey publicKey, byte[] channelKeyOnion, byte[] payloadOnion)
        {
            this.publicKey = publicKey;
            this.channelKeyOnion = channelKeyOnion;
            this.payloadOnion = payloadOnion;
        }
    }

    class ProcessedInitPacket extends InitPacket
    {
        private byte[] channelKey;

        private ProcessedInitPacket(byte[] channelKey, PublicKey element, byte[] processedChannelOnion, byte[] processedPayloadOnion)
        {
            super(element, processedChannelOnion, processedPayloadOnion);

            this.channelKey = channelKey;
        }

        byte[] getChannelKey()
        {
            return this.channelKey;
        }
    }
}
