package com.company;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


public class Main {

    public static void main(String[] args) {
        int FRAGMENT_SIZE = 273;

        int message_id = 0;
        byte has_padding = 0x01;
        byte last_fragment = 0x02;

        byte[] message_id_bytes = new byte[2];

        message_id_bytes[0] = (byte) (message_id >> 6);
        message_id_bytes[1] = (byte) ((message_id << 2) | has_padding | last_fragment);

        byte[] padding_size = {0x01, (byte) 0xFE}; // 256 padding bytes = 254

        byte[] payload = new byte[FRAGMENT_SIZE - 256];
        Arrays.fill(payload, (byte) 0x01);

        byte[] padding = new byte[254];

        try(FileOutputStream fos = new FileOutputStream("/home/michael/Schreibtisch/packet.bytes"))
        {
            fos.write(message_id_bytes);
            // no fragment byte
            fos.write(padding_size);
            fos.write(payload);
            fos.write(padding);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
