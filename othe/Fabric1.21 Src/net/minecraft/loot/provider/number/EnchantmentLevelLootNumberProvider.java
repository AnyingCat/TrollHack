/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.provider.number;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;

public record EnchantmentLevelLootNumberProvider(EnchantmentLevelBasedValue amount) implements LootNumberProvider
{
    public static final MapCodec<EnchantmentLevelLootNumberProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)EnchantmentLevelBasedValue.CODEC.fieldOf("amount")).forGetter(EnchantmentLevelLootNumberProvider::amount)).apply((Applicative<EnchantmentLevelLootNumberProvider, ?>)instance, EnchantmentLevelLootNumberProvider::new));

    @Override
    public float nextFloat(LootContext context) {
        int i = context.requireParameter(LootContextParameters.ENCHANTMENT_LEVEL);
        return this.amount.getValue(i);
    }

    @Override
    public LootNumberProviderType getType() {
        return LootNumberProviderTypes.ENCHANTMENT_LEVEL;
    }

    public static EnchantmentLevelLootNumberProvider create(EnchantmentLevelBasedValue amount) {
        return new EnchantmentLevelLootNumberProvider(amount);
    }
}

