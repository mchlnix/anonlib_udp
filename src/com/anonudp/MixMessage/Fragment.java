package com.anonudp.MixMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

class Fragment {
    public static final int FRAGMENT_DATA_PAYLOAD = 272;
    public static final int FRAGMENT_ID_SIZE = 2;
    private static final int FRAGMENT_INDEX_SIZE = 1;
    public static final int FRAGMENT_LENGTH = FRAGMENT_ID_SIZE + FRAGMENT_INDEX_SIZE + FRAGMENT_DATA_PAYLOAD;
    public static final int SINGLE_FRAGMENT_MESSAGE_ID = 0;
    public static final int SINGLE_FRAGMENT_FRAGMENT_NUMBER = 0;
    private static final int HAS_PADDING_BIT = 0x01;
    private static final int LAST_FRAGMENT_BIT = 0x02;

    private int message_id;
    private int fragment_number;
    private final boolean last;
    private byte[] payload;

    private Padding padding;

    public Fragment(int message_id, int fragment_number, byte[] payload, int payload_limit) {
        if (fragment_number == 0 && payload.length <= payload_limit + 1) {
            this.message_id = SINGLE_FRAGMENT_MESSAGE_ID;

            payload_limit += 1;
        }
        else {
            this.message_id = message_id;
        }

        this.last = payload.length <= payload_limit;

        this.fragment_number = fragment_number;

        this.padding = new Padding(payload_limit - payload.length);

        if (payload.length > payload_limit)
            this.payload = Arrays.copyOf(payload, payload_limit);
        else
            this.payload = payload;
    }

    public Fragment(byte[] fragment) {
        int current_offset = 0;

        this.message_id = fragment[current_offset];
        this.message_id <<= 8;

        ++current_offset;

        this.message_id += fragment[current_offset];
        this.message_id >>= 2;

        this.last = (fragment[current_offset] & LAST_FRAGMENT_BIT) > 0;

        if (message_id == 0 && !last)
            throw new IllegalArgumentException("Message should only contain one fragment, but the given fragment was not the last.");

        boolean has_padding = (fragment[current_offset] & HAS_PADDING_BIT) > 0;

        ++current_offset;

        if (message_id == 0) {
            this.fragment_number = 0;
        } else {
            this.fragment_number = fragment[current_offset] & 0xff;

            ++current_offset;
        }

        if (has_padding) {
            this.padding = new Padding(fragment, current_offset);

            current_offset += this.getPadding_bytes().length;
        }
        else
        {
            this.padding = new Padding(0);
        }

        int payload_length = FRAGMENT_LENGTH - current_offset - this.padding.getLength();

        this.payload = Arrays.copyOfRange(fragment, current_offset, current_offset + payload_length);
    }

    public int getMessage_id() {
        return message_id;
    }

    public int getFragment_number() {
        return fragment_number;
    }

    public boolean isLast() {
        return last;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getPadding_length() {
        return this.padding.getLength();
    }

    public byte[] getPadding_bytes() {
        return this.padding.getLengthAsBytes();
    }

    public byte[] toBytes() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int message_id_and_flags = this.message_id;

        message_id_and_flags <<= 2;

        if (this.last)
            message_id_and_flags |= LAST_FRAGMENT_BIT;

        if (this.getPadding_length() > 0)
            message_id_and_flags |= HAS_PADDING_BIT;

        bos.write(message_id_and_flags >> 8 & 0xFF);
        bos.write(message_id_and_flags & 0xFF);

        if (this.message_id != 0)
            bos.write(this.fragment_number);

        bos.write(this.getPadding_bytes());

        bos.write(this.payload);

        bos.write(this.padding.getPaddingBytes());

        return bos.toByteArray();
    }
}
