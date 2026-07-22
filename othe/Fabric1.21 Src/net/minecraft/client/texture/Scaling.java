/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.texture;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

@Environment(value=EnvType.CLIENT)
public interface Scaling {
    public static final Codec<Scaling> CODEC = Type.CODEC.dispatch(Scaling::getType, Type::getCodec);
    public static final Scaling STRETCH = new Stretch();

    public Type getType();

    @Environment(value=EnvType.CLIENT)
    public static enum Type implements StringIdentifiable
    {
        STRETCH("stretch", Stretch.CODEC),
        TILE("tile", Tile.CODEC),
        NINE_SLICE("nine_slice", NineSlice.CODEC);

        public static final Codec<Type> CODEC;
        private final String name;
        private final MapCodec<? extends Scaling> codec;

        private Type(String name, MapCodec<? extends Scaling> codec) {
            this.name = name;
            this.codec = codec;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public MapCodec<? extends Scaling> getCodec() {
            return this.codec;
        }

        static {
            CODEC = StringIdentifiable.createCodec(Type::values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Stretch() implements Scaling
    {
        public static final MapCodec<Stretch> CODEC = MapCodec.unit(Stretch::new);

        @Override
        public Type getType() {
            return Type.STRETCH;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record NineSlice(int width, int height, Border border) implements Scaling
    {
        public static final MapCodec<NineSlice> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.POSITIVE_INT.fieldOf("width")).forGetter(NineSlice::width), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("height")).forGetter(NineSlice::height), ((MapCodec)Border.CODEC.fieldOf("border")).forGetter(NineSlice::border)).apply((Applicative<NineSlice, ?>)instance, NineSlice::new)).validate(NineSlice::validate);

        private static DataResult<NineSlice> validate(NineSlice nineSlice) {
            Border border = nineSlice.border();
            if (border.left() + border.right() >= nineSlice.width()) {
                return DataResult.error(() -> "Nine-sliced texture has no horizontal center slice: " + border.left() + " + " + border.right() + " >= " + nineSlice.width());
            }
            if (border.top() + border.bottom() >= nineSlice.height()) {
                return DataResult.error(() -> "Nine-sliced texture has no vertical center slice: " + border.top() + " + " + border.bottom() + " >= " + nineSlice.height());
            }
            return DataResult.success(nineSlice);
        }

        @Override
        public Type getType() {
            return Type.NINE_SLICE;
        }

        @Environment(value=EnvType.CLIENT)
        public record Border(int left, int top, int right, int bottom) {
            private static final Codec<Border> UNIFORM_SIDE_SIZES_CODEC = Codecs.POSITIVE_INT.flatComapMap(size -> new Border((int)size, (int)size, (int)size, (int)size), border -> {
                OptionalInt optionalInt = border.getUniformSideSize();
                if (optionalInt.isPresent()) {
                    return DataResult.success(optionalInt.getAsInt());
                }
                return DataResult.error(() -> "Border has different side sizes");
            });
            private static final Codec<Border> DIFFERENT_SIDE_SIZES_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("left")).forGetter(Border::left), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("top")).forGetter(Border::top), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("right")).forGetter(Border::right), ((MapCodec)Codecs.NONNEGATIVE_INT.fieldOf("bottom")).forGetter(Border::bottom)).apply((Applicative<Border, ?>)instance, Border::new));
            static final Codec<Border> CODEC = Codec.either(UNIFORM_SIDE_SIZES_CODEC, DIFFERENT_SIDE_SIZES_CODEC).xmap(Either::unwrap, border -> {
                if (border.getUniformSideSize().isPresent()) {
                    return Either.left(border);
                }
                return Either.right(border);
            });

            private OptionalInt getUniformSideSize() {
                if (this.left() == this.top() && this.top() == this.right() && this.right() == this.bottom()) {
                    return OptionalInt.of(this.left());
                }
                return OptionalInt.empty();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Tile(int width, int height) implements Scaling
    {
        public static final MapCodec<Tile> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Codecs.POSITIVE_INT.fieldOf("width")).forGetter(Tile::width), ((MapCodec)Codecs.POSITIVE_INT.fieldOf("height")).forGetter(Tile::height)).apply((Applicative<Tile, ?>)instance, Tile::new));

        @Override
        public Type getType() {
            return Type.TILE;
        }
    }
}

