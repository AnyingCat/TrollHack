/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class AcknowledgeReconfigurationC2SPacket
implements Packet<ServerPlayPacketListener> {
    public static final AcknowledgeReconfigurationC2SPacket INSTANCE = new AcknowledgeReconfigurationC2SPacket();
    public static final PacketCodec<ByteBuf, AcknowledgeReconfigurationC2SPacket> CODEC = PacketCodec.unit(INSTANCE);

    private AcknowledgeReconfigurationC2SPacket() {
    }

    @Override
    public PacketType<AcknowledgeReconfigurationC2SPacket> getPacketId() {
        return PlayPackets.CONFIGURATION_ACKNOWLEDGED;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onAcknowledgeReconfiguration(this);
    }

    @Override
    public boolean transitionsNetworkState() {
        return true;
    }
}

