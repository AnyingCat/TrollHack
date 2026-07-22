/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.event;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

public class EntityPositionSource
implements PositionSource {
    public static final MapCodec<EntityPositionSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Uuids.INT_STREAM_CODEC.fieldOf("source_entity")).forGetter(EntityPositionSource::getUuid), ((MapCodec)Codec.FLOAT.fieldOf("y_offset")).orElse(Float.valueOf(0.0f)).forGetter(entityPositionSource -> Float.valueOf(entityPositionSource.yOffset))).apply((Applicative<EntityPositionSource, ?>)instance, (uuid, yOffset) -> new EntityPositionSource(Either.right(Either.left(uuid)), yOffset.floatValue())));
    public static final PacketCodec<ByteBuf, EntityPositionSource> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, EntityPositionSource::getEntityId, PacketCodecs.FLOAT, source -> Float.valueOf(source.yOffset), (entityId, yOffset) -> new EntityPositionSource(Either.right(Either.right(entityId)), yOffset.floatValue()));
    private Either<Entity, Either<UUID, Integer>> source;
    private final float yOffset;

    public EntityPositionSource(Entity entity, float yOffset) {
        this(Either.left(entity), yOffset);
    }

    private EntityPositionSource(Either<Entity, Either<UUID, Integer>> source, float yOffset) {
        this.source = source;
        this.yOffset = yOffset;
    }

    @Override
    public Optional<Vec3d> getPos(World world) {
        if (this.source.left().isEmpty()) {
            this.findEntityInWorld(world);
        }
        return this.source.left().map(entity -> entity.getPos().add(0.0, this.yOffset, 0.0));
    }

    private void findEntityInWorld(World world) {
        this.source.map(Optional::of, entityId -> Optional.ofNullable(entityId.map(uuid -> {
            Entity entity;
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                entity = serverWorld.getEntity((UUID)uuid);
            } else {
                entity = null;
            }
            return entity;
        }, world::getEntityById))).ifPresent(entity -> {
            this.source = Either.left(entity);
        });
    }

    private UUID getUuid() {
        return this.source.map(Entity::getUuid, entityId -> entityId.map(Function.identity(), entityIdx -> {
            throw new RuntimeException("Unable to get entityId from uuid");
        }));
    }

    private int getEntityId() {
        return this.source.map(Entity::getId, entityId -> entityId.map(uuid -> {
            throw new IllegalStateException("Unable to get entityId from uuid");
        }, Function.identity()));
    }

    public PositionSourceType<EntityPositionSource> getType() {
        return PositionSourceType.ENTITY;
    }

    public static class Type
    implements PositionSourceType<EntityPositionSource> {
        @Override
        public MapCodec<EntityPositionSource> getCodec() {
            return CODEC;
        }

        @Override
        public PacketCodec<ByteBuf, EntityPositionSource> getPacketCodec() {
            return PACKET_CODEC;
        }
    }
}

