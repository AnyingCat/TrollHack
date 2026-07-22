/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerLightingProvider
extends LightingProvider
implements AutoCloseable {
    public static final int field_44692 = 1000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final TaskExecutor<Runnable> processor;
    private final ObjectList<Pair<Stage, Runnable>> pendingTasks = new ObjectArrayList<Pair<Stage, Runnable>>();
    private final ServerChunkLoadingManager chunkLoadingManager;
    private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> executor;
    private final int taskBatchSize = 1000;
    private final AtomicBoolean ticking = new AtomicBoolean();

    public ServerLightingProvider(ChunkProvider chunkProvider, ServerChunkLoadingManager chunkLoadingManager, boolean hasBlockLight, TaskExecutor<Runnable> processor, MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> executor) {
        super(chunkProvider, true, hasBlockLight);
        this.chunkLoadingManager = chunkLoadingManager;
        this.executor = executor;
        this.processor = processor;
    }

    @Override
    public void close() {
    }

    @Override
    public int doLightUpdates() {
        throw Util.throwOrPause(new UnsupportedOperationException("Ran automatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPos pos) {
        BlockPos blockPos = pos.toImmutable();
        this.enqueue(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()), Stage.PRE_UPDATE, Util.debugRunnable(() -> super.checkBlock(blockPos), () -> "checkBlock " + String.valueOf(blockPos)));
    }

    protected void updateChunkStatus(ChunkPos pos) {
        this.enqueue(pos.x, pos.z, () -> 0, Stage.PRE_UPDATE, Util.debugRunnable(() -> {
            int i;
            super.setRetainData(pos, false);
            super.setColumnEnabled(pos, false);
            for (i = this.getBottomY(); i < this.getTopY(); ++i) {
                super.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, i), null);
                super.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, i), null);
            }
            for (i = this.world.getBottomSectionCoord(); i < this.world.getTopSectionCoord(); ++i) {
                super.setSectionStatus(ChunkSectionPos.from(pos, i), true);
            }
        }, () -> "updateChunkStatus " + String.valueOf(pos) + " true"));
    }

    @Override
    public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
        this.enqueue(pos.getSectionX(), pos.getSectionZ(), () -> 0, Stage.PRE_UPDATE, Util.debugRunnable(() -> super.setSectionStatus(pos, notReady), () -> "updateSectionStatus " + String.valueOf(pos) + " " + notReady));
    }

    @Override
    public void propagateLight(ChunkPos chunkPos) {
        this.enqueue(chunkPos.x, chunkPos.z, Stage.PRE_UPDATE, Util.debugRunnable(() -> super.propagateLight(chunkPos), () -> "propagateLight " + String.valueOf(chunkPos)));
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        this.enqueue(pos.x, pos.z, Stage.PRE_UPDATE, Util.debugRunnable(() -> super.setColumnEnabled(pos, retainData), () -> "enableLight " + String.valueOf(pos) + " " + retainData));
    }

    @Override
    public void enqueueSectionData(LightType lightType, ChunkSectionPos pos, @Nullable ChunkNibbleArray nibbles) {
        this.enqueue(pos.getSectionX(), pos.getSectionZ(), () -> 0, Stage.PRE_UPDATE, Util.debugRunnable(() -> super.enqueueSectionData(lightType, pos, nibbles), () -> "queueData " + String.valueOf(pos)));
    }

    private void enqueue(int x, int z, Stage stage, Runnable task) {
        this.enqueue(x, z, this.chunkLoadingManager.getCompletedLevelSupplier(ChunkPos.toLong(x, z)), stage, task);
    }

    private void enqueue(int x, int z, IntSupplier completedLevelSupplier, Stage stage, Runnable task) {
        this.executor.send(ChunkTaskPrioritySystem.createMessage(() -> {
            this.pendingTasks.add(Pair.of(stage, task));
            if (this.pendingTasks.size() >= 1000) {
                this.runTasks();
            }
        }, ChunkPos.toLong(x, z), completedLevelSupplier));
    }

    @Override
    public void setRetainData(ChunkPos pos, boolean retainData) {
        this.enqueue(pos.x, pos.z, () -> 0, Stage.PRE_UPDATE, Util.debugRunnable(() -> super.setRetainData(pos, retainData), () -> "retainData " + String.valueOf(pos)));
    }

    public CompletableFuture<Chunk> initializeLight(Chunk chunk, boolean bl) {
        ChunkPos chunkPos = chunk.getPos();
        this.enqueue(chunkPos.x, chunkPos.z, Stage.PRE_UPDATE, Util.debugRunnable(() -> {
            ChunkSection[] chunkSections = chunk.getSectionArray();
            for (int i = 0; i < chunk.countVerticalSections(); ++i) {
                ChunkSection chunkSection = chunkSections[i];
                if (chunkSection.isEmpty()) continue;
                int j = this.world.sectionIndexToCoord(i);
                super.setSectionStatus(ChunkSectionPos.from(chunkPos, j), false);
            }
        }, () -> "initializeLight: " + String.valueOf(chunkPos)));
        return CompletableFuture.supplyAsync(() -> {
            super.setColumnEnabled(chunkPos, bl);
            super.setRetainData(chunkPos, false);
            return chunk;
        }, task -> this.enqueue(chunkPos.x, chunkPos.z, Stage.POST_UPDATE, task));
    }

    public CompletableFuture<Chunk> light(Chunk chunk, boolean excludeBlocks) {
        ChunkPos chunkPos = chunk.getPos();
        chunk.setLightOn(false);
        this.enqueue(chunkPos.x, chunkPos.z, Stage.PRE_UPDATE, Util.debugRunnable(() -> {
            if (!excludeBlocks) {
                super.propagateLight(chunkPos);
            }
        }, () -> "lightChunk " + String.valueOf(chunkPos) + " " + excludeBlocks));
        return CompletableFuture.supplyAsync(() -> {
            chunk.setLightOn(true);
            return chunk;
        }, task -> this.enqueue(chunkPos.x, chunkPos.z, Stage.POST_UPDATE, task));
    }

    public void tick() {
        if ((!this.pendingTasks.isEmpty() || super.hasUpdates()) && this.ticking.compareAndSet(false, true)) {
            this.processor.send(() -> {
                this.runTasks();
                this.ticking.set(false);
            });
        }
    }

    private void runTasks() {
        Pair pair;
        int j;
        int i = Math.min(this.pendingTasks.size(), 1000);
        Iterator objectListIterator = this.pendingTasks.iterator();
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() != Stage.PRE_UPDATE) continue;
            ((Runnable)pair.getSecond()).run();
        }
        objectListIterator.back(j);
        super.doLightUpdates();
        for (j = 0; objectListIterator.hasNext() && j < i; ++j) {
            pair = (Pair)objectListIterator.next();
            if (pair.getFirst() == Stage.POST_UPDATE) {
                ((Runnable)pair.getSecond()).run();
            }
            objectListIterator.remove();
        }
    }

    public CompletableFuture<?> enqueue(int x, int z) {
        return CompletableFuture.runAsync(() -> {}, callback -> this.enqueue(x, z, Stage.POST_UPDATE, callback));
    }

    static enum Stage {
        PRE_UPDATE,
        POST_UPDATE;

    }
}

