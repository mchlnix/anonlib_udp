package com.anonudp.MixMessage;

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

public class InitPacketFactory {
    private PublicKey[] publicKeys;


    public InitPacketFactory(PublicKey[] publicKeys)
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

        // TODO: get rid of magic numbers
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

    public InitPacket makePacket(byte[][] channelKeys, Fragment fragment) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
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
        byte[] payloadOnion = fragment.toBytes();

        /* encrypt "onions" */

        Cipher cipher;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (int i = disposableKeys.length - 1; i >= 0; --i)
        {
            cipher = createCTRCipher(disposableKeys[i].toSymmetricKey(), Cipher.ENCRYPT_MODE);

            bos.write(channelKeys[i]);
            bos.write(Arrays.copyOf(channelOnion, channelOnion.length - EccGroup713.symmetricKeyLength));

            channelOnion = cipher.doFinal(bos.toByteArray());
            payloadOnion = cipher.doFinal(payloadOnion);

            bos.reset();
        }

        return new InitPacket(publicMessageKey, channelOnion, payloadOnion);
    }

    public static class InitPacket
    {
        private final PublicKey publicKey;
        private final byte[] channelKeyOnion;
        private final byte[] payloadOnion;

        InitPacket(PublicKey publicKey, byte[] channelKeyOnion, byte[] payloadOnion)
        {
            this.publicKey = publicKey;
            this.channelKeyOnion = channelKeyOnion;
            this.payloadOnion = payloadOnion;
        }

        PublicKey getPublicKey() {
            return publicKey;
        }

        byte[] getChannelKeyOnion() {
            return channelKeyOnion;
        }

        byte[] getPayloadOnion() {
            return payloadOnion;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ProcessedInitPacket)
                return this.equals((ProcessedInitPacket) obj);

            return super.equals(obj);
        }

        boolean equals(ProcessedInitPacket otherPacket)
        {
            boolean is_equal = this.publicKey == otherPacket.getPublicKey();
            is_equal = is_equal && this.channelKeyOnion == otherPacket.getChannelKeyOnion();
            is_equal = is_equal &&  this.payloadOnion == otherPacket.getPayloadOnion();

            return is_equal;
        }
    }

    static class ProcessedInitPacket extends InitPacket
    {
        private final byte[] channelKey;

        ProcessedInitPacket(byte[] channelKey, PublicKey element, byte[] processedChannelOnion, byte[] processedPayloadOnion)
        {
            super(element, processedChannelOnion, processedPayloadOnion);

            this.channelKey = channelKey;
        }

        byte[] getChannelKey()
        {
            return this.channelKey;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj instanceof InitPacket)
                return ((InitPacket) obj).equals(this);

            return super.equals(obj);
        }
    }
}
