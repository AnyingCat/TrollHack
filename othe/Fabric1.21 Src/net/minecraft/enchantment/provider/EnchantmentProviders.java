/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.enchantment.provider;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.provider.ByCostWithDifficultyEnchantmentProvider;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.enchantment.provider.SingleEnchantmentProvider;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

public interface EnchantmentProviders {
    public static final RegistryKey<EnchantmentProvider> MOB_SPAWN_EQUIPMENT = EnchantmentProviders.of("mob_spawn_equipment");
    public static final RegistryKey<EnchantmentProvider> PILLAGER_SPAWN_CROSSBOW = EnchantmentProviders.of("pillager_spawn_crossbow");
    public static final RegistryKey<EnchantmentProvider> PILLAGER_POST_WAVE_3_RAID = EnchantmentProviders.of("raid/pillager_post_wave_3");
    public static final RegistryKey<EnchantmentProvider> PILLAGER_POST_WAVE_5_RAID = EnchantmentProviders.of("raid/pillager_post_wave_5");
    public static final RegistryKey<EnchantmentProvider> VINDICATOR_RAID = EnchantmentProviders.of("raid/vindicator");
    public static final RegistryKey<EnchantmentProvider> VINDICATOR_POST_WAVE_5_RAID = EnchantmentProviders.of("raid/vindicator_post_wave_5");
    public static final RegistryKey<EnchantmentProvider> ENDERMAN_LOOT_DROP = EnchantmentProviders.of("enderman_loot_drop");

    public static void bootstrap(Registerable<EnchantmentProvider> registry) {
        RegistryEntryLookup<Enchantment> registryEntryLookup = registry.getRegistryLookup(RegistryKeys.ENCHANTMENT);
        registry.register(MOB_SPAWN_EQUIPMENT, new ByCostWithDifficultyEnchantmentProvider(registryEntryLookup.getOrThrow(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT), 5, 17));
        registry.register(PILLAGER_SPAWN_CROSSBOW, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.PIERCING), ConstantIntProvider.create(1)));
        registry.register(PILLAGER_POST_WAVE_3_RAID, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.QUICK_CHARGE), ConstantIntProvider.create(1)));
        registry.register(PILLAGER_POST_WAVE_5_RAID, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.QUICK_CHARGE), ConstantIntProvider.create(2)));
        registry.register(VINDICATOR_RAID, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.SHARPNESS), ConstantIntProvider.create(1)));
        registry.register(VINDICATOR_POST_WAVE_5_RAID, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.SHARPNESS), ConstantIntProvider.create(2)));
        registry.register(ENDERMAN_LOOT_DROP, new SingleEnchantmentProvider(registryEntryLookup.getOrThrow(Enchantments.SILK_TOUCH), ConstantIntProvider.create(1)));
    }

    public static RegistryKey<EnchantmentProvider> of(String id) {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT_PROVIDER, Identifier.ofVanilla(id));
    }
}

