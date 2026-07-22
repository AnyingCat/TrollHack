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

public record SculkChargeParticleEffect(float roll) implements ParticleEffect
{
    public static final MapCodec<SculkChargeParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.FLOAT.fieldOf("roll")).forGetter(particleEffect -> Float.valueOf(particleEffect.roll))).apply((Applicative<SculkChargeParticleEffect, ?>)instance, SculkChargeParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, SculkChargeParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.FLOAT, effect -> Float.valueOf(effect.roll), SculkChargeParticleEffect::new);

    public ParticleType<SculkChargeParticleEffect> getType() {
        return ParticleTypes.SCULK_CHARGE;
    }
}

