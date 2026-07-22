/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.AllOfLootCondition;
import net.minecraft.loot.condition.AnyOfLootCondition;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.DamageSourcePropertiesLootCondition;
import net.minecraft.loot.condition.EnchantmentActiveCheckLootCondition;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.condition.EntityScoresLootCondition;
import net.minecraft.loot.condition.InvertedLootCondition;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.RandomChanceWithEnchantedBonusLootCondition;
import net.minecraft.loot.condition.ReferenceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.condition.TimeCheckLootCondition;
import net.minecraft.loot.condition.ValueCheckLootCondition;
import net.minecraft.loot.condition.WeatherCheckLootCondition;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class LootConditionTypes {
    public static final LootConditionType INVERTED = LootConditionTypes.register("inverted", InvertedLootCondition.CODEC);
    public static final LootConditionType ANY_OF = LootConditionTypes.register("any_of", AnyOfLootCondition.CODEC);
    public static final LootConditionType ALL_OF = LootConditionTypes.register("all_of", AllOfLootCondition.CODEC);
    public static final LootConditionType RANDOM_CHANCE = LootConditionTypes.register("random_chance", RandomChanceLootCondition.CODEC);
    public static final LootConditionType RANDOM_CHANCE_WITH_ENCHANTED_BONUS = LootConditionTypes.register("random_chance_with_enchanted_bonus", RandomChanceWithEnchantedBonusLootCondition.CODEC);
    public static final LootConditionType ENTITY_PROPERTIES = LootConditionTypes.register("entity_properties", EntityPropertiesLootCondition.CODEC);
    public static final LootConditionType KILLED_BY_PLAYER = LootConditionTypes.register("killed_by_player", KilledByPlayerLootCondition.CODEC);
    public static final LootConditionType ENTITY_SCORES = LootConditionTypes.register("entity_scores", EntityScoresLootCondition.CODEC);
    public static final LootConditionType BLOCK_STATE_PROPERTY = LootConditionTypes.register("block_state_property", BlockStatePropertyLootCondition.CODEC);
    public static final LootConditionType MATCH_TOOL = LootConditionTypes.register("match_tool", MatchToolLootCondition.CODEC);
    public static final LootConditionType TABLE_BONUS = LootConditionTypes.register("table_bonus", TableBonusLootCondition.CODEC);
    public static final LootConditionType SURVIVES_EXPLOSION = LootConditionTypes.register("survives_explosion", SurvivesExplosionLootCondition.CODEC);
    public static final LootConditionType DAMAGE_SOURCE_PROPERTIES = LootConditionTypes.register("damage_source_properties", DamageSourcePropertiesLootCondition.CODEC);
    public static final LootConditionType LOCATION_CHECK = LootConditionTypes.register("location_check", LocationCheckLootCondition.CODEC);
    public static final LootConditionType WEATHER_CHECK = LootConditionTypes.register("weather_check", WeatherCheckLootCondition.CODEC);
    public static final LootConditionType REFERENCE = LootConditionTypes.register("reference", ReferenceLootCondition.CODEC);
    public static final LootConditionType TIME_CHECK = LootConditionTypes.register("time_check", TimeCheckLootCondition.CODEC);
    public static final LootConditionType VALUE_CHECK = LootConditionTypes.register("value_check", ValueCheckLootCondition.CODEC);
    public static final LootConditionType ENCHANTMENT_ACTIVE_CHECK = LootConditionTypes.register("enchantment_active_check", EnchantmentActiveCheckLootCondition.CODEC);

    private static LootConditionType register(String id, MapCodec<? extends LootCondition> codec) {
        return Registry.register(Registries.LOOT_CONDITION_TYPE, Identifier.ofVanilla(id), new LootConditionType(codec));
    }
}

