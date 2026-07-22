/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.ChunkDataList;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import org.slf4j.Logger;

public class EntityChunkDataAccess
implements ChunkDataAccess<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_KEY = "Entities";
    private static final String POSITION_KEY = "Position";
    private final ServerWorld world;
    private final ChunkPosKeyedStorage storage;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final TaskExecutor<Runnable> taskExecutor;

    public EntityChunkDataAccess(ChunkPosKeyedStorage storage, ServerWorld world, Executor executor) {
        this.storage = storage;
        this.world = world;
        this.taskExecutor = TaskExecutor.create(executor, "entity-deserializer");
    }

    @Override
    public CompletableFuture<ChunkDataList<Entity>> readChunkData(ChunkPos pos) {
        if (this.emptyChunks.contains(pos.toLong())) {
            return CompletableFuture.completedFuture(EntityChunkDataAccess.emptyDataList(pos));
        }
        CompletableFuture<Optional<NbtCompound>> completableFuture = this.storage.read(pos);
        this.handleLoadFailure(completableFuture, pos);
        return completableFuture.thenApplyAsync(nbt -> {
            if (nbt.isEmpty()) {
                this.emptyChunks.add(pos.toLong());
                return EntityChunkDataAccess.emptyDataList(pos);
            }
            try {
                ChunkPos chunkPos2 = EntityChunkDataAccess.getChunkPos((NbtCompound)nbt.get());
                if (!Objects.equals(pos, chunkPos2)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", pos, pos, chunkPos2);
                    this.world.getServer().onChunkMisplacement(chunkPos2, pos, this.storage.getStorageKey());
                }
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)pos, (Object)exception);
                this.world.getServer().onChunkLoadFailure(exception, this.storage.getStorageKey(), pos);
            }
            NbtCompound nbtCompound = this.storage.update((NbtCompound)nbt.get(), -1);
            NbtList nbtList = nbtCompound.getList(ENTITIES_KEY, NbtElement.COMPOUND_TYPE);
            List list = EntityType.streamFromNbt(nbtList, this.world).collect(ImmutableList.toImmutableList());
            return new ChunkDataList(pos, list);
        }, this.taskExecutor::send);
    }

    private static ChunkPos getChunkPos(NbtCompound chunkNbt) {
        int[] is = chunkNbt.getIntArray(POSITION_KEY);
        return new ChunkPos(is[0], is[1]);
    }

    private static void putChunkPos(NbtCompound chunkNbt, ChunkPos pos) {
        chunkNbt.put(POSITION_KEY, new NbtIntArray(new int[]{pos.x, pos.z}));
    }

    private static ChunkDataList<Entity> emptyDataList(ChunkPos pos) {
        return new ChunkDataList<Entity>(pos, ImmutableList.of());
    }

    @Override
    public void writeChunkData(ChunkDataList<Entity> dataList) {
        ChunkPos chunkPos = dataList.getChunkPos();
        if (dataList.isEmpty()) {
            if (this.emptyChunks.add(chunkPos.toLong())) {
                this.handleSaveFailure(this.storage.set(chunkPos, null), chunkPos);
            }
            return;
        }
        NbtList nbtList = new NbtList();
        dataList.stream().forEach(entity -> {
            NbtCompound nbtCompound = new NbtCompound();
            if (entity.saveNbt(nbtCompound)) {
                nbtList.add(nbtCompound);
            }
        });
        NbtCompound nbtCompound = NbtHelper.putDataVersion(new NbtCompound());
        nbtCompound.put(ENTITIES_KEY, nbtList);
        EntityChunkDataAccess.putChunkPos(nbtCompound, chunkPos);
        this.handleSaveFailure(this.storage.set(chunkPos, nbtCompound), chunkPos);
        this.emptyChunks.remove(chunkPos.toLong());
    }

    private void handleSaveFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to store entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkSaveFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    private void handleLoadFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to load entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkLoadFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    @Override
    public void awaitAll(boolean sync) {
        this.storage.completeAll(sync).join();
        this.taskExecutor.awaitAll();
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }
}

