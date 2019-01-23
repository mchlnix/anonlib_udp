package com.anonudp.MixMessage;

import java.security.SecureRandom;

public class Util {
    private static final SecureRandom random = new SecureRandom();

    static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }

    public static int bytesToUnsignedInt(byte[] integerInBytes)
    {
        int ret = 0;

        for (int i = 0; i < integerInBytes.length; ++i)
        {
            ret += (integerInBytes[i] + 256) % 256 << (integerInBytes.length - 1 - i) * 8;
        }

        return ret;
    }

    public static byte[] randomBytes(int numberOfBytes)
    {
        byte[] randomBytes = new byte[numberOfBytes];

        Util.random.nextBytes(randomBytes);

        return randomBytes;
    }
}
