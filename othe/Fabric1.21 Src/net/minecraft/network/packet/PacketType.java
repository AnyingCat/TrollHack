/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public record PacketType<T extends Packet<?>>(NetworkSide side, Identifier id) {
    @Override
    public String toString() {
        return this.side.getName() + "/" + String.valueOf(this.id);
    }
}

