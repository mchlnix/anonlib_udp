package com.anonudp.MixChannel;

public interface PacketListener {
    public void receivePacket(byte[] udpPayload);
}
