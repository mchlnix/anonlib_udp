package com.anonudp.MixPacket;

import java.io.IOException;

public interface IPacket {
    byte TYPE_DATA = 0x01;
    byte TYPE_INIT = 0x02;
    byte TYPE_INIT_RESPONSE = 0x03;

    byte getPacketType();

    byte[] getCTRPrefix();

    byte[] getChannelID();

    byte[] getData() throws IOException;
}
