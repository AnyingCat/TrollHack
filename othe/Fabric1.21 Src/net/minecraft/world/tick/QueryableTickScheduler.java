/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.tick;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.TickScheduler;

public interface QueryableTickScheduler<T>
extends TickScheduler<T> {
    public boolean isTicking(BlockPos var1, T var2);
}

