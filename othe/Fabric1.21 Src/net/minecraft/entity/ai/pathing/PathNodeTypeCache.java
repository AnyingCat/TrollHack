/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class PathNodeTypeCache {
    private static final int field_49417 = 4096;
    private static final int field_49418 = 4095;
    private final long[] positions = new long[4096];
    private final PathNodeType[] cache = new PathNodeType[4096];

    public PathNodeType add(BlockView world, BlockPos pos) {
        long l = pos.asLong();
        int i = PathNodeTypeCache.hash(l);
        PathNodeType pathNodeType = this.get(i, l);
        if (pathNodeType != null) {
            return pathNodeType;
        }
        return this.compute(world, pos, i, l);
    }

    @Nullable
    private PathNodeType get(int index, long pos) {
        if (this.positions[index] == pos) {
            return this.cache[index];
        }
        return null;
    }

    private PathNodeType compute(BlockView world, BlockPos pos, int index, long longPos) {
        PathNodeType pathNodeType = LandPathNodeMaker.getCommonNodeType(world, pos);
        this.positions[index] = longPos;
        this.cache[index] = pathNodeType;
        return pathNodeType;
    }

    public void invalidate(BlockPos pos) {
        long l = pos.asLong();
        int i = PathNodeTypeCache.hash(l);
        if (this.positions[i] == l) {
            this.cache[i] = null;
        }
    }

    private static int hash(long pos) {
        return (int)HashCommon.mix(pos) & 0xFFF;
    }
}

