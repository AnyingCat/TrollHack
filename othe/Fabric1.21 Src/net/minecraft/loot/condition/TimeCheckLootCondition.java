/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.server.world.ServerWorld;

public record TimeCheckLootCondition(Optional<Long> period, BoundedIntUnaryOperator value) implements LootCondition
{
    public static final MapCodec<TimeCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.LONG.optionalFieldOf("period").forGetter(TimeCheckLootCondition::period), ((MapCodec)BoundedIntUnaryOperator.CODEC.fieldOf("value")).forGetter(TimeCheckLootCondition::value)).apply((Applicative<TimeCheckLootCondition, ?>)instance, TimeCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.TIME_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return this.value.getRequiredParameters();
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerWorld serverWorld = lootContext.getWorld();
        long l = serverWorld.getTimeOfDay();
        if (this.period.isPresent()) {
            l %= this.period.get().longValue();
        }
        return this.value.test(lootContext, (int)l);
    }

    public static Builder create(BoundedIntUnaryOperator value) {
        return new Builder(value);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }

    public static class Builder
    implements LootCondition.Builder {
        private Optional<Long> period = Optional.empty();
        private final BoundedIntUnaryOperator value;

        public Builder(BoundedIntUnaryOperator value) {
            this.value = value;
        }

        public Builder period(long period) {
            this.period = Optional.of(period);
            return this;
        }

        @Override
        public TimeCheckLootCondition build() {
            return new TimeCheckLootCondition(this.period, this.value);
        }

        @Override
        public /* synthetic */ LootCondition build() {
            return this.build();
        }
    }
}

