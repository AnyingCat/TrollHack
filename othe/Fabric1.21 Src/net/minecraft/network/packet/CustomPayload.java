/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.util.Identifier;

public interface CustomPayload {
    public Id<? extends CustomPayload> getId();

    public static <B extends ByteBuf, T extends CustomPayload> PacketCodec<B, T> codecOf(ValueFirstEncoder<B, T> encoder, PacketDecoder<B, T> decoder) {
        return PacketCodec.of(encoder, decoder);
    }

    public static <T extends CustomPayload> Id<T> id(String id) {
        return new Id(Identifier.ofVanilla(id));
    }

    public static <B extends PacketByteBuf> PacketCodec<B, CustomPayload> createCodec(final CodecFactory<B> unknownCodecFactory, List<Type<? super B, ?>> types) {
        final Map<Identifier, PacketCodec> map = types.stream().collect(Collectors.toUnmodifiableMap(type -> type.id().id(), Type::codec));
        return new PacketCodec<B, CustomPayload>(){

            private PacketCodec<? super B, ? extends CustomPayload> getCodec(Identifier id) {
                PacketCodec packetCodec = (PacketCodec)map.get(id);
                if (packetCodec != null) {
                    return packetCodec;
                }
                return unknownCodecFactory.create(id);
            }

            private <T extends CustomPayload> void encode(B value, Id<T> id, CustomPayload payload) {
                ((PacketByteBuf)value).writeIdentifier(id.id());
                PacketCodec packetCodec = this.getCodec(id.id);
                packetCodec.encode(value, payload);
            }

            @Override
            public void encode(B packetByteBuf, CustomPayload customPayload) {
                this.encode(packetByteBuf, customPayload.getId(), customPayload);
            }

            @Override
            public CustomPayload decode(B packetByteBuf) {
                Identifier identifier = ((PacketByteBuf)packetByteBuf).readIdentifier();
                return (CustomPayload)this.getCodec(identifier).decode(packetByteBuf);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((Object)((PacketByteBuf)object), (CustomPayload)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((B)((PacketByteBuf)object));
            }
        };
    }

    public record Id<T extends CustomPayload>(Identifier id) {
    }

    public static interface CodecFactory<B extends PacketByteBuf> {
        public PacketCodec<B, ? extends CustomPayload> create(Identifier var1);
    }

    public record Type<B extends PacketByteBuf, T extends CustomPayload>(Id<T> id, PacketCodec<B, T> codec) {
    }
}

