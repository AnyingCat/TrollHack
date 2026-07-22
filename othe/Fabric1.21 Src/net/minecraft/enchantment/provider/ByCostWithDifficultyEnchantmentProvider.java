/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.provider;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;

public record ByCostWithDifficultyEnchantmentProvider(RegistryEntryList<Enchantment> enchantments, int minCost, int maxCostSpan) implements EnchantmentProvider
{
    public static final int MAX_COST = 10000;
    public static final MapCodec<ByCostWithDifficultyEnchantmentProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.ENCHANTMENT).fieldOf("enchantments")).forGetter(ByCostWithDifficultyEnchantmentProvider::enchantments), ((MapCodec)Codecs.rangedInt(1, 10000).fieldOf("min_cost")).forGetter(ByCostWithDifficultyEnchantmentProvider::minCost), ((MapCodec)Codecs.rangedInt(0, 10000).fieldOf("max_cost_span")).forGetter(ByCostWithDifficultyEnchantmentProvider::maxCostSpan)).apply((Applicative<ByCostWithDifficultyEnchantmentProvider, ?>)instance, ByCostWithDifficultyEnchantmentProvider::new));

    @Override
    public void provideEnchantments(ItemStack stack, ItemEnchantmentsComponent.Builder componentBuilder, Random random, LocalDifficulty localDifficulty) {
        float f = localDifficulty.getClampedLocalDifficulty();
        int i = MathHelper.nextBetween(random, this.minCost, this.minCost + (int)(f * (float)this.maxCostSpan));
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, i, this.enchantments.stream());
        for (EnchantmentLevelEntry enchantmentLevelEntry : list) {
            componentBuilder.add(enchantmentLevelEntry.enchantment, enchantmentLevelEntry.level);
        }
    }

    public MapCodec<ByCostWithDifficultyEnchantmentProvider> getCodec() {
        return CODEC;
    }
}

