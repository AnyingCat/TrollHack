/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public interface FluidFillable {
    public boolean canFillWithFluid(@Nullable PlayerEntity var1, BlockView var2, BlockPos var3, BlockState var4, Fluid var5);

    public boolean tryFillWithFluid(WorldAccess var1, BlockPos var2, BlockState var3, FluidState var4);
}

