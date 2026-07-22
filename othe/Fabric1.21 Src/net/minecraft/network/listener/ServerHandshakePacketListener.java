/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;

public interface ServerHandshakePacketListener
extends ServerCrashSafePacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.HANDSHAKING;
    }

    public void onHandshake(HandshakeC2SPacket var1);
}

