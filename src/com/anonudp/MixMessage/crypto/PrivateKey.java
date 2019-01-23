package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;

import java.math.BigInteger;
import java.security.SecureRandom;

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

    public PrivateKey blind(PublicKey disposableKey) throws SymmetricKeyCreationFailed {
        BlindingFactor blindingFactor = new BlindingFactor(disposableKey);

        BigInteger newValue = this.underlyingValue.multiply(blindingFactor.getUnderlyingValue());

        return new PrivateKey(newValue);
    }
}
