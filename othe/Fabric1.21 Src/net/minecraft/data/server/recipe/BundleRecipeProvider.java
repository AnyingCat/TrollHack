/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.server.recipe;

import java.util.concurrent.CompletableFuture;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.data.DataOutput;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

public class BundleRecipeProvider
extends RecipeProvider {
    public BundleRecipeProvider(DataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(dataOutput, completableFuture);
    }

    @Override
    protected void generate(RecipeExporter exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.TOOLS, Items.BUNDLE).input(Character.valueOf('#'), Items.RABBIT_HIDE).input(Character.valueOf('-'), Items.STRING).pattern("-#-").pattern("# #").pattern("###").criterion("has_string", (AdvancementCriterion)BundleRecipeProvider.conditionsFromItem(Items.STRING)).offerTo(exporter);
    }
}

