/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class WanderAroundPointOfInterestGoal
extends WanderAroundGoal {
    private static final int HORIZONTAL_RANGE = 10;
    private static final int VERTICAL_RANGE = 7;

    public WanderAroundPointOfInterestGoal(PathAwareEntity entity, double speed, boolean canDespawn) {
        super(entity, speed, 10, canDespawn);
    }

    @Override
    public boolean canStart() {
        BlockPos blockPos;
        ServerWorld serverWorld = (ServerWorld)this.mob.getWorld();
        if (serverWorld.isNearOccupiedPointOfInterest(blockPos = this.mob.getBlockPos())) {
            return false;
        }
        return super.canStart();
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        BlockPos blockPos;
        ChunkSectionPos chunkSectionPos;
        ServerWorld serverWorld = (ServerWorld)this.mob.getWorld();
        ChunkSectionPos chunkSectionPos2 = LookTargetUtil.getPosClosestToOccupiedPointOfInterest(serverWorld, chunkSectionPos = ChunkSectionPos.from(blockPos = this.mob.getBlockPos()), 2);
        if (chunkSectionPos2 != chunkSectionPos) {
            return NoPenaltyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(chunkSectionPos2.getCenterPos()), 1.5707963705062866);
        }
        return null;
    }
}

