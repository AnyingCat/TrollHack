/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.spawner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public interface EntityDetector {
    public static final EntityDetector SURVIVAL_PLAYERS = (world, selector, center, radius, spawner) -> selector.getPlayers(world, player -> player.getBlockPos().isWithinDistance(center, radius) && !player.isCreative() && !player.isSpectator()).stream().filter(entity -> !spawner || EntityDetector.hasLineOfSight(world, center.toCenterPos(), entity.getEyePos())).map(Entity::getUuid).toList();
    public static final EntityDetector NON_SPECTATOR_PLAYERS = (world, selector, center, radius, spawner) -> selector.getPlayers(world, player -> player.getBlockPos().isWithinDistance(center, radius) && !player.isSpectator()).stream().filter(entity -> !spawner || EntityDetector.hasLineOfSight(world, center.toCenterPos(), entity.getEyePos())).map(Entity::getUuid).toList();
    public static final EntityDetector SHEEP = (world, selector, center, radius, spawner) -> {
        Box box = new Box(center).expand(radius);
        return selector.getEntities(world, EntityType.SHEEP, box, LivingEntity::isAlive).stream().filter(entity -> !spawner || EntityDetector.hasLineOfSight(world, center.toCenterPos(), entity.getEyePos())).map(Entity::getUuid).toList();
    };

    public List<UUID> detect(ServerWorld var1, Selector var2, BlockPos var3, double var4, boolean var6);

    private static boolean hasLineOfSight(World world, Vec3d pos, Vec3d entityEyePos) {
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(entityEyePos, pos, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, ShapeContext.absent()));
        return blockHitResult.getBlockPos().equals(BlockPos.ofFloored(pos)) || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static interface Selector {
        public static final Selector IN_WORLD = new Selector(){

            public List<ServerPlayerEntity> getPlayers(ServerWorld world, Predicate<? super PlayerEntity> predicate) {
                return world.getPlayers(predicate);
            }

            @Override
            public <T extends Entity> List<T> getEntities(ServerWorld world, TypeFilter<Entity, T> typeFilter, Box box, Predicate<? super T> predicate) {
                return world.getEntitiesByType(typeFilter, box, predicate);
            }
        };

        public List<? extends PlayerEntity> getPlayers(ServerWorld var1, Predicate<? super PlayerEntity> var2);

        public <T extends Entity> List<T> getEntities(ServerWorld var1, TypeFilter<Entity, T> var2, Box var3, Predicate<? super T> var4);

        public static Selector ofPlayer(PlayerEntity player) {
            return Selector.ofPlayers(List.of(player));
        }

        public static Selector ofPlayers(final List<PlayerEntity> players) {
            return new Selector(){

                public List<PlayerEntity> getPlayers(ServerWorld world, Predicate<? super PlayerEntity> predicate) {
                    return players.stream().filter(predicate).toList();
                }

                @Override
                public <T extends Entity> List<T> getEntities(ServerWorld world, TypeFilter<Entity, T> typeFilter, Box box, Predicate<? super T> predicate) {
                    return players.stream().map(typeFilter::downcast).filter(Objects::nonNull).filter(predicate).toList();
                }
            };
        }
    }
}

