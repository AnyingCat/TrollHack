/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.EnterBlockCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.server.recipe.CookingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTrimRecipeJsonBuilder;
import net.minecraft.data.server.recipe.StonecuttingRecipeJsonBuilder;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class RecipeProvider
implements DataProvider {
    final DataOutput.PathResolver recipesPathResolver;
    final DataOutput.PathResolver advancementsPathResolver;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;
    private static final Map<BlockFamily.Variant, BiFunction<ItemConvertible, ItemConvertible, CraftingRecipeJsonBuilder>> VARIANT_FACTORIES = ImmutableMap.builder().put(BlockFamily.Variant.BUTTON, (output, input) -> RecipeProvider.createTransmutationRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CHISELED, (output, input) -> RecipeProvider.createChiseledBlockRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUT, (output, input) -> RecipeProvider.createCutCopperRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.DOOR, (output, input) -> RecipeProvider.createDoorRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUSTOM_FENCE, (output, input) -> RecipeProvider.createFenceRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.FENCE, (output, input) -> RecipeProvider.createFenceRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.CUSTOM_FENCE_GATE, (output, input) -> RecipeProvider.createFenceGateRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.FENCE_GATE, (output, input) -> RecipeProvider.createFenceGateRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.SIGN, (output, input) -> RecipeProvider.createSignRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.SLAB, (output, input) -> RecipeProvider.createSlabRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.STAIRS, (output, input) -> RecipeProvider.createStairsRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.PRESSURE_PLATE, (output, input) -> RecipeProvider.createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.POLISHED, (output, input) -> RecipeProvider.createCondensingRecipe(RecipeCategory.BUILDING_BLOCKS, output, Ingredient.ofItems(input))).put(BlockFamily.Variant.TRAPDOOR, (output, input) -> RecipeProvider.createTrapdoorRecipe(output, Ingredient.ofItems(input))).put(BlockFamily.Variant.WALL, (output, input) -> RecipeProvider.getWallRecipe(RecipeCategory.DECORATIONS, output, Ingredient.ofItems(input))).build();

    public RecipeProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.recipesPathResolver = output.getResolver(RegistryKeys.RECIPE);
        this.advancementsPathResolver = output.getResolver(RegistryKeys.ADVANCEMENT);
        this.registryLookupFuture = registryLookupFuture;
    }

    @Override
    public final CompletableFuture<?> run(DataWriter writer) {
        return this.registryLookupFuture.thenCompose(registryLookup -> this.run(writer, (RegistryWrapper.WrapperLookup)registryLookup));
    }

    public CompletableFuture<?> run(final DataWriter writer, final RegistryWrapper.WrapperLookup registryLookup) {
        final HashSet set = Sets.newHashSet();
        final ArrayList list = new ArrayList();
        this.generate(new RecipeExporter(){

            @Override
            public void accept(Identifier recipeId, Recipe<?> recipe, @Nullable AdvancementEntry advancement) {
                if (!set.add(recipeId)) {
                    throw new IllegalStateException("Duplicate recipe " + String.valueOf(recipeId));
                }
                list.add(DataProvider.writeCodecToPath(writer, registryLookup, Recipe.CODEC, recipe, RecipeProvider.this.recipesPathResolver.resolveJson(recipeId)));
                if (advancement != null) {
                    list.add(DataProvider.writeCodecToPath(writer, registryLookup, Advancement.CODEC, advancement.value(), RecipeProvider.this.advancementsPathResolver.resolveJson(advancement.id())));
                }
            }

            @Override
            public Advancement.Builder getAdvancementBuilder() {
                return Advancement.Builder.createUntelemetered().parent(CraftingRecipeJsonBuilder.ROOT);
            }
        });
        return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<?> saveRecipeAdvancement(DataWriter cache, RegistryWrapper.WrapperLookup registryLookup, AdvancementEntry advancement) {
        return DataProvider.writeCodecToPath(cache, registryLookup, Advancement.CODEC, advancement.value(), this.advancementsPathResolver.resolveJson(advancement.id()));
    }

    public abstract void generate(RecipeExporter var1);

    public static void generateFamilies(RecipeExporter exporter, FeatureSet enabledFeatures) {
        BlockFamilies.getFamilies().filter(BlockFamily::shouldGenerateRecipes).forEach(family -> RecipeProvider.generateFamily(exporter, family, enabledFeatures));
    }

    public static void offerSingleOutputShapelessRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input, @Nullable String group) {
        RecipeProvider.offerShapelessRecipe(exporter, output, input, group, 1);
    }

    public static void offerShapelessRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input, @Nullable String group, int outputCount) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, output, outputCount).input(input).group(group).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, input));
    }

    public static void offerSmelting(RecipeExporter exporter, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
        RecipeProvider.offerMultipleOptions(exporter, RecipeSerializer.SMELTING, SmeltingRecipe::new, inputs, category, output, experience, cookingTime, group, "_from_smelting");
    }

    public static void offerBlasting(RecipeExporter exporter, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group) {
        RecipeProvider.offerMultipleOptions(exporter, RecipeSerializer.BLASTING, BlastingRecipe::new, inputs, category, output, experience, cookingTime, group, "_from_blasting");
    }

    public static <T extends AbstractCookingRecipe> void offerMultipleOptions(RecipeExporter exporter, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, List<ItemConvertible> inputs, RecipeCategory category, ItemConvertible output, float experience, int cookingTime, String group, String suffix) {
        for (ItemConvertible itemConvertible : inputs) {
            CookingRecipeJsonBuilder.create(Ingredient.ofItems(itemConvertible), category, output, experience, cookingTime, serializer, recipeFactory).group(group).criterion(RecipeProvider.hasItem(itemConvertible), (AdvancementCriterion)RecipeProvider.conditionsFromItem(itemConvertible)).offerTo(exporter, RecipeProvider.getItemPath(output) + suffix + "_" + RecipeProvider.getItemPath(itemConvertible));
        }
    }

    public static void offerNetheriteUpgradeRecipe(RecipeExporter exporter, Item input, RecipeCategory category, Item result) {
        SmithingTransformRecipeJsonBuilder.create(Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.ofItems(input), Ingredient.ofItems(Items.NETHERITE_INGOT), category, result).criterion("has_netherite_ingot", RecipeProvider.conditionsFromItem(Items.NETHERITE_INGOT)).offerTo(exporter, RecipeProvider.getItemPath(result) + "_smithing");
    }

    public static void offerSmithingTrimRecipe(RecipeExporter exporter, Item template, Identifier recipeId) {
        SmithingTrimRecipeJsonBuilder.create(Ingredient.ofItems(template), Ingredient.fromTag(ItemTags.TRIMMABLE_ARMOR), Ingredient.fromTag(ItemTags.TRIM_MATERIALS), RecipeCategory.MISC).criterion("has_smithing_trim_template", RecipeProvider.conditionsFromItem(template)).offerTo(exporter, recipeId);
    }

    public static void offer2x2CompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(category, output, 1).input(Character.valueOf('#'), input).pattern("##").pattern("##").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerCompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, String criterionName) {
        ShapelessRecipeJsonBuilder.create(category, output).input(input, 9).criterion(criterionName, (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerCompactingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.offerCompactingRecipe(exporter, category, output, input, RecipeProvider.hasItem(input));
    }

    public static void offerPlanksRecipe2(RecipeExporter exporter, ItemConvertible output, TagKey<Item> input, int count) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_log", (AdvancementCriterion)RecipeProvider.conditionsFromTag(input)).offerTo(exporter);
    }

    public static void offerPlanksRecipe(RecipeExporter exporter, ItemConvertible output, TagKey<Item> input, int count) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, count).input(input).group("planks").criterion("has_logs", (AdvancementCriterion)RecipeProvider.conditionsFromTag(input)).offerTo(exporter);
    }

    public static void offerBarkBlockRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 3).input(Character.valueOf('#'), input).pattern("##").pattern("##").group("bark").criterion("has_log", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerBoatRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input(Character.valueOf('#'), input).pattern("# #").pattern("###").group("boat").criterion("in_water", (AdvancementCriterion)RecipeProvider.requireEnteringFluid(Blocks.WATER)).offerTo(exporter);
    }

    public static void offerChestBoatRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.TRANSPORTATION, output).input(Blocks.CHEST).input(input).group("chest_boat").criterion("has_boat", (AdvancementCriterion)RecipeProvider.conditionsFromTag(ItemTags.BOATS)).offerTo(exporter);
    }

    public static CraftingRecipeJsonBuilder createTransmutationRecipe(ItemConvertible output, Ingredient input) {
        return ShapelessRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input(input);
    }

    public static CraftingRecipeJsonBuilder createDoorRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 3).input(Character.valueOf('#'), input).pattern("##").pattern("##").pattern("##");
    }

    public static CraftingRecipeJsonBuilder createFenceRecipe(ItemConvertible output, Ingredient input) {
        int i = output == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
        Item item = output == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
        return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, i).input(Character.valueOf('W'), input).input(Character.valueOf('#'), item).pattern("W#W").pattern("W#W");
    }

    public static CraftingRecipeJsonBuilder createFenceGateRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output).input(Character.valueOf('#'), Items.STICK).input(Character.valueOf('W'), input).pattern("#W#").pattern("#W#");
    }

    public static void offerPressurePlateRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createPressurePlateRecipe(RecipeCategory.REDSTONE, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static CraftingRecipeJsonBuilder createPressurePlateRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("##");
    }

    public static void offerSlabRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createSlabRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static CraftingRecipeJsonBuilder createSlabRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 6).input(Character.valueOf('#'), input).pattern("###");
    }

    public static CraftingRecipeJsonBuilder createStairsRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4).input(Character.valueOf('#'), input).pattern("#  ").pattern("## ").pattern("###");
    }

    public static CraftingRecipeJsonBuilder createTrapdoorRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 2).input(Character.valueOf('#'), input).pattern("###").pattern("###");
    }

    public static CraftingRecipeJsonBuilder createSignRecipe(ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).group("sign").input(Character.valueOf('#'), input).input(Character.valueOf('X'), Items.STICK).pattern("###").pattern("###").pattern(" X ");
    }

    public static void offerHangingSignRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 6).group("hanging_sign").input(Character.valueOf('#'), input).input(Character.valueOf('X'), Items.CHAIN).pattern("X X").pattern("###").pattern("###").criterion("has_stripped_logs", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerDyeableRecipes(RecipeExporter exporter, List<Item> dyes, List<Item> dyeables, String group) {
        for (int i = 0; i < dyes.size(); ++i) {
            Item item = dyes.get(i);
            Item item2 = dyeables.get(i);
            ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, item2).input(item).input(Ingredient.ofStacks(dyeables.stream().filter(dyeable -> !dyeable.equals(item2)).map(ItemStack::new))).group(group).criterion("has_needed_dye", (AdvancementCriterion)RecipeProvider.conditionsFromItem(item)).offerTo(exporter, "dye_" + RecipeProvider.getItemPath(item2));
        }
    }

    public static void offerCarpetRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 3).input(Character.valueOf('#'), input).pattern("##").group("carpet").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerBedRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Character.valueOf('#'), input).input(Character.valueOf('X'), ItemTags.PLANKS).pattern("###").pattern("XXX").group("bed").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerBannerRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Character.valueOf('#'), input).input(Character.valueOf('|'), Items.STICK).pattern("###").pattern("###").pattern(" | ").group("banner").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerStainedGlassDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(Character.valueOf('#'), Blocks.GLASS).input(Character.valueOf('X'), input).pattern("###").pattern("#X#").pattern("###").group("stained_glass").criterion("has_glass", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GLASS)).offerTo(exporter);
    }

    public static void offerStainedGlassPaneRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 16).input(Character.valueOf('#'), input).pattern("###").pattern("###").group("stained_glass_pane").criterion("has_glass", (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerStainedGlassPaneDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ((ShapedRecipeJsonBuilder)ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output, 8).input(Character.valueOf('#'), Blocks.GLASS_PANE).input(Character.valueOf('$'), input).pattern("###").pattern("#$#").pattern("###").group("stained_glass_pane").criterion("has_glass_pane", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GLASS_PANE))).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, Blocks.GLASS_PANE));
    }

    public static void offerTerracottaDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(Character.valueOf('#'), Blocks.TERRACOTTA).input(Character.valueOf('X'), input).pattern("###").pattern("#X#").pattern("###").group("stained_terracotta").criterion("has_terracotta", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.TERRACOTTA)).offerTo(exporter);
    }

    public static void offerConcretePowderDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ((ShapelessRecipeJsonBuilder)ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 8).input(input).input(Blocks.SAND, 4).input(Blocks.GRAVEL, 4).group("concrete_powder").criterion("has_sand", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.SAND))).criterion("has_gravel", (AdvancementCriterion)RecipeProvider.conditionsFromItem(Blocks.GRAVEL)).offerTo(exporter);
    }

    public static void offerCandleDyeingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        ShapelessRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, output).input(Blocks.CANDLE).input(input).group("dyed_candle").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerWallRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.getWallRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static CraftingRecipeJsonBuilder getWallRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 6).input(Character.valueOf('#'), input).pattern("###").pattern("###");
    }

    public static void offerPolishedStoneRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createCondensingRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static CraftingRecipeJsonBuilder createCondensingRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 4).input(Character.valueOf('S'), input).pattern("SS").pattern("SS");
    }

    public static void offerCutCopperRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createCutCopperRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static ShapedRecipeJsonBuilder createCutCopperRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output, 4).input(Character.valueOf('#'), input).pattern("##").pattern("##");
    }

    public static void offerChiseledBlockRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.createChiseledBlockRecipe(category, output, Ingredient.ofItems(input)).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerMosaicRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("#").pattern("#").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static ShapedRecipeJsonBuilder createChiseledBlockRecipe(RecipeCategory category, ItemConvertible output, Ingredient input) {
        return ShapedRecipeJsonBuilder.create(category, output).input(Character.valueOf('#'), input).pattern("#").pattern("#");
    }

    public static void offerStonecuttingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input) {
        RecipeProvider.offerStonecuttingRecipe(exporter, category, output, input, 1);
    }

    public static void offerStonecuttingRecipe(RecipeExporter exporter, RecipeCategory category, ItemConvertible output, ItemConvertible input, int count) {
        StonecuttingRecipeJsonBuilder.createStonecutting(Ingredient.ofItems(input), category, output, count).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter, RecipeProvider.convertBetween(output, input) + "_stonecutting");
    }

    /**
     * Offers a smelting recipe to the exporter that is used to convert the main block of a block family to its cracked variant.
     */
    public static void offerCrackingRecipe(RecipeExporter exporter, ItemConvertible output, ItemConvertible input) {
        CookingRecipeJsonBuilder.createSmelting(Ingredient.ofItems(input), RecipeCategory.BUILDING_BLOCKS, output, 0.1f, 200).criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    /**
     * Offers two recipes to convert between a normal and compacted form of an item.
     * 
     * <p>The shaped recipe converts 9 items in a square to a compacted form of the item.
     * <p>The shapeless recipe converts the compacted form to 9 of the normal form.
     */
    public static void offerReversibleCompactingRecipes(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, RecipeProvider.getRecipeName(compactItem), null, RecipeProvider.getRecipeName(baseItem), null);
    }

    public static void offerReversibleCompactingRecipesWithCompactingRecipeGroup(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, String compactingGroup) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, compactingId, compactingGroup, RecipeProvider.getRecipeName(baseItem), null);
    }

    public static void offerReversibleCompactingRecipesWithReverseRecipeGroup(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String reverseId, String reverseGroup) {
        RecipeProvider.offerReversibleCompactingRecipes(exporter, reverseCategory, baseItem, compactingCategory, compactItem, RecipeProvider.getRecipeName(compactItem), null, reverseId, reverseGroup);
    }

    public static void offerReversibleCompactingRecipes(RecipeExporter exporter, RecipeCategory reverseCategory, ItemConvertible baseItem, RecipeCategory compactingCategory, ItemConvertible compactItem, String compactingId, @Nullable String compactingGroup, String reverseId, @Nullable String reverseGroup) {
        ((ShapelessRecipeJsonBuilder)ShapelessRecipeJsonBuilder.create(reverseCategory, baseItem, 9).input(compactItem).group(reverseGroup).criterion(RecipeProvider.hasItem(compactItem), (AdvancementCriterion)RecipeProvider.conditionsFromItem(compactItem))).offerTo(exporter, Identifier.of(reverseId));
        ((ShapedRecipeJsonBuilder)ShapedRecipeJsonBuilder.create(compactingCategory, compactItem).input(Character.valueOf('#'), baseItem).pattern("###").pattern("###").pattern("###").group(compactingGroup).criterion(RecipeProvider.hasItem(baseItem), (AdvancementCriterion)RecipeProvider.conditionsFromItem(baseItem))).offerTo(exporter, Identifier.of(compactingId));
    }

    public static void offerSmithingTemplateCopyingRecipe(RecipeExporter exporter, ItemConvertible template, TagKey<Item> resource) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), resource).input(Character.valueOf('S'), template).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(template), (AdvancementCriterion)RecipeProvider.conditionsFromItem(template)).offerTo(exporter);
    }

    public static void offerSmithingTemplateCopyingRecipe(RecipeExporter exporter, ItemConvertible template, ItemConvertible resource) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), resource).input(Character.valueOf('S'), template).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(template), (AdvancementCriterion)RecipeProvider.conditionsFromItem(template)).offerTo(exporter);
    }

    public static void offerSmithingTemplateCopyingRecipe(RecipeExporter exporter, ItemConvertible template, Ingredient resource) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, template, 2).input(Character.valueOf('#'), Items.DIAMOND).input(Character.valueOf('C'), resource).input(Character.valueOf('S'), template).pattern("#S#").pattern("#C#").pattern("###").criterion(RecipeProvider.hasItem(template), (AdvancementCriterion)RecipeProvider.conditionsFromItem(template)).offerTo(exporter);
    }

    public static <T extends AbstractCookingRecipe> void generateCookingRecipes(RecipeExporter exporter, String cooker, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime) {
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.BEEF, Items.COOKED_BEEF, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.COD, Items.COOKED_COD, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.KELP, Items.DRIED_KELP, 0.1f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.SALMON, Items.COOKED_SALMON, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.MUTTON, Items.COOKED_MUTTON, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.POTATO, Items.BAKED_POTATO, 0.35f);
        RecipeProvider.offerFoodCookingRecipe(exporter, cooker, serializer, recipeFactory, cookingTime, Items.RABBIT, Items.COOKED_RABBIT, 0.35f);
    }

    public static <T extends AbstractCookingRecipe> void offerFoodCookingRecipe(RecipeExporter exporter, String cooker, RecipeSerializer<T> serializer, AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime, ItemConvertible items, ItemConvertible output, float experience) {
        CookingRecipeJsonBuilder.create(Ingredient.ofItems(items), RecipeCategory.FOOD, output, experience, cookingTime, serializer, recipeFactory).criterion(RecipeProvider.hasItem(items), (AdvancementCriterion)RecipeProvider.conditionsFromItem(items)).offerTo(exporter, RecipeProvider.getItemPath(output) + "_from_" + cooker);
    }

    public static void offerWaxingRecipes(RecipeExporter exporter, FeatureSet enabledFeatures) {
        HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get().forEach((unwaxed, waxed) -> {
            if (!waxed.getRequiredFeatures().isSubsetOf(enabledFeatures)) {
                return;
            }
            ShapelessRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, waxed).input((ItemConvertible)unwaxed).input(Items.HONEYCOMB).group(RecipeProvider.getItemPath(waxed)).criterion(RecipeProvider.hasItem(unwaxed), (AdvancementCriterion)RecipeProvider.conditionsFromItem(unwaxed)).offerTo(exporter, RecipeProvider.convertBetween(waxed, Items.HONEYCOMB));
        });
    }

    public static void offerGrateRecipe(RecipeExporter exporter, Block output, Block input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, output, 4).input(Character.valueOf('M'), input).pattern(" M ").pattern("M M").pattern(" M ").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void offerBulbRecipe(RecipeExporter exporter, Block output, Block input) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, output, 4).input(Character.valueOf('C'), input).input(Character.valueOf('R'), Items.REDSTONE).input(Character.valueOf('B'), Items.BLAZE_ROD).pattern(" C ").pattern("CBC").pattern(" R ").criterion(RecipeProvider.hasItem(input), (AdvancementCriterion)RecipeProvider.conditionsFromItem(input)).offerTo(exporter);
    }

    public static void generateFamily(RecipeExporter exporter, BlockFamily family, FeatureSet enabledFeatures) {
        family.getVariants().forEach((variant, block) -> {
            if (!block.getRequiredFeatures().isSubsetOf(enabledFeatures)) {
                return;
            }
            BiFunction<ItemConvertible, ItemConvertible, CraftingRecipeJsonBuilder> biFunction = VARIANT_FACTORIES.get(variant);
            Block itemConvertible = RecipeProvider.getVariantRecipeInput(family, variant);
            if (biFunction != null) {
                CraftingRecipeJsonBuilder craftingRecipeJsonBuilder = biFunction.apply((ItemConvertible)block, itemConvertible);
                family.getGroup().ifPresent(group -> craftingRecipeJsonBuilder.group(group + (String)(variant == BlockFamily.Variant.CUT ? "" : "_" + variant.getName())));
                craftingRecipeJsonBuilder.criterion(family.getUnlockCriterionName().orElseGet(() -> RecipeProvider.hasItem(itemConvertible)), RecipeProvider.conditionsFromItem(itemConvertible));
                craftingRecipeJsonBuilder.offerTo(exporter);
            }
            if (variant == BlockFamily.Variant.CRACKED) {
                RecipeProvider.offerCrackingRecipe(exporter, block, itemConvertible);
            }
        });
    }

    /**
     * Gets the block used to craft a certain {@linkplain net.minecraft.data.family.BlockFamily.Variant variant} of a base block.
     * 
     * <p>Normally, the block used to craft a variant is the base block.
     * For chiseled variants, this is the slab variant of that block.
     * 
     * <p>Purpur is handled separately because both purpur and purpur pillars can be used to craft purpur slabs and stairs.
     */
    public static Block getVariantRecipeInput(BlockFamily family, BlockFamily.Variant variant) {
        if (variant == BlockFamily.Variant.CHISELED) {
            if (!family.getVariants().containsKey((Object)BlockFamily.Variant.SLAB)) {
                throw new IllegalStateException("Slab is not defined for the family.");
            }
            return family.getVariant(BlockFamily.Variant.SLAB);
        }
        return family.getBaseBlock();
    }

    public static AdvancementCriterion<EnterBlockCriterion.Conditions> requireEnteringFluid(Block block) {
        return Criteria.ENTER_BLOCK.create(new EnterBlockCriterion.Conditions(Optional.empty(), Optional.of(block.getRegistryEntry()), Optional.empty()));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(NumberRange.IntRange count, ItemConvertible item) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().items(item).count(count));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItem(ItemConvertible item) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().items(item));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromTag(TagKey<Item> tag) {
        return RecipeProvider.conditionsFromPredicates(ItemPredicate.Builder.create().tag(tag));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromPredicates(ItemPredicate.Builder ... predicates) {
        return RecipeProvider.conditionsFromItemPredicates((ItemPredicate[])Arrays.stream(predicates).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
    }

    public static AdvancementCriterion<InventoryChangedCriterion.Conditions> conditionsFromItemPredicates(ItemPredicate ... predicates) {
        return Criteria.INVENTORY_CHANGED.create(new InventoryChangedCriterion.Conditions(Optional.empty(), InventoryChangedCriterion.Conditions.Slots.ANY, List.of(predicates)));
    }

    public static String hasItem(ItemConvertible item) {
        return "has_" + RecipeProvider.getItemPath(item);
    }

    public static String getItemPath(ItemConvertible item) {
        return Registries.ITEM.getId(item.asItem()).getPath();
    }

    public static String getRecipeName(ItemConvertible item) {
        return RecipeProvider.getItemPath(item);
    }

    public static String convertBetween(ItemConvertible to, ItemConvertible from) {
        return RecipeProvider.getItemPath(to) + "_from_" + RecipeProvider.getItemPath(from);
    }

    public static String getSmeltingItemPath(ItemConvertible item) {
        return RecipeProvider.getItemPath(item) + "_from_smelting";
    }

    public static String getBlastingItemPath(ItemConvertible item) {
        return RecipeProvider.getItemPath(item) + "_from_blasting";
    }

    @Override
    public String getName() {
        return "Recipes";
    }
}

