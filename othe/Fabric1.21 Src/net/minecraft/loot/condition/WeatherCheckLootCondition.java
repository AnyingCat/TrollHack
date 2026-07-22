/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;

public record WeatherCheckLootCondition(Optional<Boolean> raining, Optional<Boolean> thundering) implements LootCondition
{
    public static final MapCodec<WeatherCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheckLootCondition::raining), Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheckLootCondition::thundering)).apply((Applicative<WeatherCheckLootCondition, ?>)instance, WeatherCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.WEATHER_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld serverWorld = lootContext.getWorld();
        if (this.raining.isPresent() && this.raining.get().booleanValue() != serverWorld.isRaining()) {
            return false;
        }
        return !this.thundering.isPresent() || this.thundering.get().booleanValue() == serverWorld.isThundering();
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private Optional<Boolean> raining = Optional.empty();
        private Optional<Boolean> thundering = Optional.empty();

        public Builder raining(boolean raining) {
            this.raining = Optional.of(raining);
            return this;
        }

        public Builder thundering(boolean thundering) {
            this.thundering = Optional.of(thundering);
            return this;
        }

        @Override
        public WeatherCheckLootCondition build() {
            return new WeatherCheckLootCondition(this.raining, this.thundering);
        }

        @Override
        public /* synthetic */ LootCondition build() {
            return this.build();
        }
    }
}

