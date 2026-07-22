/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class BundleS2CPacket
extends BundlePacket<ClientPlayPacketListener> {
    public BundleS2CPacket(Iterable<Packet<? super ClientPlayPacketListener>> iterable) {
        super(iterable);
    }

    @Override
    public PacketType<BundleS2CPacket> getPacketId() {
        return PlayPackets.BUNDLE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onBundle(this);
    }
}

