package com.anonudp.MixMessage.crypto;

import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class Util {
    static final int GCM_MAC_SIZE = 16;
    static final int IV_SIZE = 16;

    @Deprecated
    public static PaddedBufferedBlockCipher createCTRCipher(byte[] symmetricKey, byte[] iv, boolean shouldEncrypt) {
        PaddedBufferedBlockCipher cipher;

        cipher = new PaddedBufferedBlockCipher(new SICBlockCipher(new AESFastEngine()));

        cipher.init(shouldEncrypt, new ParametersWithIV(new KeyParameter(symmetricKey), iv));

        return cipher;
    }

    static AEADBlockCipher createGCM(byte[] symmetricKey, byte[] iv, boolean shouldEncrypt) {
        GCMBlockCipher cipher;

        cipher = new GCMBlockCipher(new AESFastEngine());

        AEADParameters keySpec = new AEADParameters(new KeyParameter(symmetricKey), Byte.SIZE * GCM_MAC_SIZE, iv, null);

        cipher.init(shouldEncrypt, keySpec);

        return cipher;
    }
}
