package com.anonudp;

public class TunDevice {
    public TunDevice()
    {

    }

    public void receiveResponse(byte[] response)
    {
        System.out.println(new String(response));
    }
}
