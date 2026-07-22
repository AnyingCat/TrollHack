/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

/**
 * Represents the position that an entity takes after being
 * {@linkplain net.minecraft.entity.Entity#teleportTo teleported}.
 */
public record TeleportTarget(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch, boolean missingRespawnBlock, PostDimensionTransition postDimensionTransition) {
    public static final PostDimensionTransition NO_OP = entity -> {};
    public static final PostDimensionTransition SEND_TRAVEL_THROUGH_PORTAL_PACKET = TeleportTarget::sendTravelThroughPortalPacket;
    public static final PostDimensionTransition ADD_PORTAL_CHUNK_TICKET = TeleportTarget::addPortalChunkTicket;

    public TeleportTarget(ServerWorld world, Vec3d pos, Vec3d velocity, float yaw, float pitch, PostDimensionTransition postDimensionTransition) {
        this(world, pos, velocity, yaw, pitch, false, postDimensionTransition);
    }

    public TeleportTarget(ServerWorld world, Entity entity, PostDimensionTransition postDimensionTransition) {
        this(world, TeleportTarget.getWorldSpawnPos(world, entity), Vec3d.ZERO, 0.0f, 0.0f, false, postDimensionTransition);
    }

    private static void sendTravelThroughPortalPacket(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.networkHandler.sendPacket(new WorldEventS2CPacket(WorldEvents.TRAVEL_THROUGH_PORTAL, BlockPos.ORIGIN, 0, false));
        }
    }

    private static void addPortalChunkTicket(Entity entity) {
        entity.addPortalChunkTicketAt(BlockPos.ofFloored(entity.getPos()));
    }

    public static TeleportTarget missingSpawnBlock(ServerWorld world, Entity entity, PostDimensionTransition postDimensionTransition) {
        return new TeleportTarget(world, TeleportTarget.getWorldSpawnPos(world, entity), Vec3d.ZERO, 0.0f, 0.0f, true, postDimensionTransition);
    }

    private static Vec3d getWorldSpawnPos(ServerWorld world, Entity entity) {
        return entity.getWorldSpawnPos(world, world.getSpawnPos()).toBottomCenterPos();
    }

    @FunctionalInterface
    public static interface PostDimensionTransition {
        public void onTransition(Entity var1);

        default public PostDimensionTransition then(PostDimensionTransition next) {
            return entity -> {
                this.onTransition(entity);
                next.onTransition(entity);
            };
        }
    }
}

