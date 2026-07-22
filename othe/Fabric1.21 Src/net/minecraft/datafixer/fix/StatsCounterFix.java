/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import net.minecraft.datafixer.schema.Schema1451v6;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class StatsCounterFix
extends DataFix {
    private static final Set<String> SKIPPED_STATS = Set.of("dummy", "trigger", "deathCount", "playerKillCount", "totalKillCount", "health", "food", "air", "armor", "xp", "level", "killedByTeam.aqua", "killedByTeam.black", "killedByTeam.blue", "killedByTeam.dark_aqua", "killedByTeam.dark_blue", "killedByTeam.dark_gray", "killedByTeam.dark_green", "killedByTeam.dark_purple", "killedByTeam.dark_red", "killedByTeam.gold", "killedByTeam.gray", "killedByTeam.green", "killedByTeam.light_purple", "killedByTeam.red", "killedByTeam.white", "killedByTeam.yellow", "teamkill.aqua", "teamkill.black", "teamkill.blue", "teamkill.dark_aqua", "teamkill.dark_blue", "teamkill.dark_gray", "teamkill.dark_green", "teamkill.dark_purple", "teamkill.dark_red", "teamkill.gold", "teamkill.gray", "teamkill.green", "teamkill.light_purple", "teamkill.red", "teamkill.white", "teamkill.yellow");
    private static final Set<String> REMOVED_STATS = ((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)((ImmutableSet.Builder)ImmutableSet.builder().add("stat.craftItem.minecraft.spawn_egg")).add("stat.useItem.minecraft.spawn_egg")).add("stat.breakItem.minecraft.spawn_egg")).add("stat.pickup.minecraft.spawn_egg")).add("stat.drop.minecraft.spawn_egg")).build();
    private static final Map<String, String> RENAMED_GENERAL_STATS = ImmutableMap.builder().put("stat.leaveGame", "minecraft:leave_game").put("stat.playOneMinute", "minecraft:play_one_minute").put("stat.timeSinceDeath", "minecraft:time_since_death").put("stat.sneakTime", "minecraft:sneak_time").put("stat.walkOneCm", "minecraft:walk_one_cm").put("stat.crouchOneCm", "minecraft:crouch_one_cm").put("stat.sprintOneCm", "minecraft:sprint_one_cm").put("stat.swimOneCm", "minecraft:swim_one_cm").put("stat.fallOneCm", "minecraft:fall_one_cm").put("stat.climbOneCm", "minecraft:climb_one_cm").put("stat.flyOneCm", "minecraft:fly_one_cm").put("stat.diveOneCm", "minecraft:dive_one_cm").put("stat.minecartOneCm", "minecraft:minecart_one_cm").put("stat.boatOneCm", "minecraft:boat_one_cm").put("stat.pigOneCm", "minecraft:pig_one_cm").put("stat.horseOneCm", "minecraft:horse_one_cm").put("stat.aviateOneCm", "minecraft:aviate_one_cm").put("stat.jump", "minecraft:jump").put("stat.drop", "minecraft:drop").put("stat.damageDealt", "minecraft:damage_dealt").put("stat.damageTaken", "minecraft:damage_taken").put("stat.deaths", "minecraft:deaths").put("stat.mobKills", "minecraft:mob_kills").put("stat.animalsBred", "minecraft:animals_bred").put("stat.playerKills", "minecraft:player_kills").put("stat.fishCaught", "minecraft:fish_caught").put("stat.talkedToVillager", "minecraft:talked_to_villager").put("stat.tradedWithVillager", "minecraft:traded_with_villager").put("stat.cakeSlicesEaten", "minecraft:eat_cake_slice").put("stat.cauldronFilled", "minecraft:fill_cauldron").put("stat.cauldronUsed", "minecraft:use_cauldron").put("stat.armorCleaned", "minecraft:clean_armor").put("stat.bannerCleaned", "minecraft:clean_banner").put("stat.brewingstandInteraction", "minecraft:interact_with_brewingstand").put("stat.beaconInteraction", "minecraft:interact_with_beacon").put("stat.dropperInspected", "minecraft:inspect_dropper").put("stat.hopperInspected", "minecraft:inspect_hopper").put("stat.dispenserInspected", "minecraft:inspect_dispenser").put("stat.noteblockPlayed", "minecraft:play_noteblock").put("stat.noteblockTuned", "minecraft:tune_noteblock").put("stat.flowerPotted", "minecraft:pot_flower").put("stat.trappedChestTriggered", "minecraft:trigger_trapped_chest").put("stat.enderchestOpened", "minecraft:open_enderchest").put("stat.itemEnchanted", "minecraft:enchant_item").put("stat.recordPlayed", "minecraft:play_record").put("stat.furnaceInteraction", "minecraft:interact_with_furnace").put("stat.craftingTableInteraction", "minecraft:interact_with_crafting_table").put("stat.chestOpened", "minecraft:open_chest").put("stat.sleepInBed", "minecraft:sleep_in_bed").put("stat.shulkerBoxOpened", "minecraft:open_shulker_box").build();
    private static final String OLD_MINE_BLOCK_ID = "stat.mineBlock";
    private static final String NEW_MINE_BLOCK_ID = "minecraft:mined";
    private static final Map<String, String> RENAMED_ITEM_STATS = ImmutableMap.builder().put("stat.craftItem", "minecraft:crafted").put("stat.useItem", "minecraft:used").put("stat.breakItem", "minecraft:broken").put("stat.pickup", "minecraft:picked_up").put("stat.drop", "minecraft:dropped").build();
    private static final Map<String, String> RENAMED_ENTITY_STATS = ImmutableMap.builder().put("stat.entityKilledBy", "minecraft:killed_by").put("stat.killEntity", "minecraft:killed").build();
    private static final Map<String, String> RENAMED_ENTITIES = ImmutableMap.builder().put("Bat", "minecraft:bat").put("Blaze", "minecraft:blaze").put("CaveSpider", "minecraft:cave_spider").put("Chicken", "minecraft:chicken").put("Cow", "minecraft:cow").put("Creeper", "minecraft:creeper").put("Donkey", "minecraft:donkey").put("ElderGuardian", "minecraft:elder_guardian").put("Enderman", "minecraft:enderman").put("Endermite", "minecraft:endermite").put("EvocationIllager", "minecraft:evocation_illager").put("Ghast", "minecraft:ghast").put("Guardian", "minecraft:guardian").put("Horse", "minecraft:horse").put("Husk", "minecraft:husk").put("Llama", "minecraft:llama").put("LavaSlime", "minecraft:magma_cube").put("MushroomCow", "minecraft:mooshroom").put("Mule", "minecraft:mule").put("Ozelot", "minecraft:ocelot").put("Parrot", "minecraft:parrot").put("Pig", "minecraft:pig").put("PolarBear", "minecraft:polar_bear").put("Rabbit", "minecraft:rabbit").put("Sheep", "minecraft:sheep").put("Shulker", "minecraft:shulker").put("Silverfish", "minecraft:silverfish").put("SkeletonHorse", "minecraft:skeleton_horse").put("Skeleton", "minecraft:skeleton").put("Slime", "minecraft:slime").put("Spider", "minecraft:spider").put("Squid", "minecraft:squid").put("Stray", "minecraft:stray").put("Vex", "minecraft:vex").put("Villager", "minecraft:villager").put("VindicationIllager", "minecraft:vindication_illager").put("Witch", "minecraft:witch").put("WitherSkeleton", "minecraft:wither_skeleton").put("Wolf", "minecraft:wolf").put("ZombieHorse", "minecraft:zombie_horse").put("PigZombie", "minecraft:zombie_pigman").put("ZombieVillager", "minecraft:zombie_villager").put("Zombie", "minecraft:zombie").build();
    private static final String CUSTOM = "minecraft:custom";

    public StatsCounterFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Nullable
    private static Stat rename(String old) {
        if (REMOVED_STATS.contains(old)) {
            return null;
        }
        String string = RENAMED_GENERAL_STATS.get(old);
        if (string != null) {
            return new Stat(CUSTOM, string);
        }
        int i = StringUtils.ordinalIndexOf(old, ".", 2);
        if (i < 0) {
            return null;
        }
        String string2 = old.substring(0, i);
        if (OLD_MINE_BLOCK_ID.equals(string2)) {
            String string3 = StatsCounterFix.getBlock(old.substring(i + 1).replace('.', ':'));
            return new Stat(NEW_MINE_BLOCK_ID, string3);
        }
        String string3 = RENAMED_ITEM_STATS.get(string2);
        if (string3 != null) {
            String string4 = old.substring(i + 1).replace('.', ':');
            String string5 = StatsCounterFix.getItem(string4);
            String string6 = string5 == null ? string4 : string5;
            return new Stat(string3, string6);
        }
        String string4 = RENAMED_ENTITY_STATS.get(string2);
        if (string4 != null) {
            String string5 = old.substring(i + 1).replace('.', ':');
            String string6 = RENAMED_ENTITIES.getOrDefault(string5, string5);
            return new Stat(string4, string6);
        }
        return null;
    }

    @Override
    public TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(this.makeFirstRoundRule(), this.makeSecondRoundRule());
    }

    private TypeRewriteRule makeFirstRoundRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.STATS);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.STATS);
        return this.fixTypeEverywhereTyped("StatsCounterFix", type, type2, (Typed<?> statsTyped) -> {
            Dynamic<?> dynamic = statsTyped.get(DSL.remainderFinder());
            HashMap map = Maps.newHashMap();
            Optional<Map<Dynamic<?>, Dynamic<?>>> optional = dynamic.getMapValues().result();
            if (optional.isPresent()) {
                for (Map.Entry<Dynamic<?>, Dynamic<?>> entry : optional.get().entrySet()) {
                    String string;
                    Stat stat;
                    if (!entry.getValue().asNumber().result().isPresent() || (stat = StatsCounterFix.rename(string = entry.getKey().asString(""))) == null) continue;
                    Dynamic dynamic22 = dynamic.createString(stat.type());
                    Dynamic dynamic3 = map.computeIfAbsent(dynamic22, dynamic2 -> dynamic.emptyMap());
                    map.put(dynamic22, dynamic3.set(stat.typeKey(), entry.getValue()));
                }
            }
            return Util.readTyped(type2, dynamic.emptyMap().set("stats", dynamic.createMap(map)));
        });
    }

    private TypeRewriteRule makeSecondRoundRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.OBJECTIVE);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.OBJECTIVE);
        return this.fixTypeEverywhereTyped("ObjectiveStatFix", type, type2, (Typed<?> objectiveTyped) -> {
            Dynamic<?> dynamic = objectiveTyped.get(DSL.remainderFinder());
            Dynamic<?> dynamic2 = dynamic.update("CriteriaName", criteriaNameDynamic -> DataFixUtils.orElse(criteriaNameDynamic.asString().result().map(criteriaName -> {
                if (SKIPPED_STATS.contains(criteriaName)) {
                    return criteriaName;
                }
                Stat stat = StatsCounterFix.rename(criteriaName);
                if (stat == null) {
                    return "dummy";
                }
                return Schema1451v6.toDotSeparated(stat.type) + ":" + Schema1451v6.toDotSeparated(stat.typeKey);
            }).map(criteriaNameDynamic::createString), criteriaNameDynamic));
            return Util.readTyped(type2, dynamic2);
        });
    }

    @Nullable
    private static String getItem(String id) {
        return ItemInstanceTheFlatteningFix.getItem(id, 0);
    }

    private static String getBlock(String id) {
        return BlockStateFlattening.lookupBlock(id);
    }

    record Stat(String type, String typeKey) {
    }
}

