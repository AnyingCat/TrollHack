/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

public class PacketBundler
extends MessageToMessageDecoder<Packet<?>> {
    private final PacketBundleHandler handler;
    @Nullable
    private PacketBundleHandler.Bundler currentBundler;

    public PacketBundler(PacketBundleHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        if (this.currentBundler != null) {
            PacketBundler.ensureNotTransitioning(packet);
            Packet<?> packet2 = this.currentBundler.add(packet);
            if (packet2 != null) {
                this.currentBundler = null;
                list.add(packet2);
            }
        } else {
            PacketBundleHandler.Bundler bundler = this.handler.createBundler(packet);
            if (bundler != null) {
                PacketBundler.ensureNotTransitioning(packet);
                this.currentBundler = bundler;
            } else {
                list.add(packet);
                if (packet.transitionsNetworkState()) {
                    channelHandlerContext.pipeline().remove(channelHandlerContext.name());
                }
            }
        }
    }

    private static void ensureNotTransitioning(Packet<?> packet) {
        if (packet.transitionsNetworkState()) {
            throw new DecoderException("Terminal message received in bundle");
        }
    }

    @Override
    protected /* synthetic */ void decode(ChannelHandlerContext context, Object packet, List packets) throws Exception {
        this.decode(context, (Packet)packet, (List<Object>)packets);
    }
}

