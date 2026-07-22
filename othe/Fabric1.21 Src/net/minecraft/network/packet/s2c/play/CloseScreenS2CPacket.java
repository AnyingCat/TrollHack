/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public class CloseScreenS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, CloseScreenS2CPacket> CODEC = Packet.createCodec(CloseScreenS2CPacket::write, CloseScreenS2CPacket::new);
    private final int syncId;

    public CloseScreenS2CPacket(int syncId) {
        this.syncId = syncId;
    }

    private CloseScreenS2CPacket(PacketByteBuf buf) {
        this.syncId = buf.readUnsignedByte();
    }

    private void write(PacketByteBuf buf) {
        buf.writeByte(this.syncId);
    }

    @Override
    public PacketType<CloseScreenS2CPacket> getPacketId() {
        return PlayPackets.CONTAINER_CLOSE_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onCloseScreen(this);
    }

    public int getSyncId() {
        return this.syncId;
    }
}

