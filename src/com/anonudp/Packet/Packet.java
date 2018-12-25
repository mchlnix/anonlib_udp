package com.anonudp.Packet;

import java.io.IOException;

public interface Packet {
    byte TYPE_DATA = 0x01;
    byte TYPE_INIT = 0x02;
    byte TYPE_INIT_RESPONSE = 0x03;

    byte getPacketType();

    byte[] getCTRPrefix();

    byte[] getChannelID();

    byte[] getData() throws IOException;
}
