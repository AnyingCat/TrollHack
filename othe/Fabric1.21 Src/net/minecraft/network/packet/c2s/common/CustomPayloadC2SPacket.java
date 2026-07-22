/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.common;

import com.google.common.collect.Lists;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public record CustomPayloadC2SPacket(CustomPayload payload) implements Packet<ServerCommonPacketListener>
{
    private static final int MAX_PAYLOAD_SIZE = Short.MAX_VALUE;
    public static final PacketCodec<PacketByteBuf, CustomPayloadC2SPacket> CODEC = CustomPayload.createCodec((Identifier id) -> UnknownCustomPayload.createCodec(id, Short.MAX_VALUE), Util.make(Lists.newArrayList(new CustomPayload.Type<PacketByteBuf, BrandCustomPayload>(BrandCustomPayload.ID, BrandCustomPayload.CODEC)), types -> {})).xmap(CustomPayloadC2SPacket::new, CustomPayloadC2SPacket::payload);

    @Override
    public PacketType<CustomPayloadC2SPacket> getPacketId() {
        return CommonPackets.CUSTOM_PAYLOAD_C2S;
    }

    @Override
    public void apply(ServerCommonPacketListener serverCommonPacketListener) {
        serverCommonPacketListener.onCustomPayload(this);
    }
}

