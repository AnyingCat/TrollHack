/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public record RandomChanceWithEnchantedBonusLootCondition(float unenchantedChance, EnchantmentLevelBasedValue enchantedChance, RegistryEntry<Enchantment> enchantment) implements LootCondition
{
    public static final MapCodec<RandomChanceWithEnchantedBonusLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("unenchanted_chance")).forGetter(RandomChanceWithEnchantedBonusLootCondition::unenchantedChance), ((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("enchanted_chance")).forGetter(RandomChanceWithEnchantedBonusLootCondition::enchantedChance), ((MapCodec)Enchantment.ENTRY_CODEC.fieldOf("enchantment")).forGetter(RandomChanceWithEnchantedBonusLootCondition::enchantment)).apply((Applicative<RandomChanceWithEnchantedBonusLootCondition, ?>)instance, RandomChanceWithEnchantedBonusLootCondition::new));

    @Override
    public LootConditionType getType() {
        return LootConditionTypes.RANDOM_CHANCE_WITH_ENCHANTED_BONUS;
    }

    @Override
    public Set<LootContextParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootContextParameters.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext lootContext) {
        int n;
        Entity entity = lootContext.get(LootContextParameters.ATTACKING_ENTITY);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            n = EnchantmentHelper.getEquipmentLevel(this.enchantment, livingEntity);
        } else {
            n = 0;
        }
        int i = n;
        float f = i > 0 ? this.enchantedChance.getValue(i) : this.unenchantedChance;
        return lootContext.getRandom().nextFloat() < f;
    }

    public static LootCondition.Builder builder(RegistryWrapper.WrapperLookup registryLookup, float base, float perLevelAboveFirst) {
        RegistryWrapper.Impl<Enchantment> impl = registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return () -> new RandomChanceWithEnchantedBonusLootCondition(base, new EnchantmentLevelBasedValue.Linear(base + perLevelAboveFirst, perLevelAboveFirst), impl.getOrThrow(Enchantments.LOOTING));
    }

    @Override
    public /* synthetic */ boolean test(Object context) {
        return this.test((LootContext)context);
    }
}

