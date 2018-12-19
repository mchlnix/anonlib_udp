package com.anonudp.MixMessage.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;

public class PrivateKey
{
    private final BigInteger underlyingValue;
    private static final SecureRandom random = new SecureRandom();

    public PrivateKey()
    {
        BigInteger randomNumber;

        do {
            randomNumber = new BigInteger(EccGroup713.getOrder().bitLength(), random);
        } while (randomNumber.compareTo(EccGroup713.getOrder()) >= 0);

        this.underlyingValue = randomNumber;
    }

    private PrivateKey(BigInteger bigInteger)
    {
        this.underlyingValue = bigInteger;
    }

    BigInteger getUnderlyingValue() {
        return underlyingValue;
    }

    public PrivateKey blind(PublicKey disposableKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        BlindingFactor blindingFactor = new BlindingFactor(disposableKey);

        BigInteger newValue = this.underlyingValue.multiply(blindingFactor.getUnderlyingValue());

        return new PrivateKey(newValue);
    }
}
