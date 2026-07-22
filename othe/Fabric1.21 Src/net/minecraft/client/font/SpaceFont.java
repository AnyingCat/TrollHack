/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.Glyph;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpaceFont
implements Font {
    private final Int2ObjectMap<Glyph.EmptyGlyph> codePointsToGlyphs;

    public SpaceFont(Map<Integer, Float> codePointsToAdvances) {
        this.codePointsToGlyphs = new Int2ObjectOpenHashMap<Glyph.EmptyGlyph>(codePointsToAdvances.size());
        codePointsToAdvances.forEach((codePoint, glyph) -> this.codePointsToGlyphs.put((int)codePoint, () -> glyph.floatValue()));
    }

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        return (Glyph)this.codePointsToGlyphs.get(codePoint);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return IntSets.unmodifiable(this.codePointsToGlyphs.keySet());
    }

    @Environment(value=EnvType.CLIENT)
    public record Loader(Map<Integer, Float> advances) implements FontLoader
    {
        public static final MapCodec<Loader> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codec.unboundedMap(Codecs.CODEPOINT, Codec.FLOAT).fieldOf("advances")).forGetter(Loader::advances)).apply((Applicative<Loader, ?>)instance, Loader::new));

        @Override
        public FontType getType() {
            return FontType.SPACE;
        }

        @Override
        public Either<FontLoader.Loadable, FontLoader.Reference> build() {
            FontLoader.Loadable loadable = resourceManager -> new SpaceFont(this.advances);
            return Either.left(loadable);
        }
    }
}

