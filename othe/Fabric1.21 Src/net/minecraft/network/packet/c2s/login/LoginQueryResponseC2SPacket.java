/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.c2s.login;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.network.packet.c2s.login.UnknownLoginQueryResponsePayload;
import org.jetbrains.annotations.Nullable;

public record LoginQueryResponseC2SPacket(int queryId, @Nullable LoginQueryResponsePayload response) implements Packet<ServerLoginPacketListener>
{
    public static final PacketCodec<PacketByteBuf, LoginQueryResponseC2SPacket> CODEC = Packet.createCodec(LoginQueryResponseC2SPacket::write, LoginQueryResponseC2SPacket::read);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private static LoginQueryResponseC2SPacket read(PacketByteBuf buf) {
        int i = buf.readVarInt();
        return new LoginQueryResponseC2SPacket(i, LoginQueryResponseC2SPacket.readPayload(i, buf));
    }

    /**
     * {@return the response payload read from {@code buf}}
     * 
     * @implNote This delegates the logic to {@link #getVanillaPayload},
     * which simply validates the size of the buffer and returns {@link
     * UnknownLoginQueryResponsePayload#INSTANCE}.
     */
    private static LoginQueryResponsePayload readPayload(int queryId, PacketByteBuf buf) {
        return LoginQueryResponseC2SPacket.getVanillaPayload(buf);
    }

    private static LoginQueryResponsePayload getVanillaPayload(PacketByteBuf buf) {
        int i = buf.readableBytes();
        if (i < 0 || i > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        buf.skipBytes(i);
        return UnknownLoginQueryResponsePayload.INSTANCE;
    }

    private void write(PacketByteBuf buf2) {
        buf2.writeVarInt(this.queryId);
        buf2.writeNullable(this.response, (buf, response) -> response.write((PacketByteBuf)buf));
    }

    @Override
    public PacketType<LoginQueryResponseC2SPacket> getPacketId() {
        return LoginPackets.CUSTOM_QUERY_ANSWER;
    }

    @Override
    public void apply(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.onQueryResponse(this);
    }

    @Nullable
    public LoginQueryResponsePayload response() {
        return this.response;
    }
}

