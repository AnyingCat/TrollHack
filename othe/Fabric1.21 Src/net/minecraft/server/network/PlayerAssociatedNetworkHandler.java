/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerAssociatedNetworkHandler {
    public ServerPlayerEntity getPlayer();

    public void sendPacket(Packet<?> var1);
}

