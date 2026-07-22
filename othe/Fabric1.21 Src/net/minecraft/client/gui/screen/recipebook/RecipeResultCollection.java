/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.registry.DynamicRegistryManager;

@Environment(value=EnvType.CLIENT)
public class RecipeResultCollection {
    private final DynamicRegistryManager registryManager;
    private final List<RecipeEntry<?>> recipes;
    private final boolean singleOutput;
    private final Set<RecipeEntry<?>> craftableRecipes = Sets.newHashSet();
    private final Set<RecipeEntry<?>> fittingRecipes = Sets.newHashSet();
    private final Set<RecipeEntry<?>> unlockedRecipes = Sets.newHashSet();

    public RecipeResultCollection(DynamicRegistryManager registryManager, List<RecipeEntry<?>> recipes) {
        this.registryManager = registryManager;
        this.recipes = ImmutableList.copyOf(recipes);
        this.singleOutput = recipes.size() <= 1 ? true : RecipeResultCollection.shouldHaveSingleOutput(registryManager, recipes);
    }

    private static boolean shouldHaveSingleOutput(DynamicRegistryManager registryManager, List<RecipeEntry<?>> recipes) {
        int i = recipes.size();
        ItemStack itemStack = recipes.get(0).value().getResult(registryManager);
        for (int j = 1; j < i; ++j) {
            ItemStack itemStack2 = recipes.get(j).value().getResult(registryManager);
            if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2)) continue;
            return false;
        }
        return true;
    }

    public DynamicRegistryManager getRegistryManager() {
        return this.registryManager;
    }

    public boolean isInitialized() {
        return !this.unlockedRecipes.isEmpty();
    }

    public void initialize(RecipeBook recipeBook) {
        for (RecipeEntry<?> recipeEntry : this.recipes) {
            if (!recipeBook.contains(recipeEntry)) continue;
            this.unlockedRecipes.add(recipeEntry);
        }
    }

    public void computeCraftables(RecipeMatcher recipeFinder, int gridWidth, int gridHeight, RecipeBook recipeBook) {
        for (RecipeEntry<?> recipeEntry : this.recipes) {
            boolean bl;
            boolean bl2 = bl = recipeEntry.value().fits(gridWidth, gridHeight) && recipeBook.contains(recipeEntry);
            if (bl) {
                this.fittingRecipes.add(recipeEntry);
            } else {
                this.fittingRecipes.remove(recipeEntry);
            }
            if (bl && recipeFinder.match((Recipe<?>)recipeEntry.value(), null)) {
                this.craftableRecipes.add(recipeEntry);
                continue;
            }
            this.craftableRecipes.remove(recipeEntry);
        }
    }

    public boolean isCraftable(RecipeEntry<?> recipe) {
        return this.craftableRecipes.contains(recipe);
    }

    public boolean hasCraftableRecipes() {
        return !this.craftableRecipes.isEmpty();
    }

    public boolean hasFittingRecipes() {
        return !this.fittingRecipes.isEmpty();
    }

    public List<RecipeEntry<?>> getAllRecipes() {
        return this.recipes;
    }

    public List<RecipeEntry<?>> getResults(boolean craftableOnly) {
        ArrayList<RecipeEntry<?>> list = Lists.newArrayList();
        Set<RecipeEntry<?>> set = craftableOnly ? this.craftableRecipes : this.fittingRecipes;
        for (RecipeEntry<?> recipeEntry : this.recipes) {
            if (!set.contains(recipeEntry)) continue;
            list.add(recipeEntry);
        }
        return list;
    }

    public List<RecipeEntry<?>> getRecipes(boolean craftable) {
        ArrayList<RecipeEntry<?>> list = Lists.newArrayList();
        for (RecipeEntry<?> recipeEntry : this.recipes) {
            if (!this.fittingRecipes.contains(recipeEntry) || this.craftableRecipes.contains(recipeEntry) != craftable) continue;
            list.add(recipeEntry);
        }
        return list;
    }

    public boolean hasSingleOutput() {
        return this.singleOutput;
    }
}

