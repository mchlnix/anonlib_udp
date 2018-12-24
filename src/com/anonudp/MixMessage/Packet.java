package com.anonudp.MixMessage;

import java.io.IOException;

public interface Packet {
    byte getPacketType();

    byte[] getCTRPrefix();

    byte[] getChannelID();

    byte[] getData() throws IOException;
}
