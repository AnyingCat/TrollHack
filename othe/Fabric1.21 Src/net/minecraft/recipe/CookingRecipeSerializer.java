/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CookingRecipeCategory;

public class CookingRecipeSerializer<T extends AbstractCookingRecipe>
implements RecipeSerializer<T> {
    private final AbstractCookingRecipe.RecipeFactory<T> recipeFactory;
    private final MapCodec<T> codec;
    private final PacketCodec<RegistryByteBuf, T> packetCodec;

    public CookingRecipeSerializer(AbstractCookingRecipe.RecipeFactory<T> recipeFactory, int cookingTime) {
        this.recipeFactory = recipeFactory;
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group), ((MapCodec)CookingRecipeCategory.CODEC.fieldOf("category")).orElse(CookingRecipeCategory.MISC).forGetter(recipe -> recipe.category), ((MapCodec)Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient")).forGetter(recipe -> recipe.ingredient), ((MapCodec)ItemStack.VALIDATED_UNCOUNTED_CODEC.fieldOf("result")).forGetter(recipe -> recipe.result), ((MapCodec)Codec.FLOAT.fieldOf("experience")).orElse(Float.valueOf(0.0f)).forGetter(recipe -> Float.valueOf(recipe.experience)), ((MapCodec)Codec.INT.fieldOf("cookingtime")).orElse(cookingTime).forGetter(recipe -> recipe.cookingTime)).apply((Applicative<AbstractCookingRecipe, ?>)instance, recipeFactory::create));
        this.packetCodec = PacketCodec.ofStatic(this::write, this::read);
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public PacketCodec<RegistryByteBuf, T> packetCodec() {
        return this.packetCodec;
    }

    private T read(RegistryByteBuf buf) {
        String string = buf.readString();
        CookingRecipeCategory cookingRecipeCategory = buf.readEnumConstant(CookingRecipeCategory.class);
        Ingredient ingredient = (Ingredient)Ingredient.PACKET_CODEC.decode(buf);
        ItemStack itemStack = (ItemStack)ItemStack.PACKET_CODEC.decode(buf);
        float f = buf.readFloat();
        int i = buf.readVarInt();
        return this.recipeFactory.create(string, cookingRecipeCategory, ingredient, itemStack, f, i);
    }

    private void write(RegistryByteBuf buf, T recipe) {
        buf.writeString(((AbstractCookingRecipe)recipe).group);
        buf.writeEnumConstant(((AbstractCookingRecipe)recipe).getCategory());
        Ingredient.PACKET_CODEC.encode(buf, ((AbstractCookingRecipe)recipe).ingredient);
        ItemStack.PACKET_CODEC.encode(buf, ((AbstractCookingRecipe)recipe).result);
        buf.writeFloat(((AbstractCookingRecipe)recipe).experience);
        buf.writeVarInt(((AbstractCookingRecipe)recipe).cookingTime);
    }

    public AbstractCookingRecipe create(String group, CookingRecipeCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        return this.recipeFactory.create(group, category, ingredient, result, experience, cookingTime);
    }
}

