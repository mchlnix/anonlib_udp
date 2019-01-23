package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;

class BlindingFactor
{
    private BigInteger underlyingValue;

    BlindingFactor(PrivateKey privateKey) {
        this.underlyingValue = privateKey.getUnderlyingValue();
    }

    BlindingFactor(PublicKey publicKey) throws SymmetricKeyCreationFailed {
        this(publicKey.toSymmetricKey());
    }

    BlindingFactor(byte[] symmetricKey) throws SymmetricKeyCreationFailed {
        this.underlyingValue = EccGroup713.hb(symmetricKey);
    }

    BigInteger getUnderlyingValue() {
        return underlyingValue;
    }
}
