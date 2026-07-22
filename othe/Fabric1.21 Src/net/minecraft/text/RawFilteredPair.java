/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.filter.FilteredMessage;

public record RawFilteredPair<T>(T raw, Optional<T> filtered) {
    public static <T> Codec<RawFilteredPair<T>> createCodec(Codec<T> baseCodec) {
        Codec codec = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)baseCodec.fieldOf("raw")).forGetter(RawFilteredPair::raw), baseCodec.optionalFieldOf("filtered").forGetter(RawFilteredPair::filtered)).apply((Applicative<RawFilteredPair, ?>)instance, RawFilteredPair::new));
        Codec<RawFilteredPair> codec2 = baseCodec.xmap(RawFilteredPair::of, RawFilteredPair::raw);
        return Codec.withAlternative(codec, codec2);
    }

    public static <B extends ByteBuf, T> PacketCodec<B, RawFilteredPair<T>> createPacketCodec(PacketCodec<B, T> basePacketCodec) {
        return PacketCodec.tuple(basePacketCodec, RawFilteredPair::raw, basePacketCodec.collect(PacketCodecs::optional), RawFilteredPair::filtered, RawFilteredPair::new);
    }

    public static <T> RawFilteredPair<T> of(T raw) {
        return new RawFilteredPair<T>(raw, Optional.empty());
    }

    public static RawFilteredPair<String> of(FilteredMessage message) {
        return new RawFilteredPair<String>(message.raw(), message.isFiltered() ? Optional.of(message.getString()) : Optional.empty());
    }

    public T get(boolean shouldFilter) {
        if (shouldFilter) {
            return this.filtered.orElse(this.raw);
        }
        return this.raw;
    }

    public <U> RawFilteredPair<U> map(Function<T, U> mapper) {
        return new RawFilteredPair<U>(mapper.apply(this.raw), this.filtered.map(mapper));
    }

    public <U> Optional<RawFilteredPair<U>> resolve(Function<T, Optional<U>> resolver) {
        Optional<U> optional = resolver.apply(this.raw);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        if (this.filtered.isPresent()) {
            Optional<U> optional2 = resolver.apply(this.filtered.get());
            if (optional2.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new RawFilteredPair<U>(optional.get(), optional2));
        }
        return Optional.of(new RawFilteredPair<U>(optional.get(), Optional.empty()));
    }
}

