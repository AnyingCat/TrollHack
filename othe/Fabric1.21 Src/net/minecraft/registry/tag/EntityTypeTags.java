/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface EntityTypeTags {
    public static final TagKey<EntityType<?>> SKELETONS = EntityTypeTags.of("skeletons");
    public static final TagKey<EntityType<?>> ZOMBIES = EntityTypeTags.of("zombies");
    public static final TagKey<EntityType<?>> RAIDERS = EntityTypeTags.of("raiders");
    public static final TagKey<EntityType<?>> UNDEAD = EntityTypeTags.of("undead");
    public static final TagKey<EntityType<?>> BEEHIVE_INHABITORS = EntityTypeTags.of("beehive_inhabitors");
    public static final TagKey<EntityType<?>> ARROWS = EntityTypeTags.of("arrows");
    public static final TagKey<EntityType<?>> IMPACT_PROJECTILES = EntityTypeTags.of("impact_projectiles");
    public static final TagKey<EntityType<?>> POWDER_SNOW_WALKABLE_MOBS = EntityTypeTags.of("powder_snow_walkable_mobs");
    public static final TagKey<EntityType<?>> AXOLOTL_ALWAYS_HOSTILES = EntityTypeTags.of("axolotl_always_hostiles");
    public static final TagKey<EntityType<?>> AXOLOTL_HUNT_TARGETS = EntityTypeTags.of("axolotl_hunt_targets");
    public static final TagKey<EntityType<?>> FREEZE_IMMUNE_ENTITY_TYPES = EntityTypeTags.of("freeze_immune_entity_types");
    public static final TagKey<EntityType<?>> FREEZE_HURTS_EXTRA_TYPES = EntityTypeTags.of("freeze_hurts_extra_types");
    public static final TagKey<EntityType<?>> CAN_BREATHE_UNDER_WATER = EntityTypeTags.of("can_breathe_under_water");
    public static final TagKey<EntityType<?>> FROG_FOOD = EntityTypeTags.of("frog_food");
    public static final TagKey<EntityType<?>> FALL_DAMAGE_IMMUNE = EntityTypeTags.of("fall_damage_immune");
    public static final TagKey<EntityType<?>> DISMOUNTS_UNDERWATER = EntityTypeTags.of("dismounts_underwater");
    public static final TagKey<EntityType<?>> NON_CONTROLLING_RIDER = EntityTypeTags.of("non_controlling_rider");
    public static final TagKey<EntityType<?>> DEFLECTS_PROJECTILES = EntityTypeTags.of("deflects_projectiles");
    public static final TagKey<EntityType<?>> CAN_TURN_IN_BOATS = EntityTypeTags.of("can_turn_in_boats");
    public static final TagKey<EntityType<?>> ILLAGER = EntityTypeTags.of("illager");
    public static final TagKey<EntityType<?>> AQUATIC = EntityTypeTags.of("aquatic");
    public static final TagKey<EntityType<?>> ARTHROPOD = EntityTypeTags.of("arthropod");
    public static final TagKey<EntityType<?>> IGNORES_POISON_AND_REGEN = EntityTypeTags.of("ignores_poison_and_regen");
    public static final TagKey<EntityType<?>> INVERTED_HEALING_AND_HARM = EntityTypeTags.of("inverted_healing_and_harm");
    public static final TagKey<EntityType<?>> WITHER_FRIENDS = EntityTypeTags.of("wither_friends");
    public static final TagKey<EntityType<?>> ILLAGER_FRIENDS = EntityTypeTags.of("illager_friends");
    public static final TagKey<EntityType<?>> NOT_SCARY_FOR_PUFFERFISH = EntityTypeTags.of("not_scary_for_pufferfish");
    public static final TagKey<EntityType<?>> SENSITIVE_TO_IMPALING = EntityTypeTags.of("sensitive_to_impaling");
    public static final TagKey<EntityType<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS = EntityTypeTags.of("sensitive_to_bane_of_arthropods");
    public static final TagKey<EntityType<?>> SENSITIVE_TO_SMITE = EntityTypeTags.of("sensitive_to_smite");
    public static final TagKey<EntityType<?>> NO_ANGER_FROM_WIND_CHARGE = EntityTypeTags.of("no_anger_from_wind_charge");
    public static final TagKey<EntityType<?>> IMMUNE_TO_OOZING = EntityTypeTags.of("immune_to_oozing");
    public static final TagKey<EntityType<?>> IMMUNE_TO_INFESTED = EntityTypeTags.of("immune_to_infested");
    public static final TagKey<EntityType<?>> REDIRECTABLE_PROJECTILE = EntityTypeTags.of("redirectable_projectile");

    private static TagKey<EntityType<?>> of(String id) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.ofVanilla(id));
    }
}

