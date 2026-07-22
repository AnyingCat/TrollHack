/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

/**
 * A functional interface that, given a value and a buffer, encodes it.
 * 
 * <p>An instance method taking {@link net.minecraft.network.PacketByteBuf} as an
 * argument can be used as a value-first encoder.
 * 
 * @see PacketDecoder
 * @see PacketEncoder
 */
@FunctionalInterface
public interface ValueFirstEncoder<O, T> {
    public void encode(T var1, O var2);
}

