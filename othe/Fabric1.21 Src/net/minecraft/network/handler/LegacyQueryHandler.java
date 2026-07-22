/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.network.QueryableServer;
import net.minecraft.network.handler.LegacyQueries;
import org.slf4j.Logger;

public class LegacyQueryHandler
extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final QueryableServer server;

    public LegacyQueryHandler(QueryableServer server) {
        this.server = server;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf)msg;
        byteBuf.markReaderIndex();
        boolean bl = true;
        try {
            if (byteBuf.readUnsignedByte() != 254) {
                return;
            }
            SocketAddress socketAddress = ctx.channel().remoteAddress();
            int i = byteBuf.readableBytes();
            if (i == 0) {
                LOGGER.debug("Ping: (<1.3.x) from {}", (Object)socketAddress);
                String string = LegacyQueryHandler.getResponseFor1_2(this.server);
                LegacyQueryHandler.reply(ctx, LegacyQueryHandler.createBuf(ctx.alloc(), string));
            } else {
                if (byteBuf.readUnsignedByte() != 1) {
                    return;
                }
                if (byteBuf.isReadable()) {
                    if (!LegacyQueryHandler.isLegacyQuery(byteBuf)) {
                        return;
                    }
                    LOGGER.debug("Ping: (1.6) from {}", (Object)socketAddress);
                } else {
                    LOGGER.debug("Ping: (1.4-1.5.x) from {}", (Object)socketAddress);
                }
                String string = LegacyQueryHandler.getResponse(this.server);
                LegacyQueryHandler.reply(ctx, LegacyQueryHandler.createBuf(ctx.alloc(), string));
            }
            byteBuf.release();
            bl = false;
        } catch (RuntimeException runtimeException) {
        } finally {
            if (bl) {
                byteBuf.resetReaderIndex();
                ctx.channel().pipeline().remove(this);
                ctx.fireChannelRead(msg);
            }
        }
    }

    private static boolean isLegacyQuery(ByteBuf buf) {
        short s = buf.readUnsignedByte();
        if (s != 250) {
            return false;
        }
        String string = LegacyQueries.read(buf);
        if (!"MC|PingHost".equals(string)) {
            return false;
        }
        int i = buf.readUnsignedShort();
        if (buf.readableBytes() != i) {
            return false;
        }
        short t = buf.readUnsignedByte();
        if (t < 73) {
            return false;
        }
        String string2 = LegacyQueries.read(buf);
        int j = buf.readInt();
        return j <= 65535;
    }

    private static String getResponseFor1_2(QueryableServer server) {
        return String.format(Locale.ROOT, "%s\u00a7%d\u00a7%d", server.getServerMotd(), server.getCurrentPlayerCount(), server.getMaxPlayerCount());
    }

    private static String getResponse(QueryableServer server) {
        return String.format(Locale.ROOT, "\u00a71\u0000%d\u0000%s\u0000%s\u0000%d\u0000%d", 127, server.getVersion(), server.getServerMotd(), server.getCurrentPlayerCount(), server.getMaxPlayerCount());
    }

    private static void reply(ChannelHandlerContext context, ByteBuf buf) {
        context.pipeline().firstContext().writeAndFlush(buf).addListener(ChannelFutureListener.CLOSE);
    }

    private static ByteBuf createBuf(ByteBufAllocator allocator, String string) {
        ByteBuf byteBuf = allocator.buffer();
        byteBuf.writeByte(255);
        LegacyQueries.write(byteBuf, string);
        return byteBuf;
    }
}

