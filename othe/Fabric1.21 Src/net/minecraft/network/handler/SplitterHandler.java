/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.handler.PacketSizeLogger;
import org.jetbrains.annotations.Nullable;

public class SplitterHandler
extends ByteToMessageDecoder {
    private static final int LENGTH_BYTES = 3;
    private final ByteBuf reusableBuf = Unpooled.directBuffer(3);
    @Nullable
    private final PacketSizeLogger packetSizeLogger;

    public SplitterHandler(@Nullable PacketSizeLogger packetSizeLogger) {
        this.packetSizeLogger = packetSizeLogger;
    }

    @Override
    protected void handlerRemoved0(ChannelHandlerContext context) {
        this.reusableBuf.release();
    }

    private static boolean shouldSplit(ByteBuf source, ByteBuf sizeBuf) {
        for (int i = 0; i < 3; ++i) {
            if (!source.isReadable()) {
                return false;
            }
            byte b = source.readByte();
            sizeBuf.writeByte(b);
            if (VarInts.shouldContinueRead(b)) continue;
            return true;
        }
        throw new CorruptedFrameException("length wider than 21-bit");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> bytes) {
        buf.markReaderIndex();
        this.reusableBuf.clear();
        if (!SplitterHandler.shouldSplit(buf, this.reusableBuf)) {
            buf.resetReaderIndex();
            return;
        }
        int i = VarInts.read(this.reusableBuf);
        if (buf.readableBytes() < i) {
            buf.resetReaderIndex();
            return;
        }
        if (this.packetSizeLogger != null) {
            this.packetSizeLogger.increment(i + VarInts.getSizeInBytes(i));
        }
        bytes.add(buf.readBytes(i));
    }
}

