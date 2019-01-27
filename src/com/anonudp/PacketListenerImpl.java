package com.anonudp;

import com.anonudp.MixChannel.PacketListener;

public class PacketListenerImpl implements PacketListener {
    @Override
    public void receivePacket(int channelID, byte[] udpPayload) {
        System.out.println("From " + channelID + ": " + new String(udpPayload));
    }
}
