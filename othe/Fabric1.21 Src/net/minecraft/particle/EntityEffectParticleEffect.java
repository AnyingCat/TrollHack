/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;

public class EntityEffectParticleEffect
implements ParticleEffect {
    private final ParticleType<EntityEffectParticleEffect> type;
    private final int color;

    public static MapCodec<EntityEffectParticleEffect> createCodec(ParticleType<EntityEffectParticleEffect> type) {
        return Codecs.ARGB.xmap(color -> new EntityEffectParticleEffect(type, (int)color), effect -> effect.color).fieldOf("color");
    }

    public static PacketCodec<? super ByteBuf, EntityEffectParticleEffect> createPacketCodec(ParticleType<EntityEffectParticleEffect> type) {
        return PacketCodecs.INTEGER.xmap(color -> new EntityEffectParticleEffect(type, (int)color), particleEffect -> particleEffect.color);
    }

    private EntityEffectParticleEffect(ParticleType<EntityEffectParticleEffect> type, int color) {
        this.type = type;
        this.color = color;
    }

    public ParticleType<EntityEffectParticleEffect> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)ColorHelper.Argb.getRed(this.color) / 255.0f;
    }

    public float getGreen() {
        return (float)ColorHelper.Argb.getGreen(this.color) / 255.0f;
    }

    public float getBlue() {
        return (float)ColorHelper.Argb.getBlue(this.color) / 255.0f;
    }

    public float getAlpha() {
        return (float)ColorHelper.Argb.getAlpha(this.color) / 255.0f;
    }

    public static EntityEffectParticleEffect create(ParticleType<EntityEffectParticleEffect> type, int color) {
        return new EntityEffectParticleEffect(type, color);
    }

    public static EntityEffectParticleEffect create(ParticleType<EntityEffectParticleEffect> type, float r, float g, float b) {
        return EntityEffectParticleEffect.create(type, ColorHelper.Argb.fromFloats(1.0f, r, g, b));
    }
}

