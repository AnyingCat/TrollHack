/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.advancement.criterion;

import com.mojang.serialization.Codec;
import net.minecraft.advancement.criterion.AnyBlockUseCriterion;
import net.minecraft.advancement.criterion.BeeNestDestroyedCriterion;
import net.minecraft.advancement.criterion.BredAnimalsCriterion;
import net.minecraft.advancement.criterion.BrewedPotionCriterion;
import net.minecraft.advancement.criterion.ChangedDimensionCriterion;
import net.minecraft.advancement.criterion.ChanneledLightningCriterion;
import net.minecraft.advancement.criterion.ConstructBeaconCriterion;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CuredZombieVillagerCriterion;
import net.minecraft.advancement.criterion.DefaultBlockUseCriterion;
import net.minecraft.advancement.criterion.EffectsChangedCriterion;
import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.EntityHurtPlayerCriterion;
import net.minecraft.advancement.criterion.FallAfterExplosionCriterion;
import net.minecraft.advancement.criterion.FilledBucketCriterion;
import net.minecraft.advancement.criterion.FishingRodHookedCriterion;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.advancement.criterion.ItemDurabilityChangedCriterion;
import net.minecraft.advancement.criterion.KilledByCrossbowCriterion;
import net.minecraft.advancement.criterion.LevitationCriterion;
import net.minecraft.advancement.criterion.LightningStrikeCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.PlayerGeneratesContainerLootCriterion;
import net.minecraft.advancement.criterion.PlayerHurtEntityCriterion;
import net.minecraft.advancement.criterion.PlayerInteractedWithEntityCriterion;
import net.minecraft.advancement.criterion.RecipeCraftedCriterion;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.advancement.criterion.ShotCrossbowCriterion;
import net.minecraft.advancement.criterion.SlideDownBlockCriterion;
import net.minecraft.advancement.criterion.StartedRidingCriterion;
import net.minecraft.advancement.criterion.SummonedEntityCriterion;
import net.minecraft.advancement.criterion.TameAnimalCriterion;
import net.minecraft.advancement.criterion.TargetHitCriterion;
import net.minecraft.advancement.criterion.ThrownItemPickedUpByEntityCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.advancement.criterion.TravelCriterion;
import net.minecraft.advancement.criterion.UsedEnderEyeCriterion;
import net.minecraft.advancement.criterion.UsedTotemCriterion;
import net.minecraft.advancement.criterion.UsingItemCriterion;
import net.minecraft.advancement.criterion.VillagerTradeCriterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Criteria {
    public static final Codec<Criterion<?>> CODEC = Registries.CRITERION.getCodec();
    public static final ImpossibleCriterion IMPOSSIBLE = Criteria.register("impossible", new ImpossibleCriterion());
    public static final OnKilledCriterion PLAYER_KILLED_ENTITY = Criteria.register("player_killed_entity", new OnKilledCriterion());
    public static final OnKilledCriterion ENTITY_KILLED_PLAYER = Criteria.register("entity_killed_player", new OnKilledCriterion());
    public static final EnterBlockCriterion ENTER_BLOCK = Criteria.register("enter_block", new EnterBlockCriterion());
    public static final InventoryChangedCriterion INVENTORY_CHANGED = Criteria.register("inventory_changed", new InventoryChangedCriterion());
    public static final RecipeUnlockedCriterion RECIPE_UNLOCKED = Criteria.register("recipe_unlocked", new RecipeUnlockedCriterion());
    public static final PlayerHurtEntityCriterion PLAYER_HURT_ENTITY = Criteria.register("player_hurt_entity", new PlayerHurtEntityCriterion());
    public static final EntityHurtPlayerCriterion ENTITY_HURT_PLAYER = Criteria.register("entity_hurt_player", new EntityHurtPlayerCriterion());
    public static final EnchantedItemCriterion ENCHANTED_ITEM = Criteria.register("enchanted_item", new EnchantedItemCriterion());
    public static final FilledBucketCriterion FILLED_BUCKET = Criteria.register("filled_bucket", new FilledBucketCriterion());
    public static final BrewedPotionCriterion BREWED_POTION = Criteria.register("brewed_potion", new BrewedPotionCriterion());
    public static final ConstructBeaconCriterion CONSTRUCT_BEACON = Criteria.register("construct_beacon", new ConstructBeaconCriterion());
    public static final UsedEnderEyeCriterion USED_ENDER_EYE = Criteria.register("used_ender_eye", new UsedEnderEyeCriterion());
    public static final SummonedEntityCriterion SUMMONED_ENTITY = Criteria.register("summoned_entity", new SummonedEntityCriterion());
    public static final BredAnimalsCriterion BRED_ANIMALS = Criteria.register("bred_animals", new BredAnimalsCriterion());
    public static final TickCriterion LOCATION = Criteria.register("location", new TickCriterion());
    public static final TickCriterion SLEPT_IN_BED = Criteria.register("slept_in_bed", new TickCriterion());
    public static final CuredZombieVillagerCriterion CURED_ZOMBIE_VILLAGER = Criteria.register("cured_zombie_villager", new CuredZombieVillagerCriterion());
    public static final VillagerTradeCriterion VILLAGER_TRADE = Criteria.register("villager_trade", new VillagerTradeCriterion());
    public static final ItemDurabilityChangedCriterion ITEM_DURABILITY_CHANGED = Criteria.register("item_durability_changed", new ItemDurabilityChangedCriterion());
    public static final LevitationCriterion LEVITATION = Criteria.register("levitation", new LevitationCriterion());
    public static final ChangedDimensionCriterion CHANGED_DIMENSION = Criteria.register("changed_dimension", new ChangedDimensionCriterion());
    public static final TickCriterion TICK = Criteria.register("tick", new TickCriterion());
    public static final TameAnimalCriterion TAME_ANIMAL = Criteria.register("tame_animal", new TameAnimalCriterion());
    public static final ItemCriterion PLACED_BLOCK = Criteria.register("placed_block", new ItemCriterion());
    public static final ConsumeItemCriterion CONSUME_ITEM = Criteria.register("consume_item", new ConsumeItemCriterion());
    public static final EffectsChangedCriterion EFFECTS_CHANGED = Criteria.register("effects_changed", new EffectsChangedCriterion());
    public static final UsedTotemCriterion USED_TOTEM = Criteria.register("used_totem", new UsedTotemCriterion());
    public static final TravelCriterion NETHER_TRAVEL = Criteria.register("nether_travel", new TravelCriterion());
    public static final FishingRodHookedCriterion FISHING_ROD_HOOKED = Criteria.register("fishing_rod_hooked", new FishingRodHookedCriterion());
    public static final ChanneledLightningCriterion CHANNELED_LIGHTNING = Criteria.register("channeled_lightning", new ChanneledLightningCriterion());
    public static final ShotCrossbowCriterion SHOT_CROSSBOW = Criteria.register("shot_crossbow", new ShotCrossbowCriterion());
    public static final KilledByCrossbowCriterion KILLED_BY_CROSSBOW = Criteria.register("killed_by_crossbow", new KilledByCrossbowCriterion());
    public static final TickCriterion HERO_OF_THE_VILLAGE = Criteria.register("hero_of_the_village", new TickCriterion());
    public static final TickCriterion VOLUNTARY_EXILE = Criteria.register("voluntary_exile", new TickCriterion());
    public static final SlideDownBlockCriterion SLIDE_DOWN_BLOCK = Criteria.register("slide_down_block", new SlideDownBlockCriterion());
    public static final BeeNestDestroyedCriterion BEE_NEST_DESTROYED = Criteria.register("bee_nest_destroyed", new BeeNestDestroyedCriterion());
    public static final TargetHitCriterion TARGET_HIT = Criteria.register("target_hit", new TargetHitCriterion());
    public static final ItemCriterion ITEM_USED_ON_BLOCK = Criteria.register("item_used_on_block", new ItemCriterion());
    public static final DefaultBlockUseCriterion DEFAULT_BLOCK_USE = Criteria.register("default_block_use", new DefaultBlockUseCriterion());
    public static final AnyBlockUseCriterion ANY_BLOCK_USE = Criteria.register("any_block_use", new AnyBlockUseCriterion());
    public static final PlayerGeneratesContainerLootCriterion PLAYER_GENERATES_CONTAINER_LOOT = Criteria.register("player_generates_container_loot", new PlayerGeneratesContainerLootCriterion());
    public static final ThrownItemPickedUpByEntityCriterion THROWN_ITEM_PICKED_UP_BY_ENTITY = Criteria.register("thrown_item_picked_up_by_entity", new ThrownItemPickedUpByEntityCriterion());
    public static final ThrownItemPickedUpByEntityCriterion THROWN_ITEM_PICKED_UP_BY_PLAYER = Criteria.register("thrown_item_picked_up_by_player", new ThrownItemPickedUpByEntityCriterion());
    public static final PlayerInteractedWithEntityCriterion PLAYER_INTERACTED_WITH_ENTITY = Criteria.register("player_interacted_with_entity", new PlayerInteractedWithEntityCriterion());
    public static final StartedRidingCriterion STARTED_RIDING = Criteria.register("started_riding", new StartedRidingCriterion());
    public static final LightningStrikeCriterion LIGHTNING_STRIKE = Criteria.register("lightning_strike", new LightningStrikeCriterion());
    public static final UsingItemCriterion USING_ITEM = Criteria.register("using_item", new UsingItemCriterion());
    public static final TravelCriterion FALL_FROM_HEIGHT = Criteria.register("fall_from_height", new TravelCriterion());
    public static final TravelCriterion RIDE_ENTITY_IN_LAVA = Criteria.register("ride_entity_in_lava", new TravelCriterion());
    public static final OnKilledCriterion KILL_MOB_NEAR_SCULK_CATALYST = Criteria.register("kill_mob_near_sculk_catalyst", new OnKilledCriterion());
    public static final ItemCriterion ALLAY_DROP_ITEM_ON_BLOCK = Criteria.register("allay_drop_item_on_block", new ItemCriterion());
    public static final TickCriterion AVOID_VIBRATION = Criteria.register("avoid_vibration", new TickCriterion());
    public static final RecipeCraftedCriterion RECIPE_CRAFTED = Criteria.register("recipe_crafted", new RecipeCraftedCriterion());
    public static final RecipeCraftedCriterion CRAFTER_RECIPE_CRAFTED = Criteria.register("crafter_recipe_crafted", new RecipeCraftedCriterion());
    public static final FallAfterExplosionCriterion FALL_AFTER_EXPLOSION = Criteria.register("fall_after_explosion", new FallAfterExplosionCriterion());

    public static <T extends Criterion<?>> T register(String id, T criterion) {
        return (T)Registry.register(Registries.CRITERION, id, criterion);
    }

    public static Criterion<?> getDefault(Registry<Criterion<?>> registry) {
        return IMPOSSIBLE;
    }
}

