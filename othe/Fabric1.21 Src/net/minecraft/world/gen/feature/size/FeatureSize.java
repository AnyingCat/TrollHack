/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.registry.Registries;
import net.minecraft.world.gen.feature.size.FeatureSizeType;

/**
 * In charge of determining the amount of space needed by a tree to generate.
 */
public abstract class FeatureSize {
    public static final Codec<FeatureSize> TYPE_CODEC = Registries.FEATURE_SIZE_TYPE.getCodec().dispatch(FeatureSize::getType, FeatureSizeType::getCodec);
    protected static final int field_31522 = 16;
    protected final OptionalInt minClippedHeight;

    protected static <S extends FeatureSize> RecordCodecBuilder<S, OptionalInt> createCodec() {
        return Codec.intRange(0, 80).optionalFieldOf("min_clipped_height").xmap(minClippedHeight -> minClippedHeight.map(OptionalInt::of).orElse(OptionalInt.empty()), minClippedHeight -> minClippedHeight.isPresent() ? Optional.of(minClippedHeight.getAsInt()) : Optional.empty()).forGetter(featureSize -> featureSize.minClippedHeight);
    }

    public FeatureSize(OptionalInt minClippedHeight) {
        this.minClippedHeight = minClippedHeight;
    }

    protected abstract FeatureSizeType<?> getType();

    /**
     * The radius that the tree needs to be empty or replaceable in order for it to generate.
     */
    public abstract int getRadius(int var1, int var2);

    public OptionalInt getMinClippedHeight() {
        return this.minClippedHeight;
    }
}

