package com.anonudp.MixMessage;

public class Fragment {
    private int message_id;
    private int fragment_number;
    private boolean last_fragment;
    private byte[] payload;
    private int padding_length;

    public Fragment(int message_id, int fragment_number, boolean is_last_fragment, byte[] payload, int payload_limit) {
        this.message_id = message_id;
        this.fragment_number = fragment_number;
        this.last_fragment = is_last_fragment;
        this.payload = payload;
        byte[] padding_bytes = padding_to_bytes(payload_limit - payload.length);
        this.padding_length = padding_from_bytes(padding_bytes);
    }

    public int getMessage_id() {
        return message_id;
    }

    public int getFragment_number() {
        return fragment_number;
    }

    public boolean isLast_fragment() {
        return last_fragment;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPadding_length() {
        return padding_length;
    }

    private static byte[] padding_to_bytes(int padding_length)
    {
        if (padding_length < 0)
            throw new IllegalArgumentException("Padding must be >=0.");

        if (padding_length == 0)
            return new byte[0];

        return new byte[0];
    }

    private static int padding_from_bytes(byte[] padding_bytes)
    {
        if (padding_bytes.length == 0)
            throw new IllegalArgumentException("No padding bytes given as argument.");

        int padding = 0;

        for ( )


        return 0;
    }
}
