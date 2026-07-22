/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.PathNodeTypeCache;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PathContext {
    private final CollisionView world;
    @Nullable
    private final PathNodeTypeCache nodeTypeCache;
    private final BlockPos entityPos;
    private final BlockPos.Mutable lastNodePos = new BlockPos.Mutable();

    public PathContext(CollisionView world, MobEntity entity) {
        this.world = world;
        World world2 = entity.getWorld();
        if (world2 instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world2;
            this.nodeTypeCache = serverWorld.getPathNodeTypeCache();
        } else {
            this.nodeTypeCache = null;
        }
        this.entityPos = entity.getBlockPos();
    }

    public PathNodeType getNodeType(int x, int y, int z) {
        BlockPos.Mutable blockPos = this.lastNodePos.set(x, y, z);
        if (this.nodeTypeCache == null) {
            return LandPathNodeMaker.getCommonNodeType(this.world, blockPos);
        }
        return this.nodeTypeCache.add(this.world, blockPos);
    }

    public BlockState getBlockState(BlockPos pos) {
        return this.world.getBlockState(pos);
    }

    public CollisionView getWorld() {
        return this.world;
    }

    public BlockPos getEntityPos() {
        return this.entityPos;
    }
}

