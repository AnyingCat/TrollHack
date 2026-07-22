/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.texture;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SpriteOpener {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static SpriteOpener create(Collection<ResourceMetadataReader<?>> metadatas) {
        return (id, resource) -> {
            NativeImage nativeImage;
            ResourceMetadata resourceMetadata;
            try {
                resourceMetadata = resource.getMetadata().copy(metadatas);
            } catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", (Object)id, (Object)exception);
                return null;
            }
            try (InputStream inputStream = resource.getInputStream();){
                nativeImage = NativeImage.read(inputStream);
            } catch (IOException iOException) {
                LOGGER.error("Using missing texture, unable to load {}", (Object)id, (Object)iOException);
                return null;
            }
            AnimationResourceMetadata animationResourceMetadata = resourceMetadata.decode(AnimationResourceMetadata.READER).orElse(AnimationResourceMetadata.EMPTY);
            SpriteDimensions spriteDimensions = animationResourceMetadata.getSize(nativeImage.getWidth(), nativeImage.getHeight());
            if (MathHelper.isMultipleOf(nativeImage.getWidth(), spriteDimensions.width()) && MathHelper.isMultipleOf(nativeImage.getHeight(), spriteDimensions.height())) {
                return new SpriteContents(id, spriteDimensions, nativeImage, resourceMetadata);
            }
            LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", id, nativeImage.getWidth(), nativeImage.getHeight(), spriteDimensions.width(), spriteDimensions.height());
            nativeImage.close();
            return null;
        };
    }

    @Nullable
    public SpriteContents loadSprite(Identifier var1, Resource var2);
}

