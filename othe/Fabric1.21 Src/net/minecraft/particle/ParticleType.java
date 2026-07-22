/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;

public abstract class ParticleType<T extends ParticleEffect> {
    private final boolean alwaysShow;

    protected ParticleType(boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    public boolean shouldAlwaysSpawn() {
        return this.alwaysShow;
    }

    public abstract MapCodec<T> getCodec();

    public abstract PacketCodec<? super RegistryByteBuf, T> getPacketCodec();
}

