/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record EntityDamageS2CPacket(int entityId, RegistryEntry<DamageType> sourceType, int sourceCauseId, int sourceDirectId, Optional<Vec3d> sourcePosition) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, EntityDamageS2CPacket> CODEC = Packet.createCodec(EntityDamageS2CPacket::write, EntityDamageS2CPacket::new);

    public EntityDamageS2CPacket(Entity entity, DamageSource damageSource) {
        this(entity.getId(), damageSource.getTypeRegistryEntry(), damageSource.getAttacker() != null ? damageSource.getAttacker().getId() : -1, damageSource.getSource() != null ? damageSource.getSource().getId() : -1, Optional.ofNullable(damageSource.getStoredPosition()));
    }

    private EntityDamageS2CPacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), (RegistryEntry)DamageType.ENTRY_PACKET_CODEC.decode(buf), EntityDamageS2CPacket.readOffsetVarInt(buf), EntityDamageS2CPacket.readOffsetVarInt(buf), buf.readOptional(pos -> new Vec3d(pos.readDouble(), pos.readDouble(), pos.readDouble())));
    }

    private static void writeOffsetVarInt(PacketByteBuf buf, int value) {
        buf.writeVarInt(value + 1);
    }

    private static int readOffsetVarInt(PacketByteBuf buf) {
        return buf.readVarInt() - 1;
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.entityId);
        DamageType.ENTRY_PACKET_CODEC.encode(buf, this.sourceType);
        EntityDamageS2CPacket.writeOffsetVarInt(buf, this.sourceCauseId);
        EntityDamageS2CPacket.writeOffsetVarInt(buf, this.sourceDirectId);
        buf.writeOptional(this.sourcePosition, (bufx, pos) -> {
            bufx.writeDouble(pos.getX());
            bufx.writeDouble(pos.getY());
            bufx.writeDouble(pos.getZ());
        });
    }

    @Override
    public PacketType<EntityDamageS2CPacket> getPacketId() {
        return PlayPackets.DAMAGE_EVENT;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onEntityDamage(this);
    }

    public DamageSource createDamageSource(World world) {
        if (this.sourcePosition.isPresent()) {
            return new DamageSource(this.sourceType, this.sourcePosition.get());
        }
        Entity entity = world.getEntityById(this.sourceCauseId);
        Entity entity2 = world.getEntityById(this.sourceDirectId);
        return new DamageSource(this.sourceType, entity2, entity);
    }
}

