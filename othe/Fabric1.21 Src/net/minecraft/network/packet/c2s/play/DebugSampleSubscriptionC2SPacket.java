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
import net.minecraft.util.profiler.log.DebugSampleType;

public record DebugSampleSubscriptionC2SPacket(DebugSampleType sampleType) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, DebugSampleSubscriptionC2SPacket> CODEC = Packet.createCodec(DebugSampleSubscriptionC2SPacket::write, DebugSampleSubscriptionC2SPacket::new);

    private DebugSampleSubscriptionC2SPacket(PacketByteBuf buf) {
        this(buf.readEnumConstant(DebugSampleType.class));
    }

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.sampleType);
    }

    @Override
    public PacketType<DebugSampleSubscriptionC2SPacket> getPacketId() {
        return PlayPackets.DEBUG_SAMPLE_SUBSCRIPTION;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onDebugSampleSubscription(this);
    }
}

