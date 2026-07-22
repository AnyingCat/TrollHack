/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.recipebook;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.CookingRecipeCategory;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ClientRecipeBook
extends RecipeBook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<RecipeBookGroup, List<RecipeResultCollection>> resultsByGroup = ImmutableMap.of();
    private List<RecipeResultCollection> orderedResults = ImmutableList.of();

    public void reload(Iterable<RecipeEntry<?>> recipes, DynamicRegistryManager registryManager) {
        Map<RecipeBookGroup, List<List<RecipeEntry<?>>>> map = ClientRecipeBook.toGroupedMap(recipes);
        HashMap map2 = Maps.newHashMap();
        ImmutableList.Builder builder = ImmutableList.builder();
        map.forEach((recipeBookGroup, list) -> map2.put(recipeBookGroup, (List)list.stream().map(recipes -> new RecipeResultCollection(registryManager, (List<RecipeEntry<?>>)recipes)).peek(builder::add).collect(ImmutableList.toImmutableList())));
        RecipeBookGroup.SEARCH_MAP.forEach((group, searchGroups) -> map2.put(group, (List)searchGroups.stream().flatMap(searchGroup -> ((List)map2.getOrDefault(searchGroup, ImmutableList.of())).stream()).collect(ImmutableList.toImmutableList())));
        this.resultsByGroup = ImmutableMap.copyOf(map2);
        this.orderedResults = builder.build();
    }

    private static Map<RecipeBookGroup, List<List<RecipeEntry<?>>>> toGroupedMap(Iterable<RecipeEntry<?>> recipes) {
        HashMap<RecipeBookGroup, List<List<RecipeEntry<?>>>> map = Maps.newHashMap();
        HashBasedTable table = HashBasedTable.create();
        for (RecipeEntry<?> recipeEntry : recipes) {
            Object recipe = recipeEntry.value();
            if (recipe.isIgnoredInRecipeBook() || recipe.isEmpty()) continue;
            RecipeBookGroup recipeBookGroup = ClientRecipeBook.getGroupForRecipe(recipeEntry);
            String string = recipe.getGroup();
            if (string.isEmpty()) {
                map.computeIfAbsent(recipeBookGroup, group -> Lists.newArrayList()).add(ImmutableList.of(recipeEntry));
                continue;
            }
            ArrayList<RecipeEntry<?>> list = (ArrayList<RecipeEntry<?>>)table.get((Object)recipeBookGroup, string);
            if (list == null) {
                list = Lists.newArrayList();
                table.put(recipeBookGroup, string, list);
                map.computeIfAbsent(recipeBookGroup, group -> Lists.newArrayList()).add(list);
            }
            list.add(recipeEntry);
        }
        return map;
    }

    private static RecipeBookGroup getGroupForRecipe(RecipeEntry<?> recipe) {
        Object recipe2 = recipe.value();
        if (recipe2 instanceof CraftingRecipe) {
            CraftingRecipe craftingRecipe = (CraftingRecipe)recipe2;
            return switch (craftingRecipe.getCategory()) {
                default -> throw new MatchException(null, null);
                case CraftingRecipeCategory.BUILDING -> RecipeBookGroup.CRAFTING_BUILDING_BLOCKS;
                case CraftingRecipeCategory.EQUIPMENT -> RecipeBookGroup.CRAFTING_EQUIPMENT;
                case CraftingRecipeCategory.REDSTONE -> RecipeBookGroup.CRAFTING_REDSTONE;
                case CraftingRecipeCategory.MISC -> RecipeBookGroup.CRAFTING_MISC;
            };
        }
        RecipeType<?> recipeType = recipe2.getType();
        if (recipe2 instanceof AbstractCookingRecipe) {
            AbstractCookingRecipe abstractCookingRecipe = (AbstractCookingRecipe)recipe2;
            CookingRecipeCategory cookingRecipeCategory = abstractCookingRecipe.getCategory();
            if (recipeType == RecipeType.SMELTING) {
                return switch (cookingRecipeCategory) {
                    default -> throw new MatchException(null, null);
                    case CookingRecipeCategory.BLOCKS -> RecipeBookGroup.FURNACE_BLOCKS;
                    case CookingRecipeCategory.FOOD -> RecipeBookGroup.FURNACE_FOOD;
                    case CookingRecipeCategory.MISC -> RecipeBookGroup.FURNACE_MISC;
                };
            }
            if (recipeType == RecipeType.BLASTING) {
                return cookingRecipeCategory == CookingRecipeCategory.BLOCKS ? RecipeBookGroup.BLAST_FURNACE_BLOCKS : RecipeBookGroup.BLAST_FURNACE_MISC;
            }
            if (recipeType == RecipeType.SMOKING) {
                return RecipeBookGroup.SMOKER_FOOD;
            }
            if (recipeType == RecipeType.CAMPFIRE_COOKING) {
                return RecipeBookGroup.CAMPFIRE;
            }
        }
        if (recipeType == RecipeType.STONECUTTING) {
            return RecipeBookGroup.STONECUTTER;
        }
        if (recipeType == RecipeType.SMITHING) {
            return RecipeBookGroup.SMITHING;
        }
        LOGGER.warn("Unknown recipe category: {}/{}", LogUtils.defer(() -> Registries.RECIPE_TYPE.getId(recipe2.getType())), LogUtils.defer(recipe::id));
        return RecipeBookGroup.UNKNOWN;
    }

    public List<RecipeResultCollection> getOrderedResults() {
        return this.orderedResults;
    }

    public List<RecipeResultCollection> getResultsForGroup(RecipeBookGroup category) {
        return this.resultsByGroup.getOrDefault((Object)category, Collections.emptyList());
    }
}

