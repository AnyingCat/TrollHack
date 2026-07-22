/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.handler.NetworkStateTransitionHandler;
import net.minecraft.network.handler.PacketEncoderException;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import org.slf4j.Logger;

public class EncoderHandler<T extends PacketListener>
extends MessageToByteEncoder<Packet<T>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final NetworkState<T> state;

    public EncoderHandler(NetworkState<T> state) {
        this.state = state;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet<T> packet, ByteBuf byteBuf) throws Exception {
        PacketType<Packet<T>> packetType = packet.getPacketId();
        try {
            this.state.codec().encode(byteBuf, packet);
            int i = byteBuf.readableBytes();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ClientConnection.PACKET_SENT_MARKER, "OUT: [{}:{}] {} -> {} bytes", this.state.id().getId(), packetType, packet.getClass().getName(), i);
            }
            FlightProfiler.INSTANCE.onPacketSent(this.state.id(), packetType, channelHandlerContext.channel().remoteAddress(), i);
        } catch (Throwable throwable) {
            LOGGER.error("Error sending packet {}", (Object)packetType, (Object)throwable);
            if (packet.isWritingErrorSkippable()) {
                throw new PacketEncoderException(throwable);
            }
            throw throwable;
        } finally {
            NetworkStateTransitionHandler.onEncoded(channelHandlerContext, packet);
        }
    }

    @Override
    protected /* synthetic */ void encode(ChannelHandlerContext context, Object packet, ByteBuf out) throws Exception {
        this.encode(context, (Packet)packet, out);
    }
}

