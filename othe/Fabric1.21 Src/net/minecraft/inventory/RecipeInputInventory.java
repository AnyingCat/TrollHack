/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import java.util.List;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.recipe.input.CraftingRecipeInput;

/**
 * Represents an inventory that is an input for a recipe, such as
 * crafting table inputs.
 */
public interface RecipeInputInventory
extends Inventory,
RecipeInputProvider {
    /**
     * {@return the width of the recipe grid}
     */
    public int getWidth();

    /**
     * {@return the height of the recipe grid}
     */
    public int getHeight();

    /**
     * {@return the stacks held by the inventory}
     */
    public List<ItemStack> getHeldStacks();

    default public CraftingRecipeInput createRecipeInput() {
        return this.createPositionedRecipeInput().input();
    }

    default public CraftingRecipeInput.Positioned createPositionedRecipeInput() {
        return CraftingRecipeInput.createPositioned(this.getWidth(), this.getHeight(), this.getHeldStacks());
    }
}

