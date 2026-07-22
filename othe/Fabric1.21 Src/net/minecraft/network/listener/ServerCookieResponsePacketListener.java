/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;

public interface ServerCookieResponsePacketListener
extends ServerCrashSafePacketListener {
    public void onCookieResponse(CookieResponseC2SPacket var1);
}

