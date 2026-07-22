/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import org.jetbrains.annotations.Nullable;

public class EntityAttachS2CPacket
implements Packet<ClientPlayPacketListener> {
    public static final PacketCodec<PacketByteBuf, EntityAttachS2CPacket> CODEC = Packet.createCodec(EntityAttachS2CPacket::write, EntityAttachS2CPacket::new);
    private final int attachedEntityId;
    private final int holdingEntityId;

    public EntityAttachS2CPacket(Entity attachedEntity, @Nullable Entity holdingEntity) {
        this.attachedEntityId = attachedEntity.getId();
        this.holdingEntityId = holdingEntity != null ? holdingEntity.getId() : 0;
    }

    private EntityAttachS2CPacket(PacketByteBuf buf) {
        this.attachedEntityId = buf.readInt();
        this.holdingEntityId = buf.readInt();
    }

    private void write(PacketByteBuf buf) {
        buf.writeInt(this.attachedEntityId);
        buf.writeInt(this.holdingEntityId);
    }

    @Override
    public PacketType<EntityAttachS2CPacket> getPacketId() {
        return PlayPackets.SET_ENTITY_LINK;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onEntityAttach(this);
    }

    public int getAttachedEntityId() {
        return this.attachedEntityId;
    }

    public int getHoldingEntityId() {
        return this.holdingEntityId;
    }
}

