/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.provider;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.provider.ByCostEnchantmentProvider;
import net.minecraft.enchantment.provider.ByCostWithDifficultyEnchantmentProvider;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.enchantment.provider.SingleEnchantmentProvider;
import net.minecraft.registry.Registry;

public interface EnchantmentProviderType {
    public static MapCodec<? extends EnchantmentProvider> registerAndGetDefault(Registry<MapCodec<? extends EnchantmentProvider>> registry) {
        Registry.register(registry, "by_cost", ByCostEnchantmentProvider.CODEC);
        Registry.register(registry, "by_cost_with_difficulty", ByCostWithDifficultyEnchantmentProvider.CODEC);
        return Registry.register(registry, "single", SingleEnchantmentProvider.CODEC);
    }
}

