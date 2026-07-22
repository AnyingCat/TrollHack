/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;

public enum Unit {
    INSTANCE;

    public static final Codec<Unit> CODEC;

    static {
        CODEC = Codec.unit(INSTANCE);
    }
}

