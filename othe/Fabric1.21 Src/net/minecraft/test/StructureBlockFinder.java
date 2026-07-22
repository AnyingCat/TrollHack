/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface StructureBlockFinder {
    public Stream<BlockPos> findStructureBlockPos();
}

