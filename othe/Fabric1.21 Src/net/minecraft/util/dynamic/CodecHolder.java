/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.dynamic;

import com.mojang.serialization.MapCodec;

public record CodecHolder<A>(MapCodec<A> codec) {
    public static <A> CodecHolder<A> of(MapCodec<A> mapCodec) {
        return new CodecHolder<A>(mapCodec);
    }
}

