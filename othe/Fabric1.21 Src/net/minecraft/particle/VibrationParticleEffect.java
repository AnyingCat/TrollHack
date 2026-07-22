/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.PositionSource;

public class VibrationParticleEffect
implements ParticleEffect {
    private static final Codec<PositionSource> POSITION_SOURCE_CODEC = PositionSource.CODEC.validate(positionSource -> positionSource instanceof EntityPositionSource ? DataResult.error(() -> "Entity position sources are not allowed") : DataResult.success(positionSource));
    public static final MapCodec<VibrationParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)POSITION_SOURCE_CODEC.fieldOf("destination")).forGetter(VibrationParticleEffect::getVibration), ((MapCodec)Codec.INT.fieldOf("arrival_in_ticks")).forGetter(VibrationParticleEffect::getArrivalInTicks)).apply((Applicative<VibrationParticleEffect, ?>)instance, VibrationParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, VibrationParticleEffect> PACKET_CODEC = PacketCodec.tuple(PositionSource.PACKET_CODEC, VibrationParticleEffect::getVibration, PacketCodecs.VAR_INT, VibrationParticleEffect::getArrivalInTicks, VibrationParticleEffect::new);
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleEffect(PositionSource destination, int arrivalInTicks) {
        this.destination = destination;
        this.arrivalInTicks = arrivalInTicks;
    }

    public ParticleType<VibrationParticleEffect> getType() {
        return ParticleTypes.VIBRATION;
    }

    public PositionSource getVibration() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}

