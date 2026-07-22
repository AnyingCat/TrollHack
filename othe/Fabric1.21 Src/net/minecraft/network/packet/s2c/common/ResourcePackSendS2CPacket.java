/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.common;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Uuids;

public record ResourcePackSendS2CPacket(UUID id, String url, String hash, boolean required, Optional<Text> prompt) implements Packet<ClientCommonPacketListener>
{
    public static final int MAX_HASH_LENGTH = 40;
    public static final PacketCodec<ByteBuf, ResourcePackSendS2CPacket> CODEC = PacketCodec.tuple(Uuids.PACKET_CODEC, ResourcePackSendS2CPacket::id, PacketCodecs.STRING, ResourcePackSendS2CPacket::url, PacketCodecs.string(40), ResourcePackSendS2CPacket::hash, PacketCodecs.BOOL, ResourcePackSendS2CPacket::required, TextCodecs.PACKET_CODEC.collect(PacketCodecs::optional), ResourcePackSendS2CPacket::prompt, ResourcePackSendS2CPacket::new);

    public ResourcePackSendS2CPacket {
        if (string2.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + string2.length() + ")");
        }
    }

    @Override
    public PacketType<ResourcePackSendS2CPacket> getPacketId() {
        return CommonPackets.RESOURCE_PACK_PUSH;
    }

    @Override
    public void apply(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.onResourcePackSend(this);
    }
}

