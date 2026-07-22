/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;

/**
 * A serializer for hardcoded recipes. The recipes with this serializer don't
 * transport any extra data besides their ID when read from JSON or synchronized
 * over network.
 * 
 * <p>The name "special" comes from the fact that in vanilla, recipes using this
 * serializer have IDs starting with {@code crafting_special_}. All of their logic and ingredients
 * are also defined in code, which distinguishes them from "non-special" recipes.
 */
public class SpecialRecipeSerializer<T extends CraftingRecipe>
implements RecipeSerializer<T> {
    private final MapCodec<T> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)CraftingRecipeCategory.CODEC.fieldOf("category")).orElse(CraftingRecipeCategory.MISC).forGetter(CraftingRecipe::getCategory)).apply((Applicative<CraftingRecipe, ?>)instance, factory::create));
    private final PacketCodec<RegistryByteBuf, T> packetCodec = PacketCodec.tuple(CraftingRecipeCategory.PACKET_CODEC, CraftingRecipe::getCategory, factory::create);

    public SpecialRecipeSerializer(Factory<T> factory) {
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public PacketCodec<RegistryByteBuf, T> packetCodec() {
        return this.packetCodec;
    }

    @FunctionalInterface
    public static interface Factory<T extends CraftingRecipe> {
        public T create(CraftingRecipeCategory var1);
    }
}

