package com.anonudp.MixMessage;

import java.util.Arrays;

import static com.anonudp.MixMessage.Util.log2;

public class Padding {
    private static final int END_BIT = 0x80;
    private static final int VALUE_MASK = 0x7F;

    private byte[] byte_representation;

    private int internal_padding_length;

    public Padding(int padding_length)
    {
        if (padding_length <= 0) {
            this.internal_padding_length = 0;
            this.byte_representation = new byte[0];
        } else if (padding_length == 1) {
            this.internal_padding_length = 0;
            this.byte_representation = new byte[]{(byte) 0x80}; // padding size byte is already padding
        } else {
            int padding_bytes_needed = (int) Math.ceil(log2(padding_length) / 7);

            padding_length -= padding_bytes_needed;

            this.internal_padding_length = padding_length;

            this.byte_representation = new byte[padding_bytes_needed];

            int padding_byte_index = 1;

            while (padding_length > 0) {
                byte_representation[byte_representation.length - padding_byte_index] = ((byte) (padding_length & VALUE_MASK));

                padding_length >>= 7;

                ++padding_byte_index;
            }

            this.byte_representation[byte_representation.length - 1] |= END_BIT;
        }
    }

    public Padding(byte[] padding_byte_representation)
    {
        this(padding_byte_representation, 0);
    }

    Padding(byte[] padding_byte_representation, int offset)
    {
        if (padding_byte_representation.length == 0)
            throw new IllegalArgumentException("No padding bytes given as argument.");

        int padding_end_mask = 0x80;
        int padding_value_mask = 0x7F;

        this.internal_padding_length = 0;

        int bytes_read = 0;

        for (int i = offset; i < padding_byte_representation.length; ++i) {
            this.internal_padding_length += padding_byte_representation[i] & padding_value_mask;

            ++bytes_read;

            if ((padding_byte_representation[i] & padding_end_mask) == padding_end_mask)
                break;

            this.internal_padding_length <<= 7;
        }

        this.byte_representation = Arrays.copyOfRange(padding_byte_representation, offset, offset + bytes_read);
    }

    public byte[] getLengthAsBytes() {
        return byte_representation;
    }

    public byte[] getPaddingBytes()
    {
        // todo: make padding bytes random
        return new byte[this.internal_padding_length];
    }

    public int getLength()
    {
        return this.internal_padding_length;
    }
}
