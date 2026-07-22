/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.PacketListener;

public interface ClientPacketListener
extends PacketListener {
    @Override
    default public NetworkSide getSide() {
        return NetworkSide.CLIENTBOUND;
    }
}

