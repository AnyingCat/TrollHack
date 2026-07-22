/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.PacketListener;

public interface TickablePacketListener
extends PacketListener {
    /**
     * Ticks this packet listener on the game engine thread.  The listener is responsible
     * for synchronizing between the game engine and netty event loop threads.
     */
    public void tick();
}

