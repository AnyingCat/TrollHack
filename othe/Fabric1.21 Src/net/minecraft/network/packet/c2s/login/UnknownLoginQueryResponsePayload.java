/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;

public record UnknownLoginQueryResponsePayload() implements LoginQueryResponsePayload
{
    public static final UnknownLoginQueryResponsePayload INSTANCE = new UnknownLoginQueryResponsePayload();

    @Override
    public void write(PacketByteBuf buf) {
    }
}

