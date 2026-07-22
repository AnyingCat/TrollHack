/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

/**
 * A functional interface that, given a buffer, decodes a value.
 * 
 * <p>A constructor taking {@link net.minecraft.network.PacketByteBuf} as an
 * argument can be used as a decoder.
 * 
 * @see PacketEncoder
 * @see ValueFirstEncoder
 */
@FunctionalInterface
public interface PacketDecoder<I, T> {
    public T decode(I var1);
}

