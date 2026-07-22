/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.predicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public record LightPredicate(NumberRange.IntRange range) {
    public static final Codec<LightPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(NumberRange.IntRange.CODEC.optionalFieldOf("light", NumberRange.IntRange.ANY).forGetter(LightPredicate::range)).apply((Applicative<LightPredicate, ?>)instance, LightPredicate::new));

    public boolean test(ServerWorld world, BlockPos pos) {
        if (!world.canSetBlock(pos)) {
            return false;
        }
        return this.range.test(world.getLightLevel(pos));
    }

    public static class Builder {
        private NumberRange.IntRange light = NumberRange.IntRange.ANY;

        public static Builder create() {
            return new Builder();
        }

        public Builder light(NumberRange.IntRange light) {
            this.light = light;
            return this;
        }

        public LightPredicate build() {
            return new LightPredicate(this.light);
        }
    }
}

