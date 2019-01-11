package com.anonudp.MixMessage.crypto;

import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class PublicKey
{
    public static final int SIZE = 29; // bytes

    private final ECPoint underlyingValue;

    public PublicKey(PrivateKey privateKey)
    {
        this.underlyingValue = EccGroup713.powInGroup(EccGroup713.getGenerator(), privateKey.getUnderlyingValue());
    }

    private PublicKey(ECPoint point)
    {
        this.underlyingValue = point;
    }

    public byte[] toSymmetricKey() throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write("aes_key:".getBytes());
        bos.write(this.underlyingValue.getEncoded(false));

        bos.close();

        byte[] hash = digest.digest(bos.toByteArray());

        return Arrays.copyOf(hash, EccGroup713.SYMMETRIC_KEY_LENGTH);
    }

    public PublicKey blind(PrivateKey privateKey)
    {
        BlindingFactor blindingFactor = new BlindingFactor(privateKey);

        return this.blind(blindingFactor);
    }

    public PublicKey blind(PublicKey publicKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        BlindingFactor blindingFactor = new BlindingFactor(publicKey);

        return this.blind(blindingFactor);
    }

    public PublicKey blind(byte[] messageCounterPrefix) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        byte[] iv = new Counter(messageCounterPrefix).asIV();
        BlindingFactor blindingFactor = new BlindingFactor(iv);

        return this.blind(blindingFactor);
    }

    private PublicKey blind(BlindingFactor blindingFactor)
    {
        return new PublicKey(EccGroup713.powInGroup(this.underlyingValue, blindingFactor.getUnderlyingValue()));
    }

    private ECPoint getUnderlyingValue() {
        return this.underlyingValue;
    }

    public static PublicKey fromBytes(byte[] binaryKey)
    {
        return new PublicKey(EccGroup713.loadPoint(binaryKey));
    }

    public byte[] toBytes()
    {
        return this.underlyingValue.getEncoded(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PublicKey)
            return this.underlyingValue.equals(((PublicKey) obj).getUnderlyingValue());

        return super.equals(obj);
    }
}
