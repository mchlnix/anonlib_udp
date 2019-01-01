package com.anonudp.MixMessage;

import com.anonudp.MixMessage.crypto.Counter;
import com.anonudp.MixMessage.crypto.EccGroup713;
import com.anonudp.MixMessage.crypto.PublicKey;
import com.anonudp.MixPacket.InitPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.anonudp.Constants.MIX_SERVER_COUNT;

public class Fragment {
    public static final int DATA_OVERHEAD = (MIX_SERVER_COUNT - 1) * Counter.CTR_PREFIX_SIZE;
    private static final int INIT_OVERHEAD = PublicKey.SIZE + MIX_SERVER_COUNT * EccGroup713.SYMMETRIC_KEY_LENGTH + InitPacket.PAYLOAD_SIZE;

    private static final int DUMMY_PAYLOAD_SIZE = 0;
    public static final int DATA_PAYLOAD_SIZE = 274;
    public static final int INIT_PAYLOAD_SIZE = DATA_PAYLOAD_SIZE - (INIT_OVERHEAD - DATA_OVERHEAD);

    static final int ID_SIZE = 2;
    private static final int INDEX_SIZE = 1;
    private static final int HEADER_SIZE = ID_SIZE + INDEX_SIZE;

    public static final int SIZE_DATA = HEADER_SIZE + DATA_PAYLOAD_SIZE;
    public static final int SIZE_INIT = HEADER_SIZE + INIT_PAYLOAD_SIZE;

    static final int SINGLE_FRAGMENT_MESSAGE_ID = 0;
    static final int SINGLE_FRAGMENT_FRAGMENT_NUMBER = 0;

    private static final int BIT_HAS_PADDING = 0x01;
    private static final int BIT_LAST_FRAGMENT = 0x02;

    private int message_id;
    private int fragment_number;
    private final boolean last;
    private byte[] payload;

    private boolean bytesCached;
    private byte[] byteArrayCache;

    private Padding padding;

    public Fragment()
    {
        this(SINGLE_FRAGMENT_MESSAGE_ID, SINGLE_FRAGMENT_FRAGMENT_NUMBER, new byte[0], DUMMY_PAYLOAD_SIZE);
    }

    public Fragment(int message_id, int fragment_number, byte[] payload, int payload_limit) {
        if (payload_limit != INIT_PAYLOAD_SIZE && payload_limit != DATA_PAYLOAD_SIZE && payload_limit != DUMMY_PAYLOAD_SIZE)
            throw new IllegalArgumentException("Payload limit is not an accepted value. Use Fragment.*_PAYLOAD_SIZE instead.");

        this.bytesCached = false;
        this.byteArrayCache = new byte[HEADER_SIZE + payload_limit];

        // TODO: Isn't this only necessary for 275 Byte fragments?
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

    public Fragment(byte[] fragment)
    {
        this(fragment, SIZE_DATA);
    }

    public Fragment(byte[] fragment, int length) {
        this.bytesCached = false;
        this.byteArrayCache = new byte[fragment.length];

        int current_offset = 0;

        this.message_id = fragment[current_offset];
        this.message_id <<= 8;

        ++current_offset;

        this.message_id += fragment[current_offset];
        this.message_id >>= 2;

        this.last = (fragment[current_offset] & BIT_LAST_FRAGMENT) > 0;

        if (message_id == 0 && !last)
            throw new IllegalArgumentException("Message should only contain one fragment, but the given fragment was not the last.");

        boolean has_padding = (fragment[current_offset] & BIT_HAS_PADDING) > 0;

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

        // we only receive data fragments, so SIZE_DATA is the length of meaningful bytes, the rest
        int payload_length = length - current_offset - this.padding.getLength();

        this.payload = Arrays.copyOfRange(fragment, current_offset, current_offset + payload_length);
    }

    int getMessage_id() {
        return message_id;
    }

    int getFragment_number() {
        return fragment_number;
    }

    boolean isLast() {
        return last;
    }

    public byte[] getPayload() {
        return payload;
    }

    int getPadding_length() {
        return this.padding.getLength();
    }

    byte[] getPadding_bytes() {
        return this.padding.getLengthAsBytes();
    }

    public byte[] toBytes() throws IOException
    {
        if (! this.bytesCached)
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            int message_id_and_flags = this.message_id;

            message_id_and_flags <<= 2;

            if (this.last)
                message_id_and_flags |= BIT_LAST_FRAGMENT;

            if (this.getPadding_length() > 0)
                message_id_and_flags |= BIT_HAS_PADDING;

            bos.write(message_id_and_flags >> 8 & 0xFF);
            bos.write(message_id_and_flags & 0xFF);

            if (this.message_id != 0)
                bos.write(this.fragment_number);

            bos.write(this.getPadding_bytes());

            bos.write(this.payload);

            bos.write(this.padding.getPaddingBytes());

            assert bos.size() == this.byteArrayCache.length;

            this.byteArrayCache = bos.toByteArray();
        }

        return this.byteArrayCache;
    }
}
