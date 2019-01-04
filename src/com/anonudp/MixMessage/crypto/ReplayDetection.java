package com.anonudp.MixMessage.crypto;

import java.util.PriorityQueue;

public class ReplayDetection {
    public static final int SIZE = 20;
    private static final int START_VALUE = 0;
    private PriorityQueue<Integer> alreadySeen = new PriorityQueue<>(SIZE);

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
        boolean notTooSmall = ! (packetID < alreadySeen.peek());

        if (notAlreadySeen && notTooSmall)
        {
            alreadySeen.add(packetID);
            alreadySeen.poll();
        }

        return notAlreadySeen && notTooSmall;
    }
}