/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SequencedPacketCreator {
    public Packet<ServerPlayPacketListener> predict(int var1);
}

