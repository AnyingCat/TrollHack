/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkLoadingManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractChunkHolder {
    private static final List<ChunkStatus> STATUSES = ChunkStatus.createOrderedList();
    private static final OptionalChunk<Chunk> NOT_DONE = OptionalChunk.of("Not done yet");
    public static final OptionalChunk<Chunk> UNLOADED = OptionalChunk.of("Unloaded chunk");
    public static final CompletableFuture<OptionalChunk<Chunk>> UNLOADED_FUTURE = CompletableFuture.completedFuture(UNLOADED);
    protected final ChunkPos pos;
    @Nullable
    private volatile ChunkStatus status;
    private final AtomicReference<ChunkStatus> currentStatus = new AtomicReference();
    private final AtomicReferenceArray<CompletableFuture<OptionalChunk<Chunk>>> chunkFuturesByStatus = new AtomicReferenceArray(STATUSES.size());
    private final AtomicReference<ChunkLoader> chunkLoader = new AtomicReference();
    private final AtomicInteger refCount = new AtomicInteger();

    public AbstractChunkHolder(ChunkPos pos) {
        this.pos = pos;
    }

    public CompletableFuture<OptionalChunk<Chunk>> load(ChunkStatus requestedStatus, ServerChunkLoadingManager chunkLoadingManager) {
        if (this.cannotBeLoaded(requestedStatus)) {
            return UNLOADED_FUTURE;
        }
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.getOrCreateFuture(requestedStatus);
        if (completableFuture.isDone()) {
            return completableFuture;
        }
        ChunkLoader chunkLoader = this.chunkLoader.get();
        if (chunkLoader == null || requestedStatus.isLaterThan(chunkLoader.targetStatus)) {
            this.createLoader(chunkLoadingManager, requestedStatus);
        }
        return completableFuture;
    }

    CompletableFuture<OptionalChunk<Chunk>> generate(ChunkGenerationStep step, ChunkLoadingManager chunkLoadingManager, BoundedRegionArray<AbstractChunkHolder> chunks) {
        if (this.cannotBeLoaded(step.targetStatus())) {
            return UNLOADED_FUTURE;
        }
        if (this.progressStatus(step.targetStatus())) {
            return chunkLoadingManager.generate(this, step, chunks).handle((chunk, throwable) -> {
                if (throwable != null) {
                    CrashReport crashReport = CrashReport.create(throwable, "Exception chunk generation/loading");
                    MinecraftServer.setWorldGenException(new CrashException(crashReport));
                } else {
                    this.completeChunkFuture(step.targetStatus(), (Chunk)chunk);
                }
                return OptionalChunk.of(chunk);
            });
        }
        return this.getOrCreateFuture(step.targetStatus());
    }

    protected void updateStatus(ServerChunkLoadingManager chunkLoadingManager) {
        boolean bl;
        ChunkStatus chunkStatus2;
        ChunkStatus chunkStatus = this.status;
        this.status = chunkStatus2 = ChunkLevels.getStatus(this.getLevel());
        boolean bl2 = bl = chunkStatus != null && (chunkStatus2 == null || chunkStatus2.isEarlierThan(chunkStatus));
        if (bl) {
            this.unload(chunkStatus2, chunkStatus);
            if (this.chunkLoader.get() != null) {
                this.createLoader(chunkLoadingManager, this.getMaxPendingStatus(chunkStatus2));
            }
        }
    }

    public void replaceWith(WrapperProtoChunk chunk) {
        CompletableFuture<OptionalChunk<WrapperProtoChunk>> completableFuture = CompletableFuture.completedFuture(OptionalChunk.of(chunk));
        for (int i = 0; i < this.chunkFuturesByStatus.length() - 1; ++i) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture2 = this.chunkFuturesByStatus.get(i);
            Objects.requireNonNull(completableFuture2);
            Chunk chunk2 = completableFuture2.getNow(NOT_DONE).orElse(null);
            if (chunk2 instanceof ProtoChunk) {
                if (this.chunkFuturesByStatus.compareAndSet(i, completableFuture2, completableFuture)) continue;
                throw new IllegalStateException("Future changed by other thread while trying to replace it");
            }
            throw new IllegalStateException("Trying to replace a ProtoChunk, but found " + String.valueOf(chunk2));
        }
    }

    void clearLoader(ChunkLoader loader) {
        this.chunkLoader.compareAndSet(loader, null);
    }

    private void createLoader(ServerChunkLoadingManager chunkLoadingManager, @Nullable ChunkStatus requestedStatus) {
        ChunkLoader chunkLoader = requestedStatus != null ? chunkLoadingManager.createLoader(requestedStatus, this.getPos()) : null;
        ChunkLoader chunkLoader2 = this.chunkLoader.getAndSet(chunkLoader);
        if (chunkLoader2 != null) {
            chunkLoader2.markPendingDisposal();
        }
    }

    private CompletableFuture<OptionalChunk<Chunk>> getOrCreateFuture(ChunkStatus status) {
        if (this.cannotBeLoaded(status)) {
            return UNLOADED_FUTURE;
        }
        int i = status.getIndex();
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(i);
        while (completableFuture == null) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture2 = new CompletableFuture<OptionalChunk<Chunk>>();
            completableFuture = this.chunkFuturesByStatus.compareAndExchange(i, null, completableFuture2);
            if (completableFuture != null) continue;
            if (this.cannotBeLoaded(status)) {
                this.unload(i, completableFuture2);
                return UNLOADED_FUTURE;
            }
            return completableFuture2;
        }
        return completableFuture;
    }

    private void unload(@Nullable ChunkStatus from, ChunkStatus to) {
        int i = from == null ? 0 : from.getIndex() + 1;
        int j = to.getIndex();
        for (int k = i; k <= j; ++k) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(k);
            if (completableFuture == null) continue;
            this.unload(k, completableFuture);
        }
    }

    private void unload(int statusIndex, CompletableFuture<OptionalChunk<Chunk>> previousFuture) {
        if (previousFuture.complete(UNLOADED) && !this.chunkFuturesByStatus.compareAndSet(statusIndex, previousFuture, null)) {
            throw new IllegalStateException("Nothing else should replace the future here");
        }
    }

    private void completeChunkFuture(ChunkStatus status, Chunk chunk) {
        OptionalChunk<Chunk> optionalChunk = OptionalChunk.of(chunk);
        int i = status.getIndex();
        while (true) {
            CompletableFuture<OptionalChunk<Chunk>> completableFuture;
            if ((completableFuture = this.chunkFuturesByStatus.get(i)) == null) {
                if (!this.chunkFuturesByStatus.compareAndSet(i, null, CompletableFuture.completedFuture(optionalChunk))) continue;
                return;
            }
            if (completableFuture.complete(optionalChunk)) {
                return;
            }
            if (completableFuture.getNow(NOT_DONE).isPresent()) {
                throw new IllegalStateException("Trying to complete a future but found it to be completed successfully already");
            }
            Thread.yield();
        }
    }

    @Nullable
    private ChunkStatus getMaxPendingStatus(@Nullable ChunkStatus checkUpperBound) {
        if (checkUpperBound == null) {
            return null;
        }
        ChunkStatus chunkStatus = checkUpperBound;
        ChunkStatus chunkStatus2 = this.currentStatus.get();
        while (chunkStatus2 == null || chunkStatus.isLaterThan(chunkStatus2)) {
            if (this.chunkFuturesByStatus.get(chunkStatus.getIndex()) != null) {
                return chunkStatus;
            }
            if (chunkStatus == ChunkStatus.EMPTY) break;
            chunkStatus = chunkStatus.getPrevious();
        }
        return null;
    }

    /**
     * Progresses to {@code nextStatus} if that is actually the next status
     * for the current status. Does nothing if {@code nextStatus} is the same as the
     * current status (or earlier).
     * 
     * @return whether the progress was made
     * @throws IllegalStateException when skipping intermediate statuses between the
     * current and {@code nextStatus}
     */
    private boolean progressStatus(ChunkStatus nextStatus) {
        ChunkStatus chunkStatus = nextStatus == ChunkStatus.EMPTY ? null : nextStatus.getPrevious();
        ChunkStatus chunkStatus2 = this.currentStatus.compareAndExchange(chunkStatus, nextStatus);
        if (chunkStatus2 == chunkStatus) {
            return true;
        }
        if (chunkStatus2 == null || nextStatus.isLaterThan(chunkStatus2)) {
            throw new IllegalStateException("Unexpected last startedWork status: " + String.valueOf(chunkStatus2) + " while trying to start: " + String.valueOf(nextStatus));
        }
        return false;
    }

    private boolean cannotBeLoaded(ChunkStatus status) {
        ChunkStatus chunkStatus = this.status;
        return chunkStatus == null || status.isLaterThan(chunkStatus);
    }

    public void incrementRefCount() {
        this.refCount.incrementAndGet();
    }

    public void decrementRefCount() {
        int i = this.refCount.decrementAndGet();
        if (i < 0) {
            throw new IllegalStateException("More releases than claims. Count: " + i);
        }
    }

    public int getRefCount() {
        return this.refCount.get();
    }

    @Nullable
    public Chunk getUncheckedOrNull(ChunkStatus requestedStatus) {
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(requestedStatus.getIndex());
        return completableFuture == null ? null : (Chunk)completableFuture.getNow(NOT_DONE).orElse(null);
    }

    @Nullable
    public Chunk getOrNull(ChunkStatus requestedStatus) {
        if (this.cannotBeLoaded(requestedStatus)) {
            return null;
        }
        return this.getUncheckedOrNull(requestedStatus);
    }

    @Nullable
    public Chunk getLatest() {
        ChunkStatus chunkStatus = this.currentStatus.get();
        if (chunkStatus == null) {
            return null;
        }
        Chunk chunk = this.getUncheckedOrNull(chunkStatus);
        if (chunk != null) {
            return chunk;
        }
        return this.getUncheckedOrNull(chunkStatus.getPrevious());
    }

    @Nullable
    public ChunkStatus getActualStatus() {
        CompletableFuture<OptionalChunk<Chunk>> completableFuture = this.chunkFuturesByStatus.get(ChunkStatus.EMPTY.getIndex());
        Chunk chunk = completableFuture == null ? null : (Chunk)completableFuture.getNow(NOT_DONE).orElse(null);
        return chunk == null ? null : chunk.getStatus();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public ChunkLevelType getLevelType() {
        return ChunkLevels.getType(this.getLevel());
    }

    public abstract int getLevel();

    public abstract int getCompletedLevel();

    @Debug
    public List<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> enumerateFutures() {
        ArrayList<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>> list = new ArrayList<Pair<ChunkStatus, CompletableFuture<OptionalChunk<Chunk>>>>();
        for (int i = 0; i < STATUSES.size(); ++i) {
            list.add(Pair.of(STATUSES.get(i), this.chunkFuturesByStatus.get(i)));
        }
        return list;
    }

    @Nullable
    @Debug
    public ChunkStatus getLatestStatus() {
        for (int i = STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkStatus = STATUSES.get(i);
            Chunk chunk = this.getUncheckedOrNull(chunkStatus);
            if (chunk == null) continue;
            return chunkStatus;
        }
        return null;
    }
}

