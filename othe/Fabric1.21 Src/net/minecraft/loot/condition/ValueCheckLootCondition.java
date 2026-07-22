/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public record ValueCheckLootCondition(LootNumberProvider value, BoundedIntUnaryOperator range) implements LootCondition
{
    public static final MapCodec<ValueCheckLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("value")).forGetter(ValueCheckLootCondition::value), ((MapCodec)BoundedIntUnaryOperator.CODEC.fieldOf("range")).forGetter(ValueCheckLootCondition::range)).apply((Applicative<ValueCheckLootCondition, ?>)instance, ValueCheckLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return Sets.union(this.value.getRequiredParameters(), this.range.getRequiredParameters());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return this.range.test(lootContext, this.value.nextInt(lootContext));
    }

    public static LootCondition.Builder builder(LootNumberProvider value, BoundedIntUnaryOperator range) {
        return () -> new ValueCheckLootCondition(value, range);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

