package com.anonudp.MixMessage.crypto;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;

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

    static BigInteger hb(byte[] symmetricKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
        byte[] key = EccGroup713.deriveKey(symmetricKey, "hbhbhbhbhbhbhbhb".getBytes());

        return EccGroup713.makeExponentFromBytes(key);
    }

    private static byte[] deriveKey(byte[] symmetricKey, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        assert symmetricKey.length == iv.length;
        assert symmetricKey.length == EccGroup713.SYMMETRIC_KEY_LENGTH;

        byte[] message = new byte[EccGroup713.SYMMETRIC_KEY_LENGTH];

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(message);
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
