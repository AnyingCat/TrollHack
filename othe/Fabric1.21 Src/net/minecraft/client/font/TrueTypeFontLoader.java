/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontType;
import net.minecraft.client.font.FreeTypeUtil;
import net.minecraft.client.font.TrueTypeFont;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

@Environment(value=EnvType.CLIENT)
public record TrueTypeFontLoader(Identifier location, float size, float oversample, Shift shift, String skip) implements FontLoader
{
    private static final Codec<String> SKIP_CODEC = Codec.withAlternative(Codec.STRING, Codec.STRING.listOf(), chars -> String.join((CharSequence)"", chars));
    public static final MapCodec<TrueTypeFontLoader> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("file")).forGetter(TrueTypeFontLoader::location), Codec.FLOAT.optionalFieldOf("size", Float.valueOf(11.0f)).forGetter(TrueTypeFontLoader::size), Codec.FLOAT.optionalFieldOf("oversample", Float.valueOf(1.0f)).forGetter(TrueTypeFontLoader::oversample), Shift.CODEC.optionalFieldOf("shift", Shift.NONE).forGetter(TrueTypeFontLoader::shift), SKIP_CODEC.optionalFieldOf("skip", "").forGetter(TrueTypeFontLoader::skip)).apply((Applicative<TrueTypeFontLoader, ?>)instance, TrueTypeFontLoader::new));

    @Override
    public FontType getType() {
        return FontType.TTF;
    }

    @Override
    public Either<FontLoader.Loadable, FontLoader.Reference> build() {
        return Either.left(this::load);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private Font load(ResourceManager resourceManager) throws IOException {
        FT_Face fT_Face = null;
        ByteBuffer byteBuffer = null;
        try (InputStream inputStream = resourceManager.open(this.location.withPrefixedPath("font/"));){
            byteBuffer = TextureUtil.readResource(inputStream);
            byteBuffer.flip();
            Object object = FreeTypeUtil.LOCK;
            synchronized (object) {
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.checkFatalError(FreeType.FT_New_Memory_Face(FreeTypeUtil.initialize(), byteBuffer, 0L, pointerBuffer), "Initializing font face");
                    fT_Face = FT_Face.create(pointerBuffer.get());
                }
                String string = FreeType.FT_Get_Font_Format(fT_Face);
                if (!"TrueType".equals(string)) {
                    throw new IOException("Font is not in TTF format, was " + string);
                }
                FreeTypeUtil.checkFatalError(FreeType.FT_Select_Charmap(fT_Face, FreeType.FT_ENCODING_UNICODE), "Find unicode charmap");
                TrueTypeFont trueTypeFont = new TrueTypeFont(byteBuffer, fT_Face, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
                return trueTypeFont;
            }
        } catch (Exception exception) {
            Object object = FreeTypeUtil.LOCK;
            synchronized (object) {
                if (fT_Face != null) {
                    FreeType.FT_Done_Face(fT_Face);
                }
            }
            MemoryUtil.memFree(byteBuffer);
            throw exception;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Shift(float x, float y) {
        public static final Shift NONE = new Shift(0.0f, 0.0f);
        public static final Codec<Shift> CODEC = Codec.floatRange(-512.0f, 512.0f).listOf().comapFlatMap(floatList2 -> Util.decodeFixedLengthList(floatList2, 2).map(floatList -> new Shift(((Float)floatList.get(0)).floatValue(), ((Float)floatList.get(1)).floatValue())), shift -> List.of(Float.valueOf(shift.x), Float.valueOf(shift.y)));
    }
}

