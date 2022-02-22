package com.github.oam.utils;

import java.util.Arrays;

public class IntTo3ByteCodec {

    public static int[] encode(final byte[] bytes) {
        int[] ints = new int[(int) Math.ceil(bytes.length / 3D)];
        for (int i = 0; i < ints.length; i++) {
            int bi = i * 3;
            byte b1 = bytes[bi];
            Byte b2 = (bi + 1 < bytes.length ? bytes[bi + 1] : null);
            Byte b3 = (bi + 2 < bytes.length ? bytes[bi + 2] : null);

            int out = 1 << 31;
            out |= 1 << 30;
            out |= (b1 & 255) << 16;
            if (b2 != null) {
                out |= 1 << 29;
                out |= (b2 & 255) << 8;
            }
            if (b3 != null) {
                out |= 1 << 28;
                out |= (b3 & 255);
            }

            ints[i] = out;
        }

        return ints;
    }

    public static byte[] decode(final int[] ints) {
        byte[] bytes = new byte[ints.length * 3];
        int cnt = 0;
        for (int i : ints) {
            int b1 = i >> 16 & 255;
            int b2 = i >> 8 & 255;
            int b3 = i & 255;

            if ((i >> 30 & 1) == 1) {
                bytes[cnt] = (byte) b1;
                cnt++;
            }
            if ((i >> 29 & 1) == 1) {
                bytes[cnt] = (byte) b2;
                cnt++;
            }
            if ((i >> 28 & 1) == 1) {
                bytes[cnt] = (byte) b3;
                cnt++;
            }
        }

        return Arrays.copyOfRange(bytes, 0, cnt);
    }

    public static byte[] decode(final int i) {
        return decode(new int[]{i});
    }

}
