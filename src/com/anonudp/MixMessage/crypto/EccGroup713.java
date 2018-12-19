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

    public static final int symmetricKeyLength = 16;

    private EccGroup713() {}

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
        assert symmetricKey.length == EccGroup713.symmetricKeyLength;

        byte[] message = new byte[EccGroup713.symmetricKeyLength];

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");

        SecretKeySpec keySpec = new SecretKeySpec(symmetricKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(message);
    }

    private static BigInteger makeExponentFromBytes(byte[] bytes)
    {
        BigInteger exponent = new BigInteger(bytes);

        return exponent.mod(EccGroup713.order);
    }

}
