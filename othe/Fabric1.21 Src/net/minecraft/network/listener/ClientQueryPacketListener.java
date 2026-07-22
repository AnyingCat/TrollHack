/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.ClientPingResultPacketListener;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;

public interface ClientQueryPacketListener
extends ClientPingResultPacketListener,
ClientPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.STATUS;
    }

    public void onResponse(QueryResponseS2CPacket var1);
}

