package com.anonudp.MixChannel;

public interface PacketListener {
    public void receivePacket(int id, byte[] udpPayload);
}
