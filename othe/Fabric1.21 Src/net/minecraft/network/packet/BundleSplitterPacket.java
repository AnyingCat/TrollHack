/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;

public abstract class BundleSplitterPacket<T extends PacketListener>
implements Packet<T> {
    @Override
    public final void apply(T listener) {
        throw new AssertionError((Object)"This packet should be handled by pipeline");
    }

    @Override
    public abstract PacketType<? extends BundleSplitterPacket<T>> getPacketId();
}

