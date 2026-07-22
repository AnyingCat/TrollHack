/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public class ShriekParticleEffect
implements ParticleEffect {
    public static final MapCodec<ShriekParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("delay")).forGetter(particleEffect -> particleEffect.delay)).apply((Applicative<ShriekParticleEffect, ?>)instance, ShriekParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, ShriekParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, effect -> effect.delay, ShriekParticleEffect::new);
    private final int delay;

    public ShriekParticleEffect(int delay) {
        this.delay = delay;
    }

    public ParticleType<ShriekParticleEffect> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}

