/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe.book;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class RecipeBook {
    protected final Set<Identifier> recipes = Sets.newHashSet();
    protected final Set<Identifier> toBeDisplayed = Sets.newHashSet();
    private final RecipeBookOptions options = new RecipeBookOptions();

    public void copyFrom(RecipeBook book) {
        this.recipes.clear();
        this.toBeDisplayed.clear();
        this.options.copyFrom(book.options);
        this.recipes.addAll(book.recipes);
        this.toBeDisplayed.addAll(book.toBeDisplayed);
    }

    public void add(RecipeEntry<?> recipe) {
        if (!recipe.value().isIgnoredInRecipeBook()) {
            this.add(recipe.id());
        }
    }

    protected void add(Identifier id) {
        this.recipes.add(id);
    }

    public boolean contains(@Nullable RecipeEntry<?> recipe) {
        if (recipe == null) {
            return false;
        }
        return this.recipes.contains(recipe.id());
    }

    public boolean contains(Identifier id) {
        return this.recipes.contains(id);
    }

    public void remove(RecipeEntry<?> recipe) {
        this.remove(recipe.id());
    }

    protected void remove(Identifier id) {
        this.recipes.remove(id);
        this.toBeDisplayed.remove(id);
    }

    public boolean shouldDisplay(RecipeEntry<?> recipe) {
        return this.toBeDisplayed.contains(recipe.id());
    }

    public void onRecipeDisplayed(RecipeEntry<?> recipe) {
        this.toBeDisplayed.remove(recipe.id());
    }

    public void display(RecipeEntry<?> recipe) {
        this.display(recipe.id());
    }

    protected void display(Identifier id) {
        this.toBeDisplayed.add(id);
    }

    public boolean isGuiOpen(RecipeBookCategory category) {
        return this.options.isGuiOpen(category);
    }

    public void setGuiOpen(RecipeBookCategory category, boolean open) {
        this.options.setGuiOpen(category, open);
    }

    public boolean isFilteringCraftable(AbstractRecipeScreenHandler<?, ?> handler) {
        return this.isFilteringCraftable(handler.getCategory());
    }

    public boolean isFilteringCraftable(RecipeBookCategory category) {
        return this.options.isFilteringCraftable(category);
    }

    public void setFilteringCraftable(RecipeBookCategory category, boolean filteringCraftable) {
        this.options.setFilteringCraftable(category, filteringCraftable);
    }

    public void setOptions(RecipeBookOptions options) {
        this.options.copyFrom(options);
    }

    public RecipeBookOptions getOptions() {
        return this.options.copy();
    }

    public void setCategoryOptions(RecipeBookCategory category, boolean guiOpen, boolean filteringCraftable) {
        this.options.setGuiOpen(category, guiOpen);
        this.options.setFilteringCraftable(category, filteringCraftable);
    }
}

