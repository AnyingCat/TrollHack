/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractDustParticleEffect
implements ParticleEffect {
    public static final float MIN_SCALE = 0.01f;
    public static final float MAX_SCALE = 4.0f;
    protected static final Codec<Float> SCALE_CODEC = Codec.FLOAT.validate(scale -> scale.floatValue() >= 0.01f && scale.floatValue() <= 4.0f ? DataResult.success(scale) : DataResult.error(() -> "Value must be within range [0.01;4.0]: " + scale));
    private final float scale;

    public AbstractDustParticleEffect(float scale) {
        this.scale = MathHelper.clamp(scale, 0.01f, 4.0f);
    }

    public float getScale() {
        return this.scale;
    }
}

