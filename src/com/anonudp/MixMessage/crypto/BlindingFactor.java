package com.anonudp.MixMessage.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

class BlindingFactor
{
    private BigInteger underlyingValue;

    BlindingFactor(PrivateKey privateKey) {
        this.underlyingValue = privateKey.getUnderlyingValue();
    }

    BlindingFactor(PublicKey publicKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException {
        this(publicKey.toSymmetricKey());
    }

    private BlindingFactor(byte[] symmetricKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
        this.underlyingValue = EccGroup713.hb(symmetricKey);
    }

    BigInteger getUnderlyingValue() {
        return underlyingValue;
    }
}
