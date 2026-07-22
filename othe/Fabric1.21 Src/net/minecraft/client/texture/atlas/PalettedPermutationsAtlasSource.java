/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.texture.atlas;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteDimensions;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceManager;
import net.minecraft.client.texture.atlas.AtlasSourceType;
import net.minecraft.client.texture.atlas.AtlasSprite;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class PalettedPermutationsAtlasSource
implements AtlasSource {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<PalettedPermutationsAtlasSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.list(Identifier.CODEC).fieldOf("textures")).forGetter(source -> source.textures), ((MapCodec)Identifier.CODEC.fieldOf("palette_key")).forGetter(source -> source.paletteKey), ((MapCodec)Codec.unboundedMap(Codec.STRING, Identifier.CODEC).fieldOf("permutations")).forGetter(source -> source.permutations)).apply((Applicative<PalettedPermutationsAtlasSource, ?>)instance, PalettedPermutationsAtlasSource::new));
    private final List<Identifier> textures;
    private final Map<String, Identifier> permutations;
    private final Identifier paletteKey;

    private PalettedPermutationsAtlasSource(List<Identifier> textures, Identifier paletteKey, Map<String, Identifier> permutations) {
        this.textures = textures;
        this.permutations = permutations;
        this.paletteKey = paletteKey;
    }

    @Override
    public void load(ResourceManager resourceManager, AtlasSource.SpriteRegions regions) {
        Supplier<int[]> supplier = Suppliers.memoize(() -> PalettedPermutationsAtlasSource.open(resourceManager, this.paletteKey));
        HashMap map = new HashMap();
        this.permutations.forEach((key, texture) -> map.put(key, Suppliers.memoize(() -> PalettedPermutationsAtlasSource.toMapper((int[])((java.util.function.Supplier)supplier).get(), PalettedPermutationsAtlasSource.open(resourceManager, texture)))));
        for (Identifier identifier : this.textures) {
            Identifier identifier2 = RESOURCE_FINDER.toResourcePath(identifier);
            Optional<Resource> optional = resourceManager.getResource(identifier2);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", (Object)identifier2);
                continue;
            }
            AtlasSprite atlasSprite = new AtlasSprite(identifier2, optional.get(), map.size());
            for (Map.Entry entry : map.entrySet()) {
                Identifier identifier3 = identifier.withSuffixedPath("_" + (String)entry.getKey());
                regions.add(identifier3, new PalettedSpriteRegion(atlasSprite, (java.util.function.Supplier)entry.getValue(), identifier3));
            }
        }
    }

    private static IntUnaryOperator toMapper(int[] from, int[] to) {
        if (to.length != from.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", (Object)from.length, (Object)to.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap(to.length);
        for (int i = 0; i < from.length; ++i) {
            int j = from[i];
            if (ColorHelper.Abgr.getAlpha(j) == 0) continue;
            int2IntMap.put(ColorHelper.Abgr.getBgr(j), to[i]);
        }
        return color -> {
            int i = ColorHelper.Abgr.getAlpha(color);
            if (i == 0) {
                return color;
            }
            int j = ColorHelper.Abgr.getBgr(color);
            int k = int2IntMap.getOrDefault(j, ColorHelper.Abgr.toOpaque(j));
            int l = ColorHelper.Abgr.getAlpha(k);
            return ColorHelper.Abgr.withAlpha(i * l / 255, k);
        };
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static int[] open(ResourceManager resourceManager, Identifier texture) {
        Optional<Resource> optional = resourceManager.getResource(RESOURCE_FINDER.toResourcePath(texture));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", (Object)texture);
            throw new IllegalArgumentException();
        }
        try (InputStream inputStream = optional.get().getInputStream();){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int[] nArray = nativeImage.copyPixelsRgba();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            } catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    } catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", (Object)texture, (Object)exception);
            throw new IllegalArgumentException();
        }
    }

    @Override
    public AtlasSourceType getType() {
        return AtlasSourceManager.PALETTED_PERMUTATIONS;
    }

    @Environment(value=EnvType.CLIENT)
    record PalettedSpriteRegion(AtlasSprite baseImage, java.util.function.Supplier<IntUnaryOperator> palette, Identifier permutationLocation) implements AtlasSource.SpriteRegion
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable
        public SpriteContents apply(SpriteOpener spriteOpener) {
            try {
                NativeImage nativeImage = this.baseImage.read().applyToCopy(this.palette.get());
                SpriteContents spriteContents = new SpriteContents(this.permutationLocation, new SpriteDimensions(nativeImage.getWidth(), nativeImage.getHeight()), nativeImage, ResourceMetadata.NONE);
                return spriteContents;
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("unable to apply palette to {}", (Object)this.permutationLocation, (Object)exception);
                SpriteContents spriteContents = null;
                return spriteContents;
            } finally {
                this.baseImage.close();
            }
        }

        @Override
        public void close() {
            this.baseImage.close();
        }

        @Override
        @Nullable
        public /* synthetic */ Object apply(Object opener) {
            return this.apply((SpriteOpener)opener);
        }
    }
}

