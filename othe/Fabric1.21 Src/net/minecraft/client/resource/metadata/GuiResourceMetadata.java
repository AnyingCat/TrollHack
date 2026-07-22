/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.resource.metadata;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.Scaling;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

@Environment(value=EnvType.CLIENT)
public record GuiResourceMetadata(Scaling scaling) {
    public static final GuiResourceMetadata DEFAULT = new GuiResourceMetadata(Scaling.STRETCH);
    public static final Codec<GuiResourceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(Scaling.CODEC.optionalFieldOf("scaling", Scaling.STRETCH).forGetter(GuiResourceMetadata::scaling)).apply((Applicative<GuiResourceMetadata, ?>)instance, GuiResourceMetadata::new));
    public static final ResourceMetadataSerializer<GuiResourceMetadata> SERIALIZER = ResourceMetadataSerializer.fromCodec("gui", CODEC);
}

