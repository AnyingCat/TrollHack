/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.NbtTextContent;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.ScoreTextContent;
import net.minecraft.text.SelectorTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public class TextCodecs {
    public static final Codec<Text> CODEC = Codec.recursive("Component", TextCodecs::createCodec);
    public static final PacketCodec<RegistryByteBuf, Text> REGISTRY_PACKET_CODEC = PacketCodecs.registryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_PACKET_CODEC = REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<RegistryByteBuf, Text> UNLIMITED_REGISTRY_PACKET_CODEC = PacketCodecs.unlimitedRegistryCodec(CODEC);
    public static final PacketCodec<RegistryByteBuf, Optional<Text>> OPTIONAL_UNLIMITED_REGISTRY_PACKET_CODEC = UNLIMITED_REGISTRY_PACKET_CODEC.collect(PacketCodecs::optional);
    public static final PacketCodec<ByteBuf, Text> PACKET_CODEC = PacketCodecs.unlimitedCodec(CODEC);
    public static final Codec<Text> STRINGIFIED_CODEC = TextCodecs.codec(Integer.MAX_VALUE);

    public static Codec<Text> codec(int maxSerializedLength) {
        final Codec<String> codec = Codec.string(0, maxSerializedLength);
        return new Codec<Text>(){

            @Override
            public <T> DataResult<Pair<Text, T>> decode(DynamicOps<T> ops, T input) {
                DynamicOps<JsonElement> dynamicOps = _1.toJsonOps(ops);
                return codec.decode(ops, input).flatMap((? super R pair) -> {
                    try {
                        JsonElement jsonElement = JsonParser.parseString((String)pair.getFirst());
                        return CODEC.parse(dynamicOps, jsonElement).map((? super R text) -> Pair.of(text, pair.getSecond()));
                    } catch (JsonParseException jsonParseException) {
                        return DataResult.error(jsonParseException::getMessage);
                    }
                });
            }

            @Override
            public <T> DataResult<T> encode(Text text, DynamicOps<T> dynamicOps, T object) {
                DynamicOps<JsonElement> dynamicOps2 = _1.toJsonOps(dynamicOps);
                return CODEC.encodeStart(dynamicOps2, text).flatMap((? super R json) -> {
                    try {
                        return codec.encodeStart(dynamicOps, JsonHelper.toSortedString(json));
                    } catch (IllegalArgumentException illegalArgumentException) {
                        return DataResult.error(illegalArgumentException::getMessage);
                    }
                });
            }

            private static <T> DynamicOps<JsonElement> toJsonOps(DynamicOps<T> ops) {
                if (ops instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)ops;
                    return registryOps.withDelegate(JsonOps.INSTANCE);
                }
                return JsonOps.INSTANCE;
            }

            @Override
            public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
                return this.encode((Text)input, ops, prefix);
            }
        };
    }

    private static MutableText combine(List<Text> texts) {
        MutableText mutableText = texts.get(0).copy();
        for (int i = 1; i < texts.size(); ++i) {
            mutableText.append(texts.get(i));
        }
        return mutableText;
    }

    public static <T extends StringIdentifiable, E> MapCodec<E> dispatchingCodec(T[] types, Function<T, MapCodec<? extends E>> typeToCodec, Function<E, T> valueToType, String dispatchingKey) {
        FuzzyCodec<Object> mapCodec = new FuzzyCodec<Object>(Stream.of(types).map(typeToCodec).toList(), object -> (MapEncoder)typeToCodec.apply((StringIdentifiable)valueToType.apply(object)));
        Codec codec = StringIdentifiable.createBasicCodec(() -> types);
        MapCodec<? extends E> mapCodec2 = codec.dispatchMap(dispatchingKey, valueToType, typeToCodec);
        DispatchingCodec<Object> mapCodec3 = new DispatchingCodec<Object>(dispatchingKey, mapCodec2, mapCodec);
        return Codecs.orCompressed(mapCodec3, mapCodec2);
    }

    private static Codec<Text> createCodec(Codec<Text> selfCodec) {
        StringIdentifiable[] types = new TextContent.Type[]{PlainTextContent.TYPE, TranslatableTextContent.TYPE, KeybindTextContent.TYPE, ScoreTextContent.TYPE, SelectorTextContent.TYPE, NbtTextContent.TYPE};
        MapCodec mapCodec = TextCodecs.dispatchingCodec((StringIdentifiable[])types, TextContent.Type::codec, TextContent::getType, (String)"type");
        Codec codec = RecordCodecBuilder.create(instance -> instance.group(mapCodec.forGetter(Text::getContent), Codecs.nonEmptyList(selfCodec.listOf()).optionalFieldOf("extra", List.of()).forGetter(Text::getSiblings), Style.Codecs.MAP_CODEC.forGetter(Text::getStyle)).apply((Applicative<Text, ?>)instance, MutableText::new));
        return Codec.either(Codec.either(Codec.STRING, Codecs.nonEmptyList(selfCodec.listOf())), codec).xmap(either -> either.map(either2 -> either2.map(Text::literal, TextCodecs::combine), text -> text), text -> {
            String string = text.getLiteralString();
            return string != null ? Either.left(Either.left(string)) : Either.right(text);
        });
    }

    static class FuzzyCodec<T>
    extends MapCodec<T> {
        private final List<MapCodec<? extends T>> codecs;
        private final Function<T, MapEncoder<? extends T>> codecGetter;

        public FuzzyCodec(List<MapCodec<? extends T>> codecs, Function<T, MapEncoder<? extends T>> codecGetter) {
            this.codecs = codecs;
            this.codecGetter = codecGetter;
        }

        @Override
        public <S> DataResult<T> decode(DynamicOps<S> ops, MapLike<S> input) {
            for (MapDecoder mapDecoder : this.codecs) {
                DataResult dataResult = mapDecoder.decode(ops, input);
                if (!dataResult.result().isPresent()) continue;
                return dataResult;
            }
            return DataResult.error(() -> "No matching codec found");
        }

        @Override
        public <S> RecordBuilder<S> encode(T input, DynamicOps<S> ops, RecordBuilder<S> prefix) {
            MapEncoder<S> mapEncoder = this.codecGetter.apply(input);
            return mapEncoder.encode(input, ops, prefix);
        }

        @Override
        public <S> Stream<S> keys(DynamicOps<S> ops) {
            return this.codecs.stream().flatMap((? super T codec) -> codec.keys(ops)).distinct();
        }

        public String toString() {
            return "FuzzyCodec[" + String.valueOf(this.codecs) + "]";
        }
    }

    static class DispatchingCodec<T>
    extends MapCodec<T> {
        private final String dispatchingKey;
        private final MapCodec<T> withKeyCodec;
        private final MapCodec<T> withoutKeyCodec;

        public DispatchingCodec(String dispatchingKey, MapCodec<T> withKeyCodec, MapCodec<T> withoutKeyCodec) {
            this.dispatchingKey = dispatchingKey;
            this.withKeyCodec = withKeyCodec;
            this.withoutKeyCodec = withoutKeyCodec;
        }

        @Override
        public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
            if (input.get(this.dispatchingKey) != null) {
                return this.withKeyCodec.decode(ops, input);
            }
            return this.withoutKeyCodec.decode(ops, input);
        }

        @Override
        public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
            return this.withoutKeyCodec.encode(input, ops, prefix);
        }

        @Override
        public <T1> Stream<T1> keys(DynamicOps<T1> ops) {
            return Stream.concat(this.withKeyCodec.keys(ops), this.withoutKeyCodec.keys(ops)).distinct();
        }
    }
}

