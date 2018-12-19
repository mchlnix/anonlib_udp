package com.anonudp.MixMessage;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.util.Arrays;

class EccGroup713 {
    private static final ECParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp224r1");
    private static final ECPoint generator = spec.getG();
    private static final BigInteger order = spec.getCurve().getOrder();

    private static final SecureRandom random = new SecureRandom();
    private static final int symmetricKeyLength = 16;

    private EccGroup713() {}

    private static ECPoint powInGroup(ECPoint base, BigInteger exponent)
    {
        return base.multiply(exponent);
    }

    private static byte[] symKeyFromPrivateKey(PrivateKey privateKey) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bos.write("aes_key:".getBytes());
        bos.write(privateKey.toBytes());

        byte[] hash = digest.digest(bos.toByteArray());

        bos.close();

        return Arrays.copyOf(hash, EccGroup713.symmetricKeyLength);
    }

    private static BigInteger hb(byte[] symmetricKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException {
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

    class PrivateKey
    {
        private BigInteger underlyingValue;

        PrivateKey()
        {
            BigInteger secret;

            do {
                secret = new BigInteger(EccGroup713.order.bitLength(), EccGroup713.random);
            } while (secret.compareTo(EccGroup713.order) >= 0);

            this.underlyingValue = secret;
        }

        PrivateKey(PrivateKey base, BlindingFactor blindingFactor)
        {
            this.underlyingValue = blindingFactor.blind(base);
        }

        byte[] toBytes()
        {
            ECPoint point_representation = EccGroup713.powInGroup(EccGroup713.generator, this.underlyingValue);

            return point_representation.getEncoded(true);
        }
    }

    class PublicKey
    {
        private ECPoint underlyingValue;

        PublicKey(PrivateKey privateKey)
        {
            this.underlyingValue = EccGroup713.powInGroup(EccGroup713.generator,
                    privateKey.underlyingValue);
        }
    }

    class BlindingFactor
    {
        private BigInteger underlyingValue;

        BlindingFactor(PrivateKey privateKey) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
            this(EccGroup713.symKeyFromPrivateKey(privateKey));
        }

        BlindingFactor(byte[] symmetricKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeyException {
            this.underlyingValue = EccGroup713.hb(symmetricKey);
        }

        public ECPoint blind(PublicKey publicKey)
        {
            return EccGroup713.powInGroup(publicKey.underlyingValue, this.underlyingValue);
        }

        BigInteger blind(PrivateKey privateKey)
        {
            return privateKey.underlyingValue.multiply(this.underlyingValue);
        }
    }
}
