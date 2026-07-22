/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.PacketType;

public interface Packet<T extends PacketListener> {
    public PacketType<? extends Packet<T>> getPacketId();

    public void apply(T var1);

    /**
     * {@return whether a throwable in writing of this packet allows the
     * connection to simply skip the packet's sending than disconnecting}
     */
    default public boolean isWritingErrorSkippable() {
        return false;
    }

    /**
     * {@return {@code true} if the packet signals transitioning between {@link
     * net.minecraft.network.NetworkState}s}
     * 
     * <p>Such packets cannot be {@linkplain net.minecraft.network.packet.BundlePacket bundled}.
     */
    default public boolean transitionsNetworkState() {
        return false;
    }

    public static <B extends ByteBuf, T extends Packet<?>> PacketCodec<B, T> createCodec(ValueFirstEncoder<B, T> encoder, PacketDecoder<B, T> decoder) {
        return PacketCodec.of(encoder, decoder);
    }
}

