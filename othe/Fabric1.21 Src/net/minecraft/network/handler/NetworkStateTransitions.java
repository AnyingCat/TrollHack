/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.NetworkState;
import net.minecraft.network.handler.DecoderHandler;
import net.minecraft.network.handler.EncoderHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

public class NetworkStateTransitions {
    public static <T extends PacketListener> DecoderTransitioner decoderTransitioner(NetworkState<T> newState) {
        return NetworkStateTransitions.decoderSwapper(new DecoderHandler<T>(newState));
    }

    private static DecoderTransitioner decoderSwapper(ChannelInboundHandler newDecoder) {
        return context -> {
            context.pipeline().replace(context.name(), "decoder", (ChannelHandler)newDecoder);
            context.channel().config().setAutoRead(true);
        };
    }

    public static <T extends PacketListener> EncoderTransitioner encoderTransitioner(NetworkState<T> newState) {
        return NetworkStateTransitions.encoderSwapper(new EncoderHandler<T>(newState));
    }

    private static EncoderTransitioner encoderSwapper(ChannelOutboundHandler newEncoder) {
        return context -> context.pipeline().replace(context.name(), "encoder", (ChannelHandler)newEncoder);
    }

    @FunctionalInterface
    public static interface DecoderTransitioner {
        public void run(ChannelHandlerContext var1);

        default public DecoderTransitioner andThen(DecoderTransitioner decoderTransitioner) {
            return context -> {
                this.run(context);
                decoderTransitioner.run(context);
            };
        }
    }

    @FunctionalInterface
    public static interface EncoderTransitioner {
        public void run(ChannelHandlerContext var1);

        default public EncoderTransitioner andThen(EncoderTransitioner encoderTransitioner) {
            return context -> {
                this.run(context);
                encoderTransitioner.run(context);
            };
        }
    }

    public static class OutboundConfigurer
    extends ChannelOutboundHandlerAdapter {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(ChannelHandlerContext context, Object received, ChannelPromise promise) throws Exception {
            if (received instanceof Packet) {
                ReferenceCountUtil.release(received);
                throw new EncoderException("Pipeline has no outbound protocol configured, can't process packet " + String.valueOf(received));
            }
            if (received instanceof EncoderTransitioner) {
                EncoderTransitioner encoderTransitioner = (EncoderTransitioner)received;
                try {
                    encoderTransitioner.run(context);
                } finally {
                    ReferenceCountUtil.release(received);
                }
                promise.setSuccess();
            } else {
                context.write(received, promise);
            }
        }
    }

    public static class InboundConfigurer
    extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext context, Object received) {
            if (received instanceof ByteBuf || received instanceof Packet) {
                ReferenceCountUtil.release(received);
                throw new DecoderException("Pipeline has no inbound protocol configured, can't process packet " + String.valueOf(received));
            }
            context.fireChannelRead(received);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void write(ChannelHandlerContext context, Object received, ChannelPromise promise) throws Exception {
            if (received instanceof DecoderTransitioner) {
                DecoderTransitioner decoderTransitioner = (DecoderTransitioner)received;
                try {
                    decoderTransitioner.run(context);
                } finally {
                    ReferenceCountUtil.release(received);
                }
                promise.setSuccess();
            } else {
                context.write(received, promise);
            }
        }
    }
}

