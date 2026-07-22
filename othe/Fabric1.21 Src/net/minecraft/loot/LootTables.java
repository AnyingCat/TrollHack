/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class LootTables {
    private static final Set<RegistryKey<LootTable>> LOOT_TABLES = new HashSet<RegistryKey<LootTable>>();
    private static final Set<RegistryKey<LootTable>> LOOT_TABLES_READ_ONLY = Collections.unmodifiableSet(LOOT_TABLES);
    public static final RegistryKey<LootTable> EMPTY = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.ofVanilla("empty"));
    public static final RegistryKey<LootTable> SPAWN_BONUS_CHEST = LootTables.register("chests/spawn_bonus_chest");
    public static final RegistryKey<LootTable> END_CITY_TREASURE_CHEST = LootTables.register("chests/end_city_treasure");
    public static final RegistryKey<LootTable> SIMPLE_DUNGEON_CHEST = LootTables.register("chests/simple_dungeon");
    public static final RegistryKey<LootTable> VILLAGE_WEAPONSMITH_CHEST = LootTables.register("chests/village/village_weaponsmith");
    public static final RegistryKey<LootTable> VILLAGE_TOOLSMITH_CHEST = LootTables.register("chests/village/village_toolsmith");
    public static final RegistryKey<LootTable> VILLAGE_ARMORER_CHEST = LootTables.register("chests/village/village_armorer");
    public static final RegistryKey<LootTable> VILLAGE_CARTOGRAPHER_CHEST = LootTables.register("chests/village/village_cartographer");
    public static final RegistryKey<LootTable> VILLAGE_MASON_CHEST = LootTables.register("chests/village/village_mason");
    public static final RegistryKey<LootTable> VILLAGE_SHEPARD_CHEST = LootTables.register("chests/village/village_shepherd");
    public static final RegistryKey<LootTable> VILLAGE_BUTCHER_CHEST = LootTables.register("chests/village/village_butcher");
    public static final RegistryKey<LootTable> VILLAGE_FLETCHER_CHEST = LootTables.register("chests/village/village_fletcher");
    public static final RegistryKey<LootTable> VILLAGE_FISHER_CHEST = LootTables.register("chests/village/village_fisher");
    public static final RegistryKey<LootTable> VILLAGE_TANNERY_CHEST = LootTables.register("chests/village/village_tannery");
    public static final RegistryKey<LootTable> VILLAGE_TEMPLE_CHEST = LootTables.register("chests/village/village_temple");
    public static final RegistryKey<LootTable> VILLAGE_DESERT_HOUSE_CHEST = LootTables.register("chests/village/village_desert_house");
    public static final RegistryKey<LootTable> VILLAGE_PLAINS_CHEST = LootTables.register("chests/village/village_plains_house");
    public static final RegistryKey<LootTable> VILLAGE_TAIGA_HOUSE_CHEST = LootTables.register("chests/village/village_taiga_house");
    public static final RegistryKey<LootTable> VILLAGE_SNOWY_HOUSE_CHEST = LootTables.register("chests/village/village_snowy_house");
    public static final RegistryKey<LootTable> VILLAGE_SAVANNA_HOUSE_CHEST = LootTables.register("chests/village/village_savanna_house");
    public static final RegistryKey<LootTable> ABANDONED_MINESHAFT_CHEST = LootTables.register("chests/abandoned_mineshaft");
    public static final RegistryKey<LootTable> NETHER_BRIDGE_CHEST = LootTables.register("chests/nether_bridge");
    public static final RegistryKey<LootTable> STRONGHOLD_LIBRARY_CHEST = LootTables.register("chests/stronghold_library");
    public static final RegistryKey<LootTable> STRONGHOLD_CROSSING_CHEST = LootTables.register("chests/stronghold_crossing");
    public static final RegistryKey<LootTable> STRONGHOLD_CORRIDOR_CHEST = LootTables.register("chests/stronghold_corridor");
    public static final RegistryKey<LootTable> DESERT_PYRAMID_CHEST = LootTables.register("chests/desert_pyramid");
    public static final RegistryKey<LootTable> JUNGLE_TEMPLE_CHEST = LootTables.register("chests/jungle_temple");
    public static final RegistryKey<LootTable> JUNGLE_TEMPLE_DISPENSER_CHEST = LootTables.register("chests/jungle_temple_dispenser");
    public static final RegistryKey<LootTable> IGLOO_CHEST_CHEST = LootTables.register("chests/igloo_chest");
    public static final RegistryKey<LootTable> WOODLAND_MANSION_CHEST = LootTables.register("chests/woodland_mansion");
    public static final RegistryKey<LootTable> UNDERWATER_RUIN_SMALL_CHEST = LootTables.register("chests/underwater_ruin_small");
    public static final RegistryKey<LootTable> UNDERWATER_RUIN_BIG_CHEST = LootTables.register("chests/underwater_ruin_big");
    public static final RegistryKey<LootTable> BURIED_TREASURE_CHEST = LootTables.register("chests/buried_treasure");
    public static final RegistryKey<LootTable> SHIPWRECK_MAP_CHEST = LootTables.register("chests/shipwreck_map");
    public static final RegistryKey<LootTable> SHIPWRECK_SUPPLY_CHEST = LootTables.register("chests/shipwreck_supply");
    public static final RegistryKey<LootTable> SHIPWRECK_TREASURE_CHEST = LootTables.register("chests/shipwreck_treasure");
    public static final RegistryKey<LootTable> PILLAGER_OUTPOST_CHEST = LootTables.register("chests/pillager_outpost");
    public static final RegistryKey<LootTable> BASTION_TREASURE_CHEST = LootTables.register("chests/bastion_treasure");
    public static final RegistryKey<LootTable> BASTION_OTHER_CHEST = LootTables.register("chests/bastion_other");
    public static final RegistryKey<LootTable> BASTION_BRIDGE_CHEST = LootTables.register("chests/bastion_bridge");
    public static final RegistryKey<LootTable> BASTION_HOGLIN_STABLE_CHEST = LootTables.register("chests/bastion_hoglin_stable");
    public static final RegistryKey<LootTable> ANCIENT_CITY_CHEST = LootTables.register("chests/ancient_city");
    public static final RegistryKey<LootTable> ANCIENT_CITY_ICE_BOX_CHEST = LootTables.register("chests/ancient_city_ice_box");
    public static final RegistryKey<LootTable> RUINED_PORTAL_CHEST = LootTables.register("chests/ruined_portal");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_CHEST = LootTables.register("chests/trial_chambers/reward");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_COMMON_CHEST = LootTables.register("chests/trial_chambers/reward_common");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_RARE_CHEST = LootTables.register("chests/trial_chambers/reward_rare");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_UNIQUE_CHEST = LootTables.register("chests/trial_chambers/reward_unique");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_CHEST = LootTables.register("chests/trial_chambers/reward_ominous");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_COMMON_CHEST = LootTables.register("chests/trial_chambers/reward_ominous_common");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_RARE_CHEST = LootTables.register("chests/trial_chambers/reward_ominous_rare");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_REWARD_OMINOUS_UNIQUE_CHEST = LootTables.register("chests/trial_chambers/reward_ominous_unique");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_SUPPLY_CHEST = LootTables.register("chests/trial_chambers/supply");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_CORRIDOR_CHEST = LootTables.register("chests/trial_chambers/corridor");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_INTERSECTION_CHEST = LootTables.register("chests/trial_chambers/intersection");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_INTERSECTION_BARREL_CHEST = LootTables.register("chests/trial_chambers/intersection_barrel");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_ENTRANCE_CHEST = LootTables.register("chests/trial_chambers/entrance");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_CORRIDOR_DISPENSER = LootTables.register("dispensers/trial_chambers/corridor");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_CHAMBER_DISPENSER = LootTables.register("dispensers/trial_chambers/chamber");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_WATER_DISPENSER = LootTables.register("dispensers/trial_chambers/water");
    public static final RegistryKey<LootTable> TRIAL_CHAMBERS_CORRIDOR_POT = LootTables.register("pots/trial_chambers/corridor");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_EQUIPMENT = LootTables.register("equipment/trial_chamber");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_RANGED_EQUIPMENT = LootTables.register("equipment/trial_chamber_ranged");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_MELEE_EQUIPMENT = LootTables.register("equipment/trial_chamber_melee");
    public static final RegistryKey<LootTable> WHITE_SHEEP_ENTITY = LootTables.register("entities/sheep/white");
    public static final RegistryKey<LootTable> ORANGE_SHEEP_ENTITY = LootTables.register("entities/sheep/orange");
    public static final RegistryKey<LootTable> MAGENTA_SHEEP_ENTITY = LootTables.register("entities/sheep/magenta");
    public static final RegistryKey<LootTable> LIGHT_BLUE_SHEEP_ENTITY = LootTables.register("entities/sheep/light_blue");
    public static final RegistryKey<LootTable> YELLOW_SHEEP_ENTITY = LootTables.register("entities/sheep/yellow");
    public static final RegistryKey<LootTable> LIME_SHEEP_ENTITY = LootTables.register("entities/sheep/lime");
    public static final RegistryKey<LootTable> PINK_SHEEP_ENTITY = LootTables.register("entities/sheep/pink");
    public static final RegistryKey<LootTable> GRAY_SHEEP_ENTITY = LootTables.register("entities/sheep/gray");
    public static final RegistryKey<LootTable> LIGHT_GRAY_SHEEP_ENTITY = LootTables.register("entities/sheep/light_gray");
    public static final RegistryKey<LootTable> CYAN_SHEEP_ENTITY = LootTables.register("entities/sheep/cyan");
    public static final RegistryKey<LootTable> PURPLE_SHEEP_ENTITY = LootTables.register("entities/sheep/purple");
    public static final RegistryKey<LootTable> BLUE_SHEEP_ENTITY = LootTables.register("entities/sheep/blue");
    public static final RegistryKey<LootTable> BROWN_SHEEP_ENTITY = LootTables.register("entities/sheep/brown");
    public static final RegistryKey<LootTable> GREEN_SHEEP_ENTITY = LootTables.register("entities/sheep/green");
    public static final RegistryKey<LootTable> RED_SHEEP_ENTITY = LootTables.register("entities/sheep/red");
    public static final RegistryKey<LootTable> BLACK_SHEEP_ENTITY = LootTables.register("entities/sheep/black");
    public static final RegistryKey<LootTable> FISHING_GAMEPLAY = LootTables.register("gameplay/fishing");
    public static final RegistryKey<LootTable> FISHING_JUNK_GAMEPLAY = LootTables.register("gameplay/fishing/junk");
    public static final RegistryKey<LootTable> FISHING_TREASURE_GAMEPLAY = LootTables.register("gameplay/fishing/treasure");
    public static final RegistryKey<LootTable> FISHING_FISH_GAMEPLAY = LootTables.register("gameplay/fishing/fish");
    public static final RegistryKey<LootTable> CAT_MORNING_GIFT_GAMEPLAY = LootTables.register("gameplay/cat_morning_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_ARMORER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/armorer_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_BUTCHER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/butcher_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_CARTOGRAPHER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/cartographer_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_CLERIC_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/cleric_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_FARMER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/farmer_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_FISHERMAN_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/fisherman_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_FLETCHER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/fletcher_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_LEATHERWORKER_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/leatherworker_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_LIBRARIAN_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/librarian_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_MASON_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/mason_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_SHEPHERD_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/shepherd_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_TOOLSMITH_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/toolsmith_gift");
    public static final RegistryKey<LootTable> HERO_OF_THE_VILLAGE_WEAPONSMITH_GIFT_GAMEPLAY = LootTables.register("gameplay/hero_of_the_village/weaponsmith_gift");
    public static final RegistryKey<LootTable> SNIFFER_DIGGING_GAMEPLAY = LootTables.register("gameplay/sniffer_digging");
    public static final RegistryKey<LootTable> PANDA_SNEEZE_GAMEPLAY = LootTables.register("gameplay/panda_sneeze");
    public static final RegistryKey<LootTable> PIGLIN_BARTERING_GAMEPLAY = LootTables.register("gameplay/piglin_bartering");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_KEY_SPAWNER = LootTables.register("spawners/trial_chamber/key");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_CONSUMABLES_SPAWNER = LootTables.register("spawners/trial_chamber/consumables");
    public static final RegistryKey<LootTable> OMINOUS_TRIAL_CHAMBER_KEY_SPAWNER = LootTables.register("spawners/ominous/trial_chamber/key");
    public static final RegistryKey<LootTable> OMINOUS_TRIAL_CHAMBER_CONSUMABLES_SPAWNER = LootTables.register("spawners/ominous/trial_chamber/consumables");
    public static final RegistryKey<LootTable> TRIAL_CHAMBER_ITEMS_TO_DROP_WHEN_OMINOUS_SPAWNER = LootTables.register("spawners/trial_chamber/items_to_drop_when_ominous");
    public static final RegistryKey<LootTable> BOGGED_SHEARING = LootTables.register("shearing/bogged");
    public static final RegistryKey<LootTable> DESERT_WELL_ARCHAEOLOGY = LootTables.register("archaeology/desert_well");
    public static final RegistryKey<LootTable> DESERT_PYRAMID_ARCHAEOLOGY = LootTables.register("archaeology/desert_pyramid");
    public static final RegistryKey<LootTable> TRAIL_RUINS_COMMON_ARCHAEOLOGY = LootTables.register("archaeology/trail_ruins_common");
    public static final RegistryKey<LootTable> TRAIL_RUINS_RARE_ARCHAEOLOGY = LootTables.register("archaeology/trail_ruins_rare");
    public static final RegistryKey<LootTable> OCEAN_RUIN_WARM_ARCHAEOLOGY = LootTables.register("archaeology/ocean_ruin_warm");
    public static final RegistryKey<LootTable> OCEAN_RUIN_COLD_ARCHAEOLOGY = LootTables.register("archaeology/ocean_ruin_cold");

    private static RegistryKey<LootTable> register(String id) {
        return LootTables.registerLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.ofVanilla(id)));
    }

    private static RegistryKey<LootTable> registerLootTable(RegistryKey<LootTable> key) {
        if (LOOT_TABLES.add(key)) {
            return key;
        }
        throw new IllegalArgumentException(String.valueOf(key.getValue()) + " is already a registered built-in loot table");
    }

    public static Set<RegistryKey<LootTable>> getAll() {
        return LOOT_TABLES_READ_ONLY;
    }
}

