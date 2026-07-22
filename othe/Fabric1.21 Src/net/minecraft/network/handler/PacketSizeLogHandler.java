/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.handler.PacketSizeLogger;

public class PacketSizeLogHandler
extends ChannelInboundHandlerAdapter {
    private final PacketSizeLogger logger;

    public PacketSizeLogHandler(PacketSizeLogger logger) {
        this.logger = logger;
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object value) {
        if (value instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf)value;
            this.logger.increment(byteBuf.readableBytes());
        }
        context.fireChannelRead(value);
    }
}

