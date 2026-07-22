/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class DustParticleEffect
extends AbstractDustParticleEffect {
    public static final Vector3f RED = Vec3d.unpackRgb(0xFF0000).toVector3f();
    public static final DustParticleEffect DEFAULT = new DustParticleEffect(RED, 1.0f);
    public static final MapCodec<DustParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.VECTOR_3F.fieldOf("color")).forGetter(effect -> effect.color), ((MapCodec)SCALE_CODEC.fieldOf("scale")).forGetter(AbstractDustParticleEffect::getScale)).apply((Applicative<DustParticleEffect, ?>)instance, DustParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, DustParticleEffect> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VECTOR3F, effect -> effect.color, PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale, DustParticleEffect::new);
    private final Vector3f color;

    public DustParticleEffect(Vector3f color, float scale) {
        super(scale);
        this.color = color;
    }

    public ParticleType<DustParticleEffect> getType() {
        return ParticleTypes.DUST;
    }

    public Vector3f getColor() {
        return this.color;
    }
}

