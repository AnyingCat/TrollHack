/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public interface SpawnLocation {
    public boolean isSpawnPositionOk(WorldView var1, BlockPos var2, @Nullable EntityType<?> var3);

    default public BlockPos adjustPosition(WorldView world, BlockPos pos) {
        return pos;
    }
}

