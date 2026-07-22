/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkStatus;

public interface ChunkLoadingManager {
    public AbstractChunkHolder acquire(long var1);

    public void release(AbstractChunkHolder var1);

    public CompletableFuture<Chunk> generate(AbstractChunkHolder var1, ChunkGenerationStep var2, BoundedRegionArray<AbstractChunkHolder> var3);

    public ChunkLoader createLoader(ChunkStatus var1, ChunkPos var2);

    public void updateChunks();
}

