/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encoding.VarInts;

public class PacketCodecDispatcher<B extends ByteBuf, V, T>
implements PacketCodec<B, V> {
    private static final int UNKNOWN_PACKET_INDEX = -1;
    private final Function<V, ? extends T> packetIdGetter;
    private final List<PacketType<B, V, T>> packetTypes;
    private final Object2IntMap<T> typeToIndex;

    PacketCodecDispatcher(Function<V, ? extends T> packetIdGetter, List<PacketType<B, V, T>> packetTypes, Object2IntMap<T> typeToIndex) {
        this.packetIdGetter = packetIdGetter;
        this.packetTypes = packetTypes;
        this.typeToIndex = typeToIndex;
    }

    @Override
    public V decode(B byteBuf) {
        int i = VarInts.read(byteBuf);
        if (i < 0 || i >= this.packetTypes.size()) {
            throw new DecoderException("Received unknown packet id " + i);
        }
        PacketType<B, V, T> packetType = this.packetTypes.get(i);
        try {
            return (V)packetType.codec.decode(byteBuf);
        } catch (Exception exception) {
            throw new DecoderException("Failed to decode packet '" + String.valueOf(packetType.id) + "'", exception);
        }
    }

    @Override
    public void encode(B byteBuf, V object) {
        T object2 = this.packetIdGetter.apply(object);
        int i = this.typeToIndex.getOrDefault((Object)object2, -1);
        if (i == -1) {
            throw new EncoderException("Sending unknown packet '" + String.valueOf(object2) + "'");
        }
        VarInts.write(byteBuf, i);
        PacketType<B, V, T> packetType = this.packetTypes.get(i);
        try {
            PacketCodec packetCodec = packetType.codec;
            packetCodec.encode(byteBuf, object);
        } catch (Exception exception) {
            throw new EncoderException("Failed to encode packet '" + String.valueOf(object2) + "'", exception);
        }
    }

    public static <B extends ByteBuf, V, T> Builder<B, V, T> builder(Function<V, ? extends T> packetIdGetter) {
        return new Builder(packetIdGetter);
    }

    @Override
    public /* synthetic */ void encode(Object object, Object object2) {
        this.encode((B)((ByteBuf)object), (V)object2);
    }

    @Override
    public /* synthetic */ Object decode(Object object) {
        return this.decode((B)((ByteBuf)object));
    }

    record PacketType<B, V, T>(PacketCodec<? super B, ? extends V> codec, T id) {
    }

    public static class Builder<B extends ByteBuf, V, T> {
        private final List<PacketType<B, V, T>> packetTypes = new ArrayList<PacketType<B, V, T>>();
        private final Function<V, ? extends T> packetIdGetter;

        Builder(Function<V, ? extends T> packetIdGetter) {
            this.packetIdGetter = packetIdGetter;
        }

        public Builder<B, V, T> add(T id, PacketCodec<? super B, ? extends V> codec) {
            this.packetTypes.add(new PacketType<B, V, T>(codec, id));
            return this;
        }

        public PacketCodecDispatcher<B, V, T> build() {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            object2IntOpenHashMap.defaultReturnValue(-2);
            for (PacketType<B, V, T> packetType : this.packetTypes) {
                int i = object2IntOpenHashMap.size();
                int j = object2IntOpenHashMap.putIfAbsent(packetType.id, i);
                if (j == -2) continue;
                throw new IllegalStateException("Duplicate registration for type " + String.valueOf(packetType.id));
            }
            return new PacketCodecDispatcher<B, V, T>(this.packetIdGetter, List.copyOf(this.packetTypes), object2IntOpenHashMap);
        }
    }
}

