/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;

public interface ClientCookieRequestPacketListener
extends ClientPacketListener {
    public void onCookieRequest(CookieRequestS2CPacket var1);
}

