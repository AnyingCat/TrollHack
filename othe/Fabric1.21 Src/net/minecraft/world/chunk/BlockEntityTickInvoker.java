/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import net.minecraft.util.math.BlockPos;

public interface BlockEntityTickInvoker {
    public void tick();

    public boolean isRemoved();

    public BlockPos getPos();

    public String getName();
}

