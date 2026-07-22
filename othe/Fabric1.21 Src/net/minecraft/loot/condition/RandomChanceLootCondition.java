/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public record RandomChanceLootCondition(LootNumberProvider chance) implements LootCondition
{
    public static final MapCodec<RandomChanceLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)LootNumberProviderTypes.CODEC.fieldOf("chance")).forGetter(RandomChanceLootCondition::chance)).apply((Applicative<RandomChanceLootCondition, ?>)instance, RandomChanceLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.RANDOM_CHANCE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        float f = this.chance.nextFloat(lootContext);
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootCondition.Builder builder(float chance) {
        return () -> new RandomChanceLootCondition(ConstantLootNumberProvider.create(chance));
    }

    public static LootCondition.Builder builder(LootNumberProvider chance) {
        return () -> new RandomChanceLootCondition(chance);
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

