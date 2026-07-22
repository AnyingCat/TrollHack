/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.handler.NetworkStateTransitions;
import net.minecraft.network.packet.Packet;

public interface NetworkStateTransitionHandler {
    public static void onDecoded(ChannelHandlerContext context, Packet<?> packet) {
        if (packet.transitionsNetworkState()) {
            context.channel().config().setAutoRead(false);
            context.pipeline().addBefore(context.name(), "inbound_config", new NetworkStateTransitions.InboundConfigurer());
            context.pipeline().remove(context.name());
        }
    }

    public static void onEncoded(ChannelHandlerContext context, Packet<?> packet) {
        if (packet.transitionsNetworkState()) {
            context.pipeline().addAfter(context.name(), "outbound_config", new NetworkStateTransitions.OutboundConfigurer());
            context.pipeline().remove(context.name());
        }
    }
}

