/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.BundleSplitterPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.jetbrains.annotations.Nullable;

public interface PacketBundleHandler {
    public static final int MAX_PACKETS = 4096;

    public static <T extends PacketListener, P extends BundlePacket<? super T>> PacketBundleHandler create(final PacketType<P> id, final Function<Iterable<Packet<? super T>>, P> bundleFunction, final BundleSplitterPacket<? super T> splitter) {
        return new PacketBundleHandler(){

            @Override
            public void forEachPacket(Packet<?> packet, Consumer<Packet<?>> consumer) {
                if (packet.getPacketId() == id) {
                    BundlePacket bundlePacket = (BundlePacket)packet;
                    consumer.accept(splitter);
                    bundlePacket.getPackets().forEach(consumer);
                    consumer.accept(splitter);
                } else {
                    consumer.accept(packet);
                }
            }

            @Override
            @Nullable
            public Bundler createBundler(Packet<?> splitter2) {
                if (splitter2 == splitter) {
                    return new Bundler(){
                        private final List<Packet<? super T>> packets = new ArrayList();

                        @Override
                        @Nullable
                        public Packet<?> add(Packet<?> packet) {
                            if (packet == splitter) {
                                return (Packet)bundleFunction.apply(this.packets);
                            }
                            Packet<?> packet2 = packet;
                            if (this.packets.size() >= 4096) {
                                throw new IllegalStateException("Too many packets in a bundle");
                            }
                            this.packets.add(packet2);
                            return null;
                        }
                    };
                }
                return null;
            }
        };
    }

    public void forEachPacket(Packet<?> var1, Consumer<Packet<?>> var2);

    @Nullable
    public Bundler createBundler(Packet<?> var1);

    public static interface Bundler {
        @Nullable
        public Packet<?> add(Packet<?> var1);
    }
}

