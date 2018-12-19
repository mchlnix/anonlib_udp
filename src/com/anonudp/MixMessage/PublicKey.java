package com.anonudp.MixMessage;

import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

class PublicKey
{
    private ECPoint underlyingValue;

    PublicKey(PrivateKey privateKey)
    {
        this.underlyingValue = EccGroup713.powInGroup(EccGroup713.getGenerator(), privateKey.getUnderlyingValue());
    }

    private PublicKey(ECPoint point)
    {
        this.underlyingValue = point;
    }

    byte[] toSymmetricKey() throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write("aes_key:".getBytes());
        bos.write(this.toBytes());

        bos.close();

        byte[] hash = digest.digest(bos.toByteArray());

        return Arrays.copyOf(hash, EccGroup713.symmetricKeyLength);
    }

    PublicKey blind(PrivateKey privateKey)
    {
        BlindingFactor blindingFactor = new BlindingFactor(privateKey);

        return this.blind(blindingFactor);
    }

    PublicKey blind(PublicKey publicKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        BlindingFactor blindingFactor = new BlindingFactor(publicKey);

        return this.blind(blindingFactor);
    }

    private PublicKey blind(BlindingFactor blindingFactor)
    {
        return new PublicKey(EccGroup713.powInGroup(this.underlyingValue, blindingFactor.getUnderlyingValue()));
    }


    ECPoint getUnderlyingValue() {
        return this.underlyingValue;
    }

    private byte[] toBytes()
    {
        return this.underlyingValue.getEncoded(true);
    }

}
