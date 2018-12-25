package com.anonudp.Packet;

import com.anonudp.MixMessage.Fragment;
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
    private byte[] channelID;

    private PublicKey[] publicKeys;


    public InitPacketFactory(byte[] channelID, PublicKey[] publicKeys)
    {
        this.channelID = channelID;

        if (publicKeys.length < 1)
            throw new IllegalArgumentException("Was given empty public key list.");

        this.publicKeys = publicKeys;
    }

    public ProcessedInitPacket process(InitPacket packet, PrivateKey privateKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException {

        /* create shared key, originally used to encrypt */

        PublicKey disposableKey = packet.getPublicKey().blind(privateKey);

        byte[] symmetricDisposableKey = disposableKey.toSymmetricKey();

        /* decrypt channel key and payload */

        Cipher cipher = createCTRCipher(symmetricDisposableKey, Cipher.DECRYPT_MODE);

        byte[] processedChannelOnion = cipher.doFinal(packet.getChannelKeyOnion());

        // TODO: get rid of magic numbers
        byte[] channelKey = Arrays.copyOf(processedChannelOnion, 16);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write(Arrays.copyOfRange(processedChannelOnion, 16, 48));
        bos.write(new byte[16]); // todo: make random bytes

        processedChannelOnion = bos.toByteArray();

        byte[] processedPayloadOnion = cipher.doFinal(packet.getPayloadOnion());

        /* generate next public key */

        PublicKey newElement = packet.getPublicKey().blind(disposableKey);

        return new ProcessedInitPacket(this.channelID, channelKey, newElement, processedChannelOnion, processedPayloadOnion);
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

        return new InitPacket(channelID, publicMessageKey, channelOnion, payloadOnion);
    }
}
