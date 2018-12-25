package com.anonudp.MixChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4AndPort {
    private InetAddress host;
    private int port;

    public IPv4AndPort(String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);

        this.port = port;
    }

    public InetAddress getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(this.host.getAddress());
        bos.write(this.port >> 8 & 0xFF);
        bos.write(this.port & 0xFF);

        return bos.toByteArray();
    }
}
