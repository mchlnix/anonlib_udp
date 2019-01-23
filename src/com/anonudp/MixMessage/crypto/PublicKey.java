package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public byte[] toSymmetricKey() throws SymmetricKeyCreationFailed {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            bos.write("aes_key:".getBytes());
            bos.write(this.underlyingValue.getEncoded(false));

            bos.close();

            byte[] hash = digest.digest(bos.toByteArray());

            return Arrays.copyOf(hash, EccGroup713.SYMMETRIC_KEY_LENGTH);
        }
        catch (NoSuchAlgorithmException | IOException e) {
            throw new SymmetricKeyCreationFailed("Turning public key into symmetric key failed.", e);
        }

    }

    public PublicKey blind(PrivateKey privateKey)
    {
        BlindingFactor blindingFactor = new BlindingFactor(privateKey);

        return this.blind(blindingFactor);
    }

    public PublicKey blind(PublicKey publicKey) throws SymmetricKeyCreationFailed {
        BlindingFactor blindingFactor = new BlindingFactor(publicKey);

        return this.blind(blindingFactor);
    }

    public PublicKey blind(byte[] messageCounterPrefix) throws SymmetricKeyCreationFailed {
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
