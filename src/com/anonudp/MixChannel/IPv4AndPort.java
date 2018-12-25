package com.anonudp.MixChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4AndPort {
    private InetAddress host;
    private int port;

    public IPv4AndPort(String host, int port) throws UnknownHostException {
        this.host = InetAddress.getByName(host);

        this.port = port;
    }

    InetAddress getHost()
    {
        return this.host;
    }

    int getPort()
    {
        return this.port;
    }
}
