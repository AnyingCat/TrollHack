/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.BitSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class LightUpdateS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, LightUpdateS2CPacket> CODEC = Packet.createCodec(LightUpdateS2CPacket::write, LightUpdateS2CPacket::new);
    private final int chunkX;
    private final int chunkZ;
    private final LightData data;

    public LightUpdateS2CPacket(ChunkPos chunkPos, LightingProvider lightProvider, @Nullable BitSet skyBits, @Nullable BitSet blockBits) {
        this.chunkX = chunkPos.x;
        this.chunkZ = chunkPos.z;
        this.data = new LightData(chunkPos, lightProvider, skyBits, blockBits);
    }

    private LightUpdateS2CPacket(PacketByteBuf buf) {
        this.chunkX = buf.readVarInt();
        this.chunkZ = buf.readVarInt();
        this.data = new LightData(buf, this.chunkX, this.chunkZ);
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.chunkX);
        buf.writeVarInt(this.chunkZ);
        this.data.write(buf);
    }

    @Override
    public PacketType<LightUpdateS2CPacket> getPacketId() {
        return PlayPackets.LIGHT_UPDATE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onLightUpdate(this);
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public LightData getData() {
        return this.data;
    }
}

