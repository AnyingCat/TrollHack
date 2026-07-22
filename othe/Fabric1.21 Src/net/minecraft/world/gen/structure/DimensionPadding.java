/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.dynamic.Codecs;

public record DimensionPadding(int bottom, int top) {
    private static final Codec<DimensionPadding> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.NONNEGATIVE_INT.lenientOptionalFieldOf("bottom", 0).forGetter(padding -> padding.bottom), Codecs.NONNEGATIVE_INT.lenientOptionalFieldOf("top", 0).forGetter(padding -> padding.top)).apply((Applicative<DimensionPadding, ?>)instance, DimensionPadding::new));
    public static final Codec<DimensionPadding> CODEC = Codec.either(Codecs.NONNEGATIVE_INT, OBJECT_CODEC).xmap(either -> either.map(DimensionPadding::new, Function.identity()), padding -> padding.paddedBySameDistance() ? Either.left(padding.bottom) : Either.right(padding));
    public static final DimensionPadding NONE = new DimensionPadding(0);

    public DimensionPadding(int value) {
        this(value, value);
    }

    public boolean paddedBySameDistance() {
        return this.top == this.bottom;
    }
}

