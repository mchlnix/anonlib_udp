package com.anonudp;

import com.anonudp.MixChannel.PacketListener;

public class TunDevice implements PacketListener {
    @Override
    public void receivePacket(byte[] udpPayload) {
        System.out.println(new String(udpPayload));
    }
}
