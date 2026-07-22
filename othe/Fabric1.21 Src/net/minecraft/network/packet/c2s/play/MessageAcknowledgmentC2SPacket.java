/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record MessageAcknowledgmentC2SPacket(int offset) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, MessageAcknowledgmentC2SPacket> CODEC = Packet.createCodec(MessageAcknowledgmentC2SPacket::write, MessageAcknowledgmentC2SPacket::new);

    private MessageAcknowledgmentC2SPacket(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.offset);
    }

    @Override
    public PacketType<MessageAcknowledgmentC2SPacket> getPacketId() {
        return PlayPackets.CHAT_ACK;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onMessageAcknowledgment(this);
    }
}

