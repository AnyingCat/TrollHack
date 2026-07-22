/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.chunk;

import net.minecraft.block.BlockState;

public interface BlockColumn {
    public BlockState getState(int var1);

    public void setState(int var1, BlockState var2);
}

