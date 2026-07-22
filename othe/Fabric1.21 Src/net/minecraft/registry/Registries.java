/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Bootstrap;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTypes;
import net.minecraft.block.Blocks;
import net.minecraft.block.DecoratedPotPattern;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.enchantment.provider.EnchantmentProviderType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Instrument;
import net.minecraft.item.Instruments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.loot.entry.LootPoolEntryTypes;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.loot.provider.nbt.LootNbtProviderType;
import net.minecraft.loot.provider.nbt.LootNbtProviderTypes;
import net.minecraft.loot.provider.number.LootNumberProviderType;
import net.minecraft.loot.provider.number.LootNumberProviderTypes;
import net.minecraft.loot.provider.score.LootScoreProviderType;
import net.minecraft.loot.provider.score.LootScoreProviderTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.predicate.entity.EntitySubPredicate;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.predicate.item.ItemSubPredicate;
import net.minecraft.predicate.item.ItemSubPredicateTypes;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.scoreboard.number.NumberFormatType;
import net.minecraft.scoreboard.number.NumberFormatTypes;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.pool.StructurePoolElementType;
import net.minecraft.structure.pool.alias.StructurePoolAliasBinding;
import net.minecraft.structure.pool.alias.StructurePoolAliasBindings;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.structure.rule.PosRuleTestType;
import net.minecraft.structure.rule.RuleTestType;
import net.minecraft.structure.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.floatprovider.FloatProviderType;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSources;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSourceType;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.carver.Carver;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGenerators;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.size.FeatureSizeType;
import net.minecraft.world.gen.foliage.FoliagePlacerType;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;
import net.minecraft.world.gen.root.RootPlacerType;
import net.minecraft.world.gen.stateprovider.BlockStateProviderType;
import net.minecraft.world.gen.structure.StructureType;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.TrunkPlacerType;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;

public class Registries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();
    private static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry(RegistryKey.ofRegistry(RegistryKeys.ROOT), Lifecycle.stable());
    public static final DefaultedRegistry<GameEvent> GAME_EVENT = Registries.create(RegistryKeys.GAME_EVENT, "step", GameEvent::registerAndGetDefault);
    public static final Registry<SoundEvent> SOUND_EVENT = Registries.create(RegistryKeys.SOUND_EVENT, registry -> SoundEvents.ENTITY_ITEM_PICKUP);
    public static final DefaultedRegistry<Fluid> FLUID = Registries.createIntrusive(RegistryKeys.FLUID, "empty", registry -> Fluids.EMPTY);
    public static final Registry<StatusEffect> STATUS_EFFECT = Registries.create(RegistryKeys.STATUS_EFFECT, StatusEffects::registerAndGetDefault);
    public static final DefaultedRegistry<Block> BLOCK = Registries.createIntrusive(RegistryKeys.BLOCK, "air", registry -> Blocks.AIR);
    public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = Registries.createIntrusive(RegistryKeys.ENTITY_TYPE, "pig", registry -> EntityType.PIG);
    public static final DefaultedRegistry<Item> ITEM = Registries.createIntrusive(RegistryKeys.ITEM, "air", registry -> Items.AIR);
    public static final Registry<Potion> POTION = Registries.create(RegistryKeys.POTION, Potions::registerAndGetDefault);
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = Registries.create(RegistryKeys.PARTICLE_TYPE, registry -> ParticleTypes.BLOCK);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = Registries.createIntrusive(RegistryKeys.BLOCK_ENTITY_TYPE, registry -> BlockEntityType.FURNACE);
    public static final Registry<Identifier> CUSTOM_STAT = Registries.create(RegistryKeys.CUSTOM_STAT, registry -> Stats.JUMP);
    public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = Registries.create(RegistryKeys.CHUNK_STATUS, "empty", (Registry<T> registry) -> ChunkStatus.EMPTY);
    public static final Registry<RuleTestType<?>> RULE_TEST = Registries.create(RegistryKeys.RULE_TEST, registry -> RuleTestType.ALWAYS_TRUE);
    public static final Registry<RuleBlockEntityModifierType<?>> RULE_BLOCK_ENTITY_MODIFIER = Registries.create(RegistryKeys.RULE_BLOCK_ENTITY_MODIFIER, registry -> RuleBlockEntityModifierType.PASSTHROUGH);
    public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = Registries.create(RegistryKeys.POS_RULE_TEST, registry -> PosRuleTestType.ALWAYS_TRUE);
    public static final Registry<ScreenHandlerType<?>> SCREEN_HANDLER = Registries.create(RegistryKeys.SCREEN_HANDLER, registry -> ScreenHandlerType.ANVIL);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = Registries.create(RegistryKeys.RECIPE_TYPE, registry -> RecipeType.CRAFTING);
    public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = Registries.create(RegistryKeys.RECIPE_SERIALIZER, registry -> RecipeSerializer.SHAPELESS);
    public static final Registry<EntityAttribute> ATTRIBUTE = Registries.create(RegistryKeys.ATTRIBUTE, EntityAttributes::registerAndGetDefault);
    public static final Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = Registries.create(RegistryKeys.POSITION_SOURCE_TYPE, registry -> PositionSourceType.BLOCK);
    public static final Registry<ArgumentSerializer<?, ?>> COMMAND_ARGUMENT_TYPE = Registries.create(RegistryKeys.COMMAND_ARGUMENT_TYPE, ArgumentTypes::register);
    public static final Registry<StatType<?>> STAT_TYPE = Registries.create(RegistryKeys.STAT_TYPE, registry -> Stats.USED);
    public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = Registries.create(RegistryKeys.VILLAGER_TYPE, "plains", (Registry<T> registry) -> VillagerType.PLAINS);
    public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = Registries.create(RegistryKeys.VILLAGER_PROFESSION, "none", (Registry<T> registry) -> VillagerProfession.NONE);
    public static final Registry<PointOfInterestType> POINT_OF_INTEREST_TYPE = Registries.create(RegistryKeys.POINT_OF_INTEREST_TYPE, PointOfInterestTypes::registerAndGetDefault);
    public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = Registries.create(RegistryKeys.MEMORY_MODULE_TYPE, "dummy", (Registry<T> registry) -> MemoryModuleType.DUMMY);
    public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = Registries.create(RegistryKeys.SENSOR_TYPE, "dummy", (Registry<T> registry) -> SensorType.DUMMY);
    public static final Registry<Schedule> SCHEDULE = Registries.create(RegistryKeys.SCHEDULE, registry -> Schedule.EMPTY);
    public static final Registry<Activity> ACTIVITY = Registries.create(RegistryKeys.ACTIVITY, registry -> Activity.IDLE);
    public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = Registries.create(RegistryKeys.LOOT_POOL_ENTRY_TYPE, registry -> LootPoolEntryTypes.EMPTY);
    public static final Registry<LootFunctionType<?>> LOOT_FUNCTION_TYPE = Registries.create(RegistryKeys.LOOT_FUNCTION_TYPE, registry -> LootFunctionTypes.SET_COUNT);
    public static final Registry<LootConditionType> LOOT_CONDITION_TYPE = Registries.create(RegistryKeys.LOOT_CONDITION_TYPE, registry -> LootConditionTypes.INVERTED);
    public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = Registries.create(RegistryKeys.LOOT_NUMBER_PROVIDER_TYPE, registry -> LootNumberProviderTypes.CONSTANT);
    public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = Registries.create(RegistryKeys.LOOT_NBT_PROVIDER_TYPE, registry -> LootNbtProviderTypes.CONTEXT);
    public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = Registries.create(RegistryKeys.LOOT_SCORE_PROVIDER_TYPE, registry -> LootScoreProviderTypes.CONTEXT);
    public static final Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPE = Registries.create(RegistryKeys.FLOAT_PROVIDER_TYPE, registry -> FloatProviderType.CONSTANT);
    public static final Registry<IntProviderType<?>> INT_PROVIDER_TYPE = Registries.create(RegistryKeys.INT_PROVIDER_TYPE, registry -> IntProviderType.CONSTANT);
    public static final Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPE = Registries.create(RegistryKeys.HEIGHT_PROVIDER_TYPE, registry -> HeightProviderType.CONSTANT);
    public static final Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPE = Registries.create(RegistryKeys.BLOCK_PREDICATE_TYPE, registry -> BlockPredicateType.NOT);
    public static final Registry<Carver<?>> CARVER = Registries.create(RegistryKeys.CARVER, registry -> Carver.CAVE);
    public static final Registry<Feature<?>> FEATURE = Registries.create(RegistryKeys.FEATURE, registry -> Feature.ORE);
    public static final Registry<StructurePlacementType<?>> STRUCTURE_PLACEMENT = Registries.create(RegistryKeys.STRUCTURE_PLACEMENT, registry -> StructurePlacementType.RANDOM_SPREAD);
    public static final Registry<StructurePieceType> STRUCTURE_PIECE = Registries.create(RegistryKeys.STRUCTURE_PIECE, registry -> StructurePieceType.MINESHAFT_ROOM);
    public static final Registry<StructureType<?>> STRUCTURE_TYPE = Registries.create(RegistryKeys.STRUCTURE_TYPE, registry -> StructureType.JIGSAW);
    public static final Registry<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPE = Registries.create(RegistryKeys.PLACEMENT_MODIFIER_TYPE, registry -> PlacementModifierType.COUNT);
    public static final Registry<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPE = Registries.create(RegistryKeys.BLOCK_STATE_PROVIDER_TYPE, registry -> BlockStateProviderType.SIMPLE_STATE_PROVIDER);
    public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPE = Registries.create(RegistryKeys.FOLIAGE_PLACER_TYPE, registry -> FoliagePlacerType.BLOB_FOLIAGE_PLACER);
    public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPE = Registries.create(RegistryKeys.TRUNK_PLACER_TYPE, registry -> TrunkPlacerType.STRAIGHT_TRUNK_PLACER);
    public static final Registry<RootPlacerType<?>> ROOT_PLACER_TYPE = Registries.create(RegistryKeys.ROOT_PLACER_TYPE, registry -> RootPlacerType.MANGROVE_ROOT_PLACER);
    public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPE = Registries.create(RegistryKeys.TREE_DECORATOR_TYPE, registry -> TreeDecoratorType.LEAVE_VINE);
    public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPE = Registries.create(RegistryKeys.FEATURE_SIZE_TYPE, registry -> FeatureSizeType.TWO_LAYERS_FEATURE_SIZE);
    public static final Registry<MapCodec<? extends BiomeSource>> BIOME_SOURCE = Registries.create(RegistryKeys.BIOME_SOURCE, BiomeSources::registerAndGetDefault);
    public static final Registry<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATOR = Registries.create(RegistryKeys.CHUNK_GENERATOR, ChunkGenerators::registerAndGetDefault);
    public static final Registry<MapCodec<? extends MaterialRules.MaterialCondition>> MATERIAL_CONDITION = Registries.create(RegistryKeys.MATERIAL_CONDITION, MaterialRules.MaterialCondition::registerAndGetDefault);
    public static final Registry<MapCodec<? extends MaterialRules.MaterialRule>> MATERIAL_RULE = Registries.create(RegistryKeys.MATERIAL_RULE, MaterialRules.MaterialRule::registerAndGetDefault);
    public static final Registry<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPE = Registries.create(RegistryKeys.DENSITY_FUNCTION_TYPE, DensityFunctionTypes::registerAndGetDefault);
    public static final Registry<MapCodec<? extends Block>> BLOCK_TYPE = Registries.create(RegistryKeys.BLOCK_TYPE, BlockTypes::registerAndGetDefault);
    public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = Registries.create(RegistryKeys.STRUCTURE_PROCESSOR, registry -> StructureProcessorType.BLOCK_IGNORE);
    public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = Registries.create(RegistryKeys.STRUCTURE_POOL_ELEMENT, registry -> StructurePoolElementType.EMPTY_POOL_ELEMENT);
    public static final Registry<MapCodec<? extends StructurePoolAliasBinding>> POOL_ALIAS_BINDING = Registries.create(RegistryKeys.POOL_ALIAS_BINDING, StructurePoolAliasBindings::registerAndGetDefault);
    public static final Registry<CatVariant> CAT_VARIANT = Registries.create(RegistryKeys.CAT_VARIANT, CatVariant::registerAndGetDefault);
    public static final Registry<FrogVariant> FROG_VARIANT = Registries.create(RegistryKeys.FROG_VARIANT, FrogVariant::registerAndGetDefault);
    public static final Registry<Instrument> INSTRUMENT = Registries.create(RegistryKeys.INSTRUMENT, Instruments::registerAndGetDefault);
    public static final Registry<DecoratedPotPattern> DECORATED_POT_PATTERN = Registries.create(RegistryKeys.DECORATED_POT_PATTERN, DecoratedPotPatterns::registerAndGetDefault);
    public static final Registry<ItemGroup> ITEM_GROUP = Registries.create(RegistryKeys.ITEM_GROUP, ItemGroups::registerAndGetDefault);
    public static final Registry<Criterion<?>> CRITERION = Registries.create(RegistryKeys.CRITERION, Criteria::getDefault);
    public static final Registry<NumberFormatType<?>> NUMBER_FORMAT_TYPE = Registries.create(RegistryKeys.NUMBER_FORMAT_TYPE, NumberFormatTypes::registerAndGetDefault);
    public static final Registry<ArmorMaterial> ARMOR_MATERIAL = Registries.create(RegistryKeys.ARMOR_MATERIAL, ArmorMaterials::getDefault);
    public static final Registry<ComponentType<?>> DATA_COMPONENT_TYPE = Registries.create(RegistryKeys.DATA_COMPONENT_TYPE, DataComponentTypes::getDefault);
    public static final Registry<MapCodec<? extends EntitySubPredicate>> ENTITY_SUB_PREDICATE_TYPE = Registries.create(RegistryKeys.ENTITY_SUB_PREDICATE_TYPE, EntitySubPredicateTypes::getDefault);
    public static final Registry<ItemSubPredicate.Type<?>> ITEM_SUB_PREDICATE_TYPE = Registries.create(RegistryKeys.ITEM_SUB_PREDICATE_TYPE, ItemSubPredicateTypes::getDefault);
    public static final Registry<MapDecorationType> MAP_DECORATION_TYPE = Registries.create(RegistryKeys.MAP_DECORATION_TYPE, MapDecorationTypes::getDefault);
    public static final Registry<ComponentType<?>> ENCHANTMENT_EFFECT_COMPONENT_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_EFFECT_COMPONENT_TYPE, EnchantmentEffectComponentTypes::getDefault);
    public static final Registry<MapCodec<? extends EnchantmentLevelBasedValue>> ENCHANTMENT_LEVEL_BASED_VALUE_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_LEVEL_BASED_VALUE_TYPE, EnchantmentLevelBasedValue::registerAndGetDefault);
    public static final Registry<MapCodec<? extends EnchantmentEntityEffect>> ENCHANTMENT_ENTITY_EFFECT_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_ENTITY_EFFECT_TYPE, EnchantmentEntityEffect::registerAndGetDefault);
    public static final Registry<MapCodec<? extends EnchantmentLocationBasedEffect>> ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE, EnchantmentLocationBasedEffect::registerAndGetDefault);
    public static final Registry<MapCodec<? extends EnchantmentValueEffect>> ENCHANTMENT_VALUE_EFFECT_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_VALUE_EFFECT_TYPE, EnchantmentValueEffect::registerAndGetDefault);
    public static final Registry<MapCodec<? extends EnchantmentProvider>> ENCHANTMENT_PROVIDER_TYPE = Registries.create(RegistryKeys.ENCHANTMENT_PROVIDER_TYPE, EnchantmentProviderType::registerAndGetDefault);
    public static final Registry<? extends Registry<?>> REGISTRIES = ROOT;

    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, Initializer<T> initializer) {
        return Registries.create(key, new SimpleRegistry(key, Lifecycle.stable(), false), initializer);
    }

    private static <T> Registry<T> createIntrusive(RegistryKey<? extends Registry<T>> key, Initializer<T> initializer) {
        return Registries.create(key, new SimpleRegistry(key, Lifecycle.stable(), true), initializer);
    }

    private static <T> DefaultedRegistry<T> create(RegistryKey<? extends Registry<T>> key, String defaultId, Initializer<T> initializer) {
        return Registries.create(key, new SimpleDefaultedRegistry(defaultId, key, Lifecycle.stable(), false), initializer);
    }

    private static <T> DefaultedRegistry<T> createIntrusive(RegistryKey<? extends Registry<T>> key, String defaultId, Initializer<T> initializer) {
        return Registries.create(key, new SimpleDefaultedRegistry(defaultId, key, Lifecycle.stable(), true), initializer);
    }

    private static <T, R extends MutableRegistry<T>> R create(RegistryKey<? extends Registry<T>> key, R registry, Initializer<T> initializer) {
        Bootstrap.ensureBootstrapped(() -> "registry " + String.valueOf(key));
        Identifier identifier = key.getValue();
        DEFAULT_ENTRIES.put(identifier, () -> initializer.run(registry));
        ROOT.add(key, registry, RegistryEntryInfo.DEFAULT);
        return registry;
    }

    public static void bootstrap() {
        Registries.init();
        Registries.freezeRegistries();
        Registries.validate(REGISTRIES);
    }

    private static void init() {
        DEFAULT_ENTRIES.forEach((id, initializer) -> {
            if (initializer.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", id);
            }
        });
    }

    private static void freezeRegistries() {
        REGISTRIES.freeze();
        for (Registry registry : REGISTRIES) {
            registry.freeze();
        }
    }

    private static <T extends Registry<?>> void validate(Registry<T> registries) {
        registries.forEach(registry -> {
            if (registry.getIds().isEmpty()) {
                Util.error("Registry '" + String.valueOf(registries.getId((Registry)registry)) + "' was empty after loading");
            }
            if (registry instanceof DefaultedRegistry) {
                Identifier identifier = ((DefaultedRegistry)registry).getDefaultId();
                Validate.notNull(registry.get(identifier), "Missing default of DefaultedMappedRegistry: " + String.valueOf(identifier), new Object[0]);
            }
        });
    }

    @FunctionalInterface
    static interface Initializer<T> {
        public Object run(Registry<T> var1);
    }
}

