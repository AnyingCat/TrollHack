/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.packet.Packet;

public class PacketUnbundler
extends MessageToMessageEncoder<Packet<?>> {
    private final PacketBundleHandler bundleHandler;

    public PacketUnbundler(PacketBundleHandler bundleHandler) {
        this.bundleHandler = bundleHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<?> packet, List<Object> list) throws Exception {
        this.bundleHandler.forEachPacket(packet, list::add);
        if (packet.transitionsNetworkState()) {
            channelHandlerContext.pipeline().remove(channelHandlerContext.name());
        }
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext context, Object packet, List packets) throws Exception {
        this.encode(context, (Packet)packet, (List<Object>)packets);
    }
}

