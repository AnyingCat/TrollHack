/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.listener.ServerQueryPingPacketListener;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;

public interface ServerQueryPacketListener
extends ServerCrashSafePacketListener,
ServerQueryPingPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.STATUS;
    }

    public void onRequest(QueryRequestC2SPacket var1);
}

