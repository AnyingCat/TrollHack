/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public interface Fertilizable {
    public boolean isFertilizable(WorldView var1, BlockPos var2, BlockState var3);

    public boolean canGrow(World var1, Random var2, BlockPos var3, BlockState var4);

    public void grow(ServerWorld var1, Random var2, BlockPos var3, BlockState var4);

    default public BlockPos getFertilizeParticlePos(BlockPos pos) {
        return switch (this.getFertilizableType().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> pos.up();
            case 1 -> pos;
        };
    }

    default public FertilizableType getFertilizableType() {
        return FertilizableType.GROWER;
    }

    public static enum FertilizableType {
        NEIGHBOR_SPREADER,
        GROWER;

    }
}

