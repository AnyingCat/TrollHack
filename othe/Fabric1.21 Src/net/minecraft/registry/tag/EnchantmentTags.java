/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface EnchantmentTags {
    public static final TagKey<Enchantment> TOOLTIP_ORDER = EnchantmentTags.of("tooltip_order");
    public static final TagKey<Enchantment> ARMOR_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/armor");
    public static final TagKey<Enchantment> BOOTS_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/boots");
    public static final TagKey<Enchantment> BOW_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/bow");
    public static final TagKey<Enchantment> CROSSBOW_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/crossbow");
    public static final TagKey<Enchantment> DAMAGE_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/damage");
    public static final TagKey<Enchantment> MINING_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/mining");
    public static final TagKey<Enchantment> RIPTIDE_EXCLUSIVE_SET = EnchantmentTags.of("exclusive_set/riptide");
    public static final TagKey<Enchantment> TRADEABLE = EnchantmentTags.of("tradeable");
    public static final TagKey<Enchantment> DOUBLE_TRADE_PRICE = EnchantmentTags.of("double_trade_price");
    public static final TagKey<Enchantment> IN_ENCHANTING_TABLE = EnchantmentTags.of("in_enchanting_table");
    public static final TagKey<Enchantment> ON_MOB_SPAWN_EQUIPMENT = EnchantmentTags.of("on_mob_spawn_equipment");
    public static final TagKey<Enchantment> ON_TRADED_EQUIPMENT = EnchantmentTags.of("on_traded_equipment");
    public static final TagKey<Enchantment> ON_RANDOM_LOOT = EnchantmentTags.of("on_random_loot");
    public static final TagKey<Enchantment> CURSE = EnchantmentTags.of("curse");
    public static final TagKey<Enchantment> SMELTS_LOOT = EnchantmentTags.of("smelts_loot");
    public static final TagKey<Enchantment> PREVENTS_BEE_SPAWNS_WHEN_MINING = EnchantmentTags.of("prevents_bee_spawns_when_mining");
    public static final TagKey<Enchantment> PREVENTS_DECORATED_POT_SHATTERING = EnchantmentTags.of("prevents_decorated_pot_shattering");
    public static final TagKey<Enchantment> PREVENTS_ICE_MELTING = EnchantmentTags.of("prevents_ice_melting");
    public static final TagKey<Enchantment> PREVENTS_INFESTED_SPAWNS = EnchantmentTags.of("prevents_infested_spawns");
    public static final TagKey<Enchantment> TREASURE = EnchantmentTags.of("treasure");
    public static final TagKey<Enchantment> NON_TREASURE = EnchantmentTags.of("non_treasure");
    public static final TagKey<Enchantment> DESERT_COMMON_TRADE = EnchantmentTags.of("trades/desert_common");
    public static final TagKey<Enchantment> JUNGLE_COMMON_TRADE = EnchantmentTags.of("trades/jungle_common");
    public static final TagKey<Enchantment> PLAINS_COMMON_TRADE = EnchantmentTags.of("trades/plains_common");
    public static final TagKey<Enchantment> SAVANNA_COMMON_TRADE = EnchantmentTags.of("trades/savanna_common");
    public static final TagKey<Enchantment> SNOW_COMMON_TRADE = EnchantmentTags.of("trades/snow_common");
    public static final TagKey<Enchantment> SWAMP_COMMON_TRADE = EnchantmentTags.of("trades/swamp_common");
    public static final TagKey<Enchantment> TAIGA_COMMON_TRADE = EnchantmentTags.of("trades/taiga_common");
    public static final TagKey<Enchantment> DESERT_SPECIAL_TRADE = EnchantmentTags.of("trades/desert_special");
    public static final TagKey<Enchantment> JUNGLE_SPECIAL_TRADE = EnchantmentTags.of("trades/jungle_special");
    public static final TagKey<Enchantment> PLAINS_SPECIAL_TRADE = EnchantmentTags.of("trades/plains_special");
    public static final TagKey<Enchantment> SAVANNA_SPECIAL_TRADE = EnchantmentTags.of("trades/savanna_special");
    public static final TagKey<Enchantment> SNOW_SPECIAL_TRADE = EnchantmentTags.of("trades/snow_special");
    public static final TagKey<Enchantment> SWAMP_SPECIAL_TRADE = EnchantmentTags.of("trades/swamp_special");
    public static final TagKey<Enchantment> TAIGA_SPECIAL_TRADE = EnchantmentTags.of("trades/taiga_special");

    private static TagKey<Enchantment> of(String id) {
        return TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.ofVanilla(id));
    }
}

