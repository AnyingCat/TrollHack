/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record PlayerSessionC2SPacket(PublicPlayerSession.Serialized chatSession) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PlayerSessionC2SPacket> CODEC = Packet.createCodec(PlayerSessionC2SPacket::write, PlayerSessionC2SPacket::new);

    private PlayerSessionC2SPacket(PacketByteBuf buf) {
        this(PublicPlayerSession.Serialized.fromBuf(buf));
    }

    private void write(PacketByteBuf buf) {
        PublicPlayerSession.Serialized.write(buf, this.chatSession);
    }

    @Override
    public PacketType<PlayerSessionC2SPacket> getPacketId() {
        return PlayPackets.CHAT_SESSION_UPDATE;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onPlayerSession(this);
    }
}

