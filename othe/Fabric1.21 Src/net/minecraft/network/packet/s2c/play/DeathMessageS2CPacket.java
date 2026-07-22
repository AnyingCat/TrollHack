/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record DeathMessageS2CPacket(int playerId, Text message) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, DeathMessageS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, DeathMessageS2CPacket::playerId, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC, DeathMessageS2CPacket::message, DeathMessageS2CPacket::new);

    @Override
    public PacketType<DeathMessageS2CPacket> getPacketId() {
        return PlayPackets.PLAYER_COMBAT_KILL;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onDeathMessage(this);
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }
}

