/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jetbrains.annotations.Nullable;

public interface BlockSource {
    @Nullable
    public BlockState apply(ChunkNoiseSampler var1, int var2, int var3, int var4);
}

