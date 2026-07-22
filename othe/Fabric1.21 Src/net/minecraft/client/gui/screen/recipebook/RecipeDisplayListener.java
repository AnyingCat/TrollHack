/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.recipebook;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.recipe.RecipeEntry;

@Environment(value=EnvType.CLIENT)
public interface RecipeDisplayListener {
    public void onRecipesDisplayed(List<RecipeEntry<?>> var1);
}

