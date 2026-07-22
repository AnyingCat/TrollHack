/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.floatprovider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.random.Random;

public class ConstantFloatProvider
extends FloatProvider {
    public static final ConstantFloatProvider ZERO = new ConstantFloatProvider(0.0f);
    public static final MapCodec<ConstantFloatProvider> CODEC = ((MapCodec)Codec.FLOAT.fieldOf("value")).xmap(ConstantFloatProvider::create, ConstantFloatProvider::getValue);
    private final float value;

    public static ConstantFloatProvider create(float value) {
        if (value == 0.0f) {
            return ZERO;
        }
        return new ConstantFloatProvider(value);
    }

    private ConstantFloatProvider(float value) {
        this.value = value;
    }

    public float getValue() {
        return this.value;
    }

    @Override
    public float get(Random random) {
        return this.value;
    }

    @Override
    public float getMin() {
        return this.value;
    }

    @Override
    public float getMax() {
        return this.value;
    }

    @Override
    public FloatProviderType<?> getType() {
        return FloatProviderType.CONSTANT;
    }

    public String toString() {
        return Float.toString(this.value);
    }
}

