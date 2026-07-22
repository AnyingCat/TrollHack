/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;

public class BrewingRecipeRegistry {
    public static final int field_30942 = 20;
    public static final BrewingRecipeRegistry EMPTY = new BrewingRecipeRegistry(List.of(), List.of(), List.of());
    private final List<Ingredient> potionTypes;
    private final List<Recipe<Potion>> potionRecipes;
    private final List<Recipe<Item>> itemRecipes;

    BrewingRecipeRegistry(List<Ingredient> potionTypes, List<Recipe<Potion>> potionRecipes, List<Recipe<Item>> itemRecipes) {
        this.potionTypes = potionTypes;
        this.potionRecipes = potionRecipes;
        this.itemRecipes = itemRecipes;
    }

    public boolean isValidIngredient(ItemStack stack) {
        return this.isItemRecipeIngredient(stack) || this.isPotionRecipeIngredient(stack);
    }

    private boolean isPotionType(ItemStack stack) {
        for (Ingredient ingredient : this.potionTypes) {
            if (!ingredient.test(stack)) continue;
            return true;
        }
        return false;
    }

    public boolean isItemRecipeIngredient(ItemStack stack) {
        for (Recipe<Item> recipe : this.itemRecipes) {
            if (!recipe.ingredient.test(stack)) continue;
            return true;
        }
        return false;
    }

    public boolean isPotionRecipeIngredient(ItemStack stack) {
        for (Recipe<Potion> recipe : this.potionRecipes) {
            if (!recipe.ingredient.test(stack)) continue;
            return true;
        }
        return false;
    }

    public boolean isBrewable(RegistryEntry<Potion> potion) {
        for (Recipe<Potion> recipe : this.potionRecipes) {
            if (!recipe.to.matches(potion)) continue;
            return true;
        }
        return false;
    }

    public boolean hasRecipe(ItemStack input, ItemStack ingredient) {
        if (!this.isPotionType(input)) {
            return false;
        }
        return this.hasItemRecipe(input, ingredient) || this.hasPotionRecipe(input, ingredient);
    }

    public boolean hasItemRecipe(ItemStack input, ItemStack ingredient) {
        for (Recipe<Item> recipe : this.itemRecipes) {
            if (!input.itemMatches(recipe.from) || !recipe.ingredient.test(ingredient)) continue;
            return true;
        }
        return false;
    }

    public boolean hasPotionRecipe(ItemStack input, ItemStack ingredient) {
        Optional<RegistryEntry<Potion>> optional = input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
        if (optional.isEmpty()) {
            return false;
        }
        for (Recipe<Potion> recipe : this.potionRecipes) {
            if (!recipe.from.matches(optional.get()) || !recipe.ingredient.test(ingredient)) continue;
            return true;
        }
        return false;
    }

    public ItemStack craft(ItemStack ingredient, ItemStack input) {
        if (input.isEmpty()) {
            return input;
        }
        Optional<RegistryEntry<Potion>> optional = input.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion();
        if (optional.isEmpty()) {
            return input;
        }
        for (Recipe<Item> recipe : this.itemRecipes) {
            if (!input.itemMatches(recipe.from) || !recipe.ingredient.test(ingredient)) continue;
            return PotionContentsComponent.createStack((Item)recipe.to.value(), optional.get());
        }
        for (Recipe<ToggleableFeature> recipe : this.potionRecipes) {
            if (!recipe.from.matches(optional.get()) || !recipe.ingredient.test(ingredient)) continue;
            return PotionContentsComponent.createStack(input.getItem(), recipe.to);
        }
        return input;
    }

    public static BrewingRecipeRegistry create(FeatureSet enabledFeatures) {
        Builder builder = new Builder(enabledFeatures);
        BrewingRecipeRegistry.registerDefaults(builder);
        return builder.build();
    }

    public static void registerDefaults(Builder builder) {
        builder.registerPotionType(Items.POTION);
        builder.registerPotionType(Items.SPLASH_POTION);
        builder.registerPotionType(Items.LINGERING_POTION);
        builder.registerItemRecipe(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
        builder.registerItemRecipe(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
        builder.registerPotionRecipe(Potions.WATER, Items.GLOWSTONE_DUST, Potions.THICK);
        builder.registerPotionRecipe(Potions.WATER, Items.REDSTONE, Potions.MUNDANE);
        builder.registerPotionRecipe(Potions.WATER, Items.NETHER_WART, Potions.AWKWARD);
        builder.registerRecipes(Items.BREEZE_ROD, Potions.WIND_CHARGED);
        builder.registerRecipes(Items.SLIME_BLOCK, Potions.OOZING);
        builder.registerRecipes(Items.STONE, Potions.INFESTED);
        builder.registerRecipes(Items.COBWEB, Potions.WEAVING);
        builder.registerPotionRecipe(Potions.AWKWARD, Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        builder.registerPotionRecipe(Potions.NIGHT_VISION, Items.REDSTONE, Potions.LONG_NIGHT_VISION);
        builder.registerPotionRecipe(Potions.NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.INVISIBILITY);
        builder.registerPotionRecipe(Potions.LONG_NIGHT_VISION, Items.FERMENTED_SPIDER_EYE, Potions.LONG_INVISIBILITY);
        builder.registerPotionRecipe(Potions.INVISIBILITY, Items.REDSTONE, Potions.LONG_INVISIBILITY);
        builder.registerRecipes(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        builder.registerPotionRecipe(Potions.FIRE_RESISTANCE, Items.REDSTONE, Potions.LONG_FIRE_RESISTANCE);
        builder.registerRecipes(Items.RABBIT_FOOT, Potions.LEAPING);
        builder.registerPotionRecipe(Potions.LEAPING, Items.REDSTONE, Potions.LONG_LEAPING);
        builder.registerPotionRecipe(Potions.LEAPING, Items.GLOWSTONE_DUST, Potions.STRONG_LEAPING);
        builder.registerPotionRecipe(Potions.LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.registerPotionRecipe(Potions.LONG_LEAPING, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.registerPotionRecipe(Potions.SLOWNESS, Items.REDSTONE, Potions.LONG_SLOWNESS);
        builder.registerPotionRecipe(Potions.SLOWNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SLOWNESS);
        builder.registerPotionRecipe(Potions.AWKWARD, Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        builder.registerPotionRecipe(Potions.TURTLE_MASTER, Items.REDSTONE, Potions.LONG_TURTLE_MASTER);
        builder.registerPotionRecipe(Potions.TURTLE_MASTER, Items.GLOWSTONE_DUST, Potions.STRONG_TURTLE_MASTER);
        builder.registerPotionRecipe(Potions.SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.SLOWNESS);
        builder.registerPotionRecipe(Potions.LONG_SWIFTNESS, Items.FERMENTED_SPIDER_EYE, Potions.LONG_SLOWNESS);
        builder.registerRecipes(Items.SUGAR, Potions.SWIFTNESS);
        builder.registerPotionRecipe(Potions.SWIFTNESS, Items.REDSTONE, Potions.LONG_SWIFTNESS);
        builder.registerPotionRecipe(Potions.SWIFTNESS, Items.GLOWSTONE_DUST, Potions.STRONG_SWIFTNESS);
        builder.registerPotionRecipe(Potions.AWKWARD, Items.PUFFERFISH, Potions.WATER_BREATHING);
        builder.registerPotionRecipe(Potions.WATER_BREATHING, Items.REDSTONE, Potions.LONG_WATER_BREATHING);
        builder.registerRecipes(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        builder.registerPotionRecipe(Potions.HEALING, Items.GLOWSTONE_DUST, Potions.STRONG_HEALING);
        builder.registerPotionRecipe(Potions.HEALING, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.registerPotionRecipe(Potions.STRONG_HEALING, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.registerPotionRecipe(Potions.HARMING, Items.GLOWSTONE_DUST, Potions.STRONG_HARMING);
        builder.registerPotionRecipe(Potions.POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.registerPotionRecipe(Potions.LONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.HARMING);
        builder.registerPotionRecipe(Potions.STRONG_POISON, Items.FERMENTED_SPIDER_EYE, Potions.STRONG_HARMING);
        builder.registerRecipes(Items.SPIDER_EYE, Potions.POISON);
        builder.registerPotionRecipe(Potions.POISON, Items.REDSTONE, Potions.LONG_POISON);
        builder.registerPotionRecipe(Potions.POISON, Items.GLOWSTONE_DUST, Potions.STRONG_POISON);
        builder.registerRecipes(Items.GHAST_TEAR, Potions.REGENERATION);
        builder.registerPotionRecipe(Potions.REGENERATION, Items.REDSTONE, Potions.LONG_REGENERATION);
        builder.registerPotionRecipe(Potions.REGENERATION, Items.GLOWSTONE_DUST, Potions.STRONG_REGENERATION);
        builder.registerRecipes(Items.BLAZE_POWDER, Potions.STRENGTH);
        builder.registerPotionRecipe(Potions.STRENGTH, Items.REDSTONE, Potions.LONG_STRENGTH);
        builder.registerPotionRecipe(Potions.STRENGTH, Items.GLOWSTONE_DUST, Potions.STRONG_STRENGTH);
        builder.registerPotionRecipe(Potions.WATER, Items.FERMENTED_SPIDER_EYE, Potions.WEAKNESS);
        builder.registerPotionRecipe(Potions.WEAKNESS, Items.REDSTONE, Potions.LONG_WEAKNESS);
        builder.registerPotionRecipe(Potions.AWKWARD, Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
        builder.registerPotionRecipe(Potions.SLOW_FALLING, Items.REDSTONE, Potions.LONG_SLOW_FALLING);
    }

    record Recipe<T>(RegistryEntry<T> from, Ingredient ingredient, RegistryEntry<T> to) {
    }

    public static class Builder
    implements FabricBrewingRecipeRegistryBuilder {
        private final List<Ingredient> potionTypes = new ArrayList<Ingredient>();
        private final List<Recipe<Potion>> potionRecipes = new ArrayList<Recipe<Potion>>();
        private final List<Recipe<Item>> itemRecipes = new ArrayList<Recipe<Item>>();
        private final FeatureSet enabledFeatures;

        public Builder(FeatureSet enabledFeatures) {
            this.enabledFeatures = enabledFeatures;
        }

        private static void assertPotion(Item potionType) {
            if (!(potionType instanceof PotionItem)) {
                throw new IllegalArgumentException("Expected a potion, got: " + String.valueOf(Registries.ITEM.getId(potionType)));
            }
        }

        public void registerItemRecipe(Item input, Item ingredient, Item output) {
            if (!(input.isEnabled(this.enabledFeatures) && ingredient.isEnabled(this.enabledFeatures) && output.isEnabled(this.enabledFeatures))) {
                return;
            }
            Builder.assertPotion(input);
            Builder.assertPotion(output);
            this.itemRecipes.add(new Recipe<Item>(input.getRegistryEntry(), Ingredient.ofItems(ingredient), output.getRegistryEntry()));
        }

        public void registerPotionType(Item item) {
            if (!item.isEnabled(this.enabledFeatures)) {
                return;
            }
            Builder.assertPotion(item);
            this.potionTypes.add(Ingredient.ofItems(item));
        }

        public void registerPotionRecipe(RegistryEntry<Potion> input, Item ingredient, RegistryEntry<Potion> output) {
            if (input.value().isEnabled(this.enabledFeatures) && ingredient.isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
                this.potionRecipes.add(new Recipe<Potion>(input, Ingredient.ofItems(ingredient), output));
            }
        }

        public void registerRecipes(Item ingredient, RegistryEntry<Potion> potion) {
            if (potion.value().isEnabled(this.enabledFeatures)) {
                this.registerPotionRecipe(Potions.WATER, ingredient, Potions.MUNDANE);
                this.registerPotionRecipe(Potions.AWKWARD, ingredient, potion);
            }
        }

        public BrewingRecipeRegistry build() {
            return new BrewingRecipeRegistry(List.copyOf(this.potionTypes), List.copyOf(this.potionRecipes), List.copyOf(this.itemRecipes));
        }
    }
}

