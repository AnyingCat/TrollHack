/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.query;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerQueryPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.StatusPackets;

public class QueryRequestC2SPacket
implements Packet<ServerQueryPacketListener> {
    public static final QueryRequestC2SPacket INSTANCE = new QueryRequestC2SPacket();
    public static final PacketCodec<ByteBuf, QueryRequestC2SPacket> CODEC = PacketCodec.unit(INSTANCE);

    private QueryRequestC2SPacket() {
    }

    @Override
    public PacketType<QueryRequestC2SPacket> getPacketId() {
        return StatusPackets.STATUS_REQUEST;
    }

    @Override
    public void apply(ServerQueryPacketListener serverQueryPacketListener) {
        serverQueryPacketListener.onRequest(this);
    }
}

