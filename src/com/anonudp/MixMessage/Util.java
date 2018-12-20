package com.anonudp.MixMessage;

import java.security.SecureRandom;

public class Util {
    private static final SecureRandom random = new SecureRandom();

    static double log2(double num) {
        return (Math.log(num) / Math.log(2));
    }

    public static byte[] randomBytes(int numberOfBytes)
    {
        byte[] randomBytes = new byte[numberOfBytes];

        Util.random.nextBytes(randomBytes);

        return randomBytes;
    }
}
