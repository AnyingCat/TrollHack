/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProperties;

public interface MutableWorldProperties
extends WorldProperties {
    public void setSpawnPos(BlockPos var1, float var2);
}

