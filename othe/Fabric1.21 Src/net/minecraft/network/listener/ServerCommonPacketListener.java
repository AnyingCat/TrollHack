/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.listener.ServerCookieResponsePacketListener;
import net.minecraft.network.listener.ServerCrashSafePacketListener;
import net.minecraft.network.packet.c2s.common.ClientOptionsC2SPacket;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;

public interface ServerCommonPacketListener
extends ServerCookieResponsePacketListener,
ServerCrashSafePacketListener {
    public void onKeepAlive(KeepAliveC2SPacket var1);

    public void onPong(CommonPongC2SPacket var1);

    public void onCustomPayload(CustomPayloadC2SPacket var1);

    public void onResourcePackStatus(ResourcePackStatusC2SPacket var1);

    public void onClientOptions(ClientOptionsC2SPacket var1);
}

