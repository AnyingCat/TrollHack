/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * A recipe is an arrangement of item stacks in an inventory that can
 * yield a product item stack.
 * 
 * <p>Recipes are loaded by and stored in the {@link RecipeManager}. They
 * are part of the server's data packs and are sent to the client, governed
 * by their {@linkplain #getSerializer() serializers}. Hence, recipes
 * should not be stored, as they may become obsolete after reloads.
 * 
 * <p>A few of the methods in this class are dedicated to crafting recipes
 * or recipe books. Users can have stub implementations if they do not use
 * those functionalities.
 */
public interface Recipe<T extends RecipeInput> {
    public static final Codec<Recipe<?>> CODEC = Registries.RECIPE_SERIALIZER.getCodec().dispatch(Recipe::getSerializer, RecipeSerializer::codec);
    public static final PacketCodec<RegistryByteBuf, Recipe<?>> PACKET_CODEC = PacketCodecs.registryValue(RegistryKeys.RECIPE_SERIALIZER).dispatch(Recipe::getSerializer, RecipeSerializer::packetCodec);

    /**
     * {@return whether this recipe matches the contents inside the
     * {@code inventory} in the given {@code world}}
     * 
     * <p>The {@code world} currently is only used by the map cloning recipe to
     * prevent duplication of explorer maps.
     * 
     * @param world the input world
     */
    public boolean matches(T var1, World var2);

    /**
     * Crafts this recipe.
     * 
     * <p>This method does not perform side effects on the {@code inventory}.
     * 
     * <p>This method should return a new item stack on each call.
     * 
     * @return the resulting item stack
     */
    public ItemStack craft(T var1, RegistryWrapper.WrapperLookup var2);

    /**
     * {@return whether this recipe will fit into the given grid size}
     * 
     * <p>This is currently only used by recipe book.
     * 
     * @param width the width of the input inventory
     * @param height the height of the input inventory
     */
    public boolean fits(int var1, int var2);

    /**
     * {@return a preview of the recipe's output}
     * 
     * <p>The returned stack should not be modified. To obtain the actual output,
     * call {@link #craft(Inventory, DynamicRegistryManager)}.
     */
    public ItemStack getResult(RegistryWrapper.WrapperLookup var1);

    /**
     * {@return the remaining stacks to be left in the {@code inventory} after the recipe is used}
     * At each index, the remainder item stack in the list should correspond to the original
     * item stack in the {@code inventory}.
     * 
     * @implSpec The default implementation returns a list of the same size as the {@code inventory}.
     * At each index, the list contains the {@linkplain net.minecraft.item.Item#getRecipeRemainder()
     * remainder} of the item stack at the same index in the {@code inventory}, or is {@linkplain
     * ItemStack#EMPTY empty} if the stack has no remainder.
     */
    default public DefaultedList<ItemStack> getRemainder(T input) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(input.getSize(), ItemStack.EMPTY);
        for (int i = 0; i < defaultedList.size(); ++i) {
            Item item = input.getStackInSlot(i).getItem();
            if (!item.hasRecipeRemainder()) continue;
            defaultedList.set(i, new ItemStack(item.getRecipeRemainder()));
        }
        return defaultedList;
    }

    /**
     * {@return the ingredients accepted as inputs for this recipe} Used by the recipe book
     * when displaying a ghost form of this recipe on the crafting grid as well as for
     * previewing the possible inputs in the book itself.
     */
    default public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.of();
    }

    /**
     * {@return whether this recipe is ignored by the recipe book} If a recipe
     * is ignored by the recipe book, it will be never displayed. In addition,
     * it won't be restricted by the {@link net.minecraft.world.GameRules#DO_LIMITED_CRAFTING
     * doLimitedCrafting} game rule.
     */
    default public boolean isIgnoredInRecipeBook() {
        return false;
    }

    default public boolean showNotification() {
        return true;
    }

    /**
     * {@return a group this recipe belongs in, or an empty string} This is
     * only used by the recipe book.
     * 
     * <p>The group string is arbitrary, and is not rendered anywhere; in
     * the recipe book, recipes with the same group will belong to the same
     * cell in the grid of recipes. If the string is empty, this recipe will
     * belong to its own cell.
     */
    default public String getGroup() {
        return "";
    }

    /**
     * {@return an item rendered on the top left of the {@linkplain #getResult(DynamicRegistryManager)
     * output preview} on the recipe toast when a new recipe is unlocked} This
     * can be interpreted as a catalyst for the recipe.
     */
    default public ItemStack createIcon() {
        return new ItemStack(Blocks.CRAFTING_TABLE);
    }

    /**
     * {@return the serializer associated with this recipe}
     */
    public RecipeSerializer<?> getSerializer();

    /**
     * {@return the type of this recipe}
     * 
     * <p>The {@code type} in the recipe JSON format is the {@linkplain
     * #getSerializer() serializer} instead.
     */
    public RecipeType<?> getType();

    /**
     * {@return whether this recipe has no ingredient or has any empty ingredient}
     * The recipe book uses this to ignore recipes for display.
     */
    default public boolean isEmpty() {
        DefaultedList<Ingredient> defaultedList = this.getIngredients();
        return defaultedList.isEmpty() || defaultedList.stream().anyMatch(ingredient -> ingredient.getMatchingStacks().length == 0);
    }
}

