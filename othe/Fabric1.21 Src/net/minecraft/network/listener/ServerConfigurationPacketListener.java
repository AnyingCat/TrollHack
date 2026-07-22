/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.config.SelectKnownPacksC2SPacket;

public interface ServerConfigurationPacketListener
extends ServerCommonPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.CONFIGURATION;
    }

    public void onReady(ReadyC2SPacket var1);

    public void onSelectKnownPacks(SelectKnownPacksC2SPacket var1);
}

