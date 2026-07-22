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
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;

public record ByCostEnchantmentProvider(RegistryEntryList<Enchantment> enchantments, IntProvider cost) implements EnchantmentProvider
{
    public static final MapCodec<ByCostEnchantmentProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)RegistryCodecs.entryList(RegistryKeys.ENCHANTMENT).fieldOf("enchantments")).forGetter(ByCostEnchantmentProvider::enchantments), ((MapCodec)IntProvider.VALUE_CODEC.fieldOf("cost")).forGetter(ByCostEnchantmentProvider::cost)).apply((Applicative<ByCostEnchantmentProvider, ?>)instance, ByCostEnchantmentProvider::new));

    @Override
    public void provideEnchantments(ItemStack stack, ItemEnchantmentsComponent.Builder componentBuilder, Random random, LocalDifficulty localDifficulty) {
        List<EnchantmentLevelEntry> list = EnchantmentHelper.generateEnchantments(random, stack, this.cost.get(random), this.enchantments.stream());
        for (EnchantmentLevelEntry enchantmentLevelEntry : list) {
            componentBuilder.add(enchantmentLevelEntry.enchantment, enchantmentLevelEntry.level);
        }
    }

    public MapCodec<ByCostEnchantmentProvider> getCodec() {
        return CODEC;
    }
}

