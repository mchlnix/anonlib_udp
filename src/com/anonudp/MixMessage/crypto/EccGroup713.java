package com.anonudp.MixMessage.crypto;

import com.anonudp.MixMessage.crypto.Exception.SymmetricKeyCreationFailed;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP224R1Curve;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.Security;

public class EccGroup713 {
    /*
     * secp224r1
     */
    private static X9ECParameters createParametersSECP224R1() {
        byte[] S = Hex.decode("BD71344799D5C7FCDC45B59FA3B9AB8F6A948BC5");
        ECCurve curve = new SecP224R1Curve();
        X9ECPoint G = new X9ECPoint(curve, Hex.decode("04"
                + "B70E0CBD6BB4BF7F321390B94A03C1D356C21122343280D6115C1D21"
                + "BD376388B5F723FB4C22DFE6CD4375A05A07476444D5819985007E34"));
        return new X9ECParameters(curve, G, curve.getOrder(), curve.getCofactor(), S);
    }

    private static final X9ECParameters spec = createParametersSECP224R1();
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

    static BigInteger hb(byte[] symmetricKey) throws SymmetricKeyCreationFailed
    {
        byte[] key = EccGroup713.deriveKey(symmetricKey, "hbhbhbhbhbhbhbhb".getBytes());

        return EccGroup713.makeExponentFromBytes(key);
    }

    private static byte[] deriveKey(byte[] symmetricKey, byte[] iv) throws SymmetricKeyCreationFailed
    {
        assert symmetricKey.length == iv.length;
        assert symmetricKey.length == EccGroup713.SYMMETRIC_KEY_LENGTH;

        byte[] derivedKey = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];

        CTRCipher cipher = CTRCipher.getCipher(symmetricKey, iv, CTRCipher.ENCRYPT_MODE);

        try
        {
            derivedKey = cipher.encryptBuffer(derivedKey);
        } catch (InvalidCipherTextException e)
        {
            throw new SymmetricKeyCreationFailed("The encryption step failed, when deriving a symmetric key.", e);
        }

        return derivedKey;
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
