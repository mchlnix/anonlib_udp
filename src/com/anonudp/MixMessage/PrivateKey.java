package com.anonudp.MixMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;

class PrivateKey
{
    private BigInteger underlyingValue;
    private static SecureRandom random = new SecureRandom();

    PrivateKey()
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

    PrivateKey blind(PublicKey disposableKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException, IOException {
        BlindingFactor blindingFactor = new BlindingFactor(disposableKey);

        BigInteger newValue = this.underlyingValue.multiply(blindingFactor.getUnderlyingValue());

        return new PrivateKey(newValue);
    }
}
