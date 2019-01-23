package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.math.BigInteger;
import java.security.Security;

import static com.anonudp.MixMessage.crypto.Util.createCTRCipher;

public class EccGroup713 {
    private static final ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp224r1");
    private static final ECPoint generator = spec.getG();
    private static final BigInteger order = spec.getCurve().getOrder();

    public static final int SYMMETRIC_KEY_LENGTH = 16;

    static
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private EccGroup713() {}

    static ECPoint loadPoint(byte[] encodedPoint)
    {
        return spec.getCurve().decodePoint(encodedPoint);
    }

    static ECPoint getGenerator() {
        return generator;
    }

    static BigInteger getOrder() {
        return order;
    }

    static ECPoint powInGroup(ECPoint base, BigInteger exponent)
    {
        return base.multiply(exponent);
    }

    static BigInteger hb(byte[] symmetricKey) throws SymmetricKeyCreationFailed {
        byte[] key = EccGroup713.deriveKey(symmetricKey, "hbhbhbhbhbhbhbhb".getBytes());

        return EccGroup713.makeExponentFromBytes(key);
    }

    private static byte[] deriveKey(byte[] symmetricKey, byte[] iv) throws SymmetricKeyCreationFailed {
        assert symmetricKey.length == iv.length;
        assert symmetricKey.length == EccGroup713.SYMMETRIC_KEY_LENGTH;

        byte[] message = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];

        Cipher cipher = createCTRCipher(symmetricKey, iv, Cipher.ENCRYPT_MODE);

        try {
            return cipher.doFinal(message);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new SymmetricKeyCreationFailed("The encryption step failed, when deriving a symmetric key.", e);
        }
    }

    private static BigInteger makeExponentFromBytes(byte[] bytes)
    {
        /*
        Java implementation of byte to BigInteger regards the first bit as a sign bit.
        Python implementation does not, so we prepend a 0 byte to force Java into producing
        a positive BigInteger.
         */
        byte[] fixedBytes = new byte[1 + bytes.length];

        System.arraycopy(bytes, 0, fixedBytes, 1, bytes.length);

        BigInteger exponent = new BigInteger(fixedBytes);

        return exponent.mod(EccGroup713.order);
    }

}
