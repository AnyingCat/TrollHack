/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item.trim;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record ArmorTrimPattern(Identifier assetId, RegistryEntry<Item> templateItem, Text description, boolean decal) {
    public static final Codec<ArmorTrimPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("asset_id")).forGetter(ArmorTrimPattern::assetId), ((MapCodec)RegistryFixedCodec.of(RegistryKeys.ITEM).fieldOf("template_item")).forGetter(ArmorTrimPattern::templateItem), ((MapCodec)TextCodecs.CODEC.fieldOf("description")).forGetter(ArmorTrimPattern::description), ((MapCodec)Codec.BOOL.fieldOf("decal")).orElse(false).forGetter(ArmorTrimPattern::decal)).apply((Applicative<ArmorTrimPattern, ?>)instance, ArmorTrimPattern::new));
    public static final PacketCodec<RegistryByteBuf, ArmorTrimPattern> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, ArmorTrimPattern::assetId, PacketCodecs.registryEntry(RegistryKeys.ITEM), ArmorTrimPattern::templateItem, TextCodecs.REGISTRY_PACKET_CODEC, ArmorTrimPattern::description, PacketCodecs.BOOL, ArmorTrimPattern::decal, ArmorTrimPattern::new);
    public static final Codec<RegistryEntry<ArmorTrimPattern>> ENTRY_CODEC = RegistryElementCodec.of(RegistryKeys.TRIM_PATTERN, CODEC);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<ArmorTrimPattern>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.TRIM_PATTERN, PACKET_CODEC);

    public Text getDescription(RegistryEntry<ArmorTrimMaterial> material) {
        return this.description.copy().fillStyle(material.value().description().getStyle());
    }
}

