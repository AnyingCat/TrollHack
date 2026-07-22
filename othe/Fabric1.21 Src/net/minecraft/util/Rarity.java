/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum Rarity implements StringIdentifiable
{
    COMMON(0, "common", Formatting.WHITE),
    UNCOMMON(1, "uncommon", Formatting.YELLOW),
    RARE(2, "rare", Formatting.AQUA),
    EPIC(3, "epic", Formatting.LIGHT_PURPLE);

    public static final Codec<Rarity> CODEC;
    public static final IntFunction<Rarity> ID_TO_VALUE;
    public static final PacketCodec<ByteBuf, Rarity> PACKET_CODEC;
    private final int index;
    private final String name;
    private final Formatting formatting;

    private Rarity(int index, String name, Formatting formatting) {
        this.index = index;
        this.name = name;
        this.formatting = formatting;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    @Override
    public String asString() {
        return this.name;
    }

    static {
        CODEC = StringIdentifiable.createBasicCodec(Rarity::values);
        ID_TO_VALUE = ValueLists.createIdToValueFunction(value -> value.index, Rarity.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, value -> value.index);
    }
}

