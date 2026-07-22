/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class FleeTask<E extends PathAwareEntity>
extends MultiTickTask<E> {
    private static final int MIN_RUN_TIME = 100;
    private static final int MAX_RUN_TIME = 120;
    private static final int HORIZONTAL_RANGE = 5;
    private static final int VERTICAL_RANGE = 4;
    private final float speed;
    private final Function<PathAwareEntity, TagKey<DamageType>> entityToDangerousDamageTypes;

    public FleeTask(float speed) {
        this(speed, entity -> DamageTypeTags.PANIC_CAUSES);
    }

    public FleeTask(float speed, Function<PathAwareEntity, TagKey<DamageType>> entityToDangerousDamageTypes) {
        super(Map.of(MemoryModuleType.IS_PANICKING, MemoryModuleState.REGISTERED, MemoryModuleType.HURT_BY, MemoryModuleState.REGISTERED), 100, 120);
        this.speed = speed;
        this.entityToDangerousDamageTypes = entityToDangerousDamageTypes;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, E pathAwareEntity) {
        return ((LivingEntity)pathAwareEntity).getBrain().getOptionalRegisteredMemory(MemoryModuleType.HURT_BY).map(hurtBy -> hurtBy.isIn(this.entityToDangerousDamageTypes.apply((PathAwareEntity)pathAwareEntity))).orElse(false) != false || ((LivingEntity)pathAwareEntity).getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, E pathAwareEntity, long l) {
        return true;
    }

    @Override
    protected void run(ServerWorld serverWorld, E pathAwareEntity, long l) {
        ((LivingEntity)pathAwareEntity).getBrain().remember(MemoryModuleType.IS_PANICKING, true);
        ((LivingEntity)pathAwareEntity).getBrain().forget(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, E pathAwareEntity, long l) {
        Brain<?> brain = ((LivingEntity)pathAwareEntity).getBrain();
        brain.forget(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, E pathAwareEntity, long l) {
        Vec3d vec3d;
        if (((MobEntity)pathAwareEntity).getNavigation().isIdle() && (vec3d = this.findTarget(pathAwareEntity, serverWorld)) != null) {
            ((LivingEntity)pathAwareEntity).getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(vec3d, this.speed, 0));
        }
    }

    @Nullable
    private Vec3d findTarget(E entity, ServerWorld world) {
        Optional<Vec3d> optional;
        if (((Entity)entity).isOnFire() && (optional = this.findClosestWater(world, (Entity)entity).map(Vec3d::ofBottomCenter)).isPresent()) {
            return optional.get();
        }
        return FuzzyTargeting.find(entity, 5, 4);
    }

    private Optional<BlockPos> findClosestWater(BlockView world, Entity entity) {
        BlockPos blockPos = entity.getBlockPos();
        if (!world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty()) {
            return Optional.empty();
        }
        Predicate<BlockPos> predicate = MathHelper.ceil(entity.getWidth()) == 2 ? pos -> BlockPos.streamSouthEastSquare(pos).allMatch(posx -> world.getFluidState((BlockPos)posx).isIn(FluidTags.WATER)) : pos -> world.getFluidState((BlockPos)pos).isIn(FluidTags.WATER);
        return BlockPos.findClosest(blockPos, 5, 1, predicate);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (E)((PathAwareEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (E)((PathAwareEntity)entity), time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (E)((PathAwareEntity)entity), time);
    }
}

