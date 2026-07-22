/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.encoding;

import io.netty.buffer.ByteBuf;

public class VarInts {
    private static final int MAX_BYTES = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int MORE_BITS_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    public static int getSizeInBytes(int i) {
        for (int j = 1; j < MAX_BYTES; ++j) {
            if ((i & -1 << j * 7) != 0) continue;
            return j;
        }
        return MAX_BYTES;
    }

    public static boolean shouldContinueRead(byte b) {
        return (b & 0x80) == 128;
    }

    public static int read(ByteBuf buf) {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = buf.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while (VarInts.shouldContinueRead(b));
        return i;
    }

    public static ByteBuf write(ByteBuf buf, int i) {
        while (true) {
            if ((i & 0xFFFFFF80) == 0) {
                buf.writeByte(i);
                return buf;
            }
            buf.writeByte(i & 0x7F | 0x80);
            i >>>= 7;
        }
    }
}

