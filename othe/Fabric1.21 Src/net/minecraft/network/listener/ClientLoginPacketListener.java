/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.listener;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.listener.ClientCookieRequestPacketListener;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.packet.s2c.login.LoginCompressionS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginHelloS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;

public interface ClientLoginPacketListener
extends ClientCookieRequestPacketListener,
ClientPacketListener {
    @Override
    default public NetworkPhase getPhase() {
        return NetworkPhase.LOGIN;
    }

    public void onHello(LoginHelloS2CPacket var1);

    public void onSuccess(LoginSuccessS2CPacket var1);

    public void onDisconnect(LoginDisconnectS2CPacket var1);

    public void onCompression(LoginCompressionS2CPacket var1);

    public void onQueryRequest(LoginQueryRequestS2CPacket var1);
}

