/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.decoration.painting;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record PaintingVariant(int width, int height, Identifier assetId) {
    public static final Codec<PaintingVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.rangedInt(1, 16).fieldOf("width")).forGetter(PaintingVariant::width), ((MapCodec)Codecs.rangedInt(1, 16).fieldOf("height")).forGetter(PaintingVariant::height), ((MapCodec)Identifier.CODEC.fieldOf("asset_id")).forGetter(PaintingVariant::assetId)).apply((Applicative<PaintingVariant, ?>)instance, PaintingVariant::new));
    public static final PacketCodec<ByteBuf, PaintingVariant> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, PaintingVariant::width, PacketCodecs.VAR_INT, PaintingVariant::height, Identifier.PACKET_CODEC, PaintingVariant::assetId, PaintingVariant::new);
    public static final Codec<RegistryEntry<PaintingVariant>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.PAINTING_VARIANT, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<PaintingVariant>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.PAINTING_VARIANT, PACKET_CODEC);

    public int getArea() {
        return this.width() * this.height();
    }
}

