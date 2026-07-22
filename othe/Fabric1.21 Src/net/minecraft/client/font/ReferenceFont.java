/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontType;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public record ReferenceFont(Identifier id) implements FontLoader
{
    public static final MapCodec<ReferenceFont> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("id")).forGetter(ReferenceFont::id)).apply((Applicative<ReferenceFont, ?>)instance, ReferenceFont::new));

    @Override
    public FontType getType() {
        return FontType.REFERENCE;
    }

    @Override
    public Either<FontLoader.Loadable, FontLoader.Reference> build() {
        return Either.right(new FontLoader.Reference(this.id));
    }
}

