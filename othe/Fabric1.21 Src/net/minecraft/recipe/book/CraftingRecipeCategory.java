/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.recipe.book;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum CraftingRecipeCategory implements StringIdentifiable
{
    BUILDING("building", 0),
    REDSTONE("redstone", 1),
    EQUIPMENT("equipment", 2),
    MISC("misc", 3);

    public static final Codec<CraftingRecipeCategory> CODEC;
    public static final IntFunction<CraftingRecipeCategory> INDEX_TO_VALUE;
    public static final PacketCodec<ByteBuf, CraftingRecipeCategory> PACKET_CODEC;
    private final String id;
    private final int index;

    private CraftingRecipeCategory(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    static {
        CODEC = StringIdentifiable.createCodec(CraftingRecipeCategory::values);
        INDEX_TO_VALUE = ValueLists.createIdToValueFunction(CraftingRecipeCategory::getIndex, CraftingRecipeCategory.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, CraftingRecipeCategory::getIndex);
    }
}

