package com.anonudp.MixMessage.crypto;

import java.util.ArrayList;
import java.util.Collections;

public class ReplayDetection {
    public static final int SIZE = 20;
    private static final int START_VALUE = 0;
    private static final int SMALLEST_ELEMENT = 0;
    private final ArrayList alreadySeen = new ArrayList(SIZE + 1);

    public ReplayDetection()
    {
        for ( int i = 0; i < SIZE; ++i)
            alreadySeen.add(START_VALUE);
    }

    public int size()
    {
        return alreadySeen.size();
    }

    public boolean isValid(byte[] counterPrefix)
    {
        int packetID = new Counter(counterPrefix).asInt();

        return isValid(packetID);
    }

    public boolean isValid(int packetID)
    {
        if (alreadySeen.isEmpty())
            return false;

        boolean notAlreadySeen = ! alreadySeen.contains(packetID);
        boolean notTooSmall = ! (Integer.compareUnsigned(packetID, (int) alreadySeen.get(SMALLEST_ELEMENT)) < 0);

        if (notAlreadySeen && notTooSmall)
        {
            alreadySeen.add(packetID);
            Collections.sort(alreadySeen);
            alreadySeen.remove(SMALLEST_ELEMENT);
        }

        return notAlreadySeen && notTooSmall;
    }
}
