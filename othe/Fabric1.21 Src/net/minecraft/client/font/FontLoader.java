/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.font;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.FontFilterType;
import net.minecraft.client.font.FontType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public interface FontLoader {
    public static final MapCodec<FontLoader> CODEC = FontType.CODEC.dispatchMap(FontLoader::getType, FontType::getLoaderCodec);

    public FontType getType();

    public Either<Loadable, Reference> build();

    @Environment(value=EnvType.CLIENT)
    public record Provider(FontLoader definition, FontFilterType.FilterMap filter) {
        public static final Codec<Provider> CODEC = RecordCodecBuilder.create(instance -> instance.group(CODEC.forGetter(Provider::definition), FontFilterType.FilterMap.CODEC.optionalFieldOf("filter", FontFilterType.FilterMap.NO_FILTER).forGetter(Provider::filter)).apply((Applicative<Provider, ?>)instance, Provider::new));
    }

    @Environment(value=EnvType.CLIENT)
    public record Reference(Identifier id) {
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Loadable {
        public Font load(ResourceManager var1) throws IOException;
    }
}

