/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.codec;

/**
 * A functional interface that, given a buffer and a value, encodes it.
 * 
 * <p>A static method taking {@link net.minecraft.network.PacketByteBuf} and the
 * value as the arguments can be used as an encoder.
 * 
 * @see PacketDecoder
 * @see ValueFirstEncoder
 */
@FunctionalInterface
public interface PacketEncoder<O, T> {
    public void encode(O var1, T var2);
}

