/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public record UnknownLoginQueryRequestPayload(Identifier id) implements LoginQueryRequestPayload
{
    @Override
    public void write(PacketByteBuf buf) {
    }
}

