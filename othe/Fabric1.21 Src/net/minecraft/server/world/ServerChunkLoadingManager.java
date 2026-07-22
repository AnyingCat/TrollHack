/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkBiomeDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.LevelPrioritizedQueue;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.PlayerChunkWatchingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.Util;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkLoadingManager;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.SimulationDistanceLevelPropagator;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkLoader;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.chunk.ChunkType;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerChunkLoadingManager
extends VersionedChunkStorage
implements ChunkHolder.PlayersWatchingChunkProvider,
ChunkLoadingManager {
    private static final OptionalChunk<List<Chunk>> UNLOADED_CHUNKS = OptionalChunk.of("Unloaded chunks found in range");
    private static final CompletableFuture<OptionalChunk<List<Chunk>>> UNLOADED_CHUNKS_FUTURE = CompletableFuture.completedFuture(UNLOADED_CHUNKS);
    private static final byte PROTO_CHUNK = -1;
    private static final byte UNMARKED_CHUNK = 0;
    private static final byte LEVEL_CHUNK = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29674 = 200;
    private static final int field_36291 = 20;
    private static final int field_36384 = 10000;
    public static final int DEFAULT_VIEW_DISTANCE = 2;
    public static final int field_29669 = 32;
    public static final int field_29670 = ChunkLevels.getLevelFromType(ChunkLevelType.ENTITY_TICKING);
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> currentChunkHolders = new Long2ObjectLinkedOpenHashMap();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkHolders = this.currentChunkHolders.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload = new Long2ObjectLinkedOpenHashMap();
    private final List<ChunkLoader> loaders = new ArrayList<ChunkLoader>();
    final ServerWorld world;
    private final ServerLightingProvider lightingProvider;
    private final ThreadExecutor<Runnable> mainThreadExecutor;
    private final NoiseConfig noiseConfig;
    private final StructurePlacementCalculator structurePlacementCalculator;
    private final Supplier<PersistentStateManager> persistentStateManagerFactory;
    private final PointOfInterestStorage pointOfInterestStorage;
    final LongSet unloadedChunks = new LongOpenHashSet();
    private boolean chunkHolderListDirty;
    private final ChunkTaskPrioritySystem chunkTaskPrioritySystem;
    private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldGenExecutor;
    private final MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainExecutor;
    private final WorldGenerationProgressListener worldGenerationProgressListener;
    private final ChunkStatusChangeListener chunkStatusChangeListener;
    private final TicketManager ticketManager;
    private final AtomicInteger totalChunksLoadedCount = new AtomicInteger();
    private final String saveDir;
    private final PlayerChunkWatchingManager playerChunkWatchingManager = new PlayerChunkWatchingManager();
    private final Int2ObjectMap<EntityTracker> entityTrackers = new Int2ObjectOpenHashMap<EntityTracker>();
    private final Long2ByteMap chunkToType = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkToNextSaveTimeMs = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadTaskQueue = Queues.newConcurrentLinkedQueue();
    private int watchDistance;
    private final ChunkGenerationContext generationContext;

    public ServerChunkLoadingManager(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync) {
        super(new StorageKey(session.getDirectoryName(), world.getRegistryKey(), "chunk"), session.getWorldDirectory(world.getRegistryKey()).resolve("region"), dataFixer, dsync);
        Path path = session.getWorldDirectory(world.getRegistryKey());
        this.saveDir = path.getFileName().toString();
        this.world = world;
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();
        long l = world.getSeed();
        if (chunkGenerator instanceof NoiseChunkGenerator) {
            NoiseChunkGenerator noiseChunkGenerator = (NoiseChunkGenerator)chunkGenerator;
            this.noiseConfig = NoiseConfig.create(noiseChunkGenerator.getSettings().value(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        } else {
            this.noiseConfig = NoiseConfig.create(ChunkGeneratorSettings.createMissingSettings(), dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.NOISE_PARAMETERS), l);
        }
        this.structurePlacementCalculator = chunkGenerator.createStructurePlacementCalculator(dynamicRegistryManager.getWrapperOrThrow(RegistryKeys.STRUCTURE_SET), this.noiseConfig, l);
        this.mainThreadExecutor = mainThreadExecutor;
        TaskExecutor<Runnable> taskExecutor = TaskExecutor.create(executor, "worldgen");
        MessageListener<Runnable> messageListener = MessageListener.create("main", mainThreadExecutor::send);
        this.worldGenerationProgressListener = worldGenerationProgressListener;
        this.chunkStatusChangeListener = chunkStatusChangeListener;
        TaskExecutor<Runnable> taskExecutor2 = TaskExecutor.create(executor, "light");
        this.chunkTaskPrioritySystem = new ChunkTaskPrioritySystem(ImmutableList.of(taskExecutor, messageListener, taskExecutor2), executor, Integer.MAX_VALUE);
        this.worldGenExecutor = this.chunkTaskPrioritySystem.createExecutor(taskExecutor, false);
        this.mainExecutor = this.chunkTaskPrioritySystem.createExecutor(messageListener, false);
        this.lightingProvider = new ServerLightingProvider(chunkProvider, this, this.world.getDimension().hasSkyLight(), taskExecutor2, this.chunkTaskPrioritySystem.createExecutor(taskExecutor2, false));
        this.ticketManager = new TicketManager(executor, mainThreadExecutor);
        this.persistentStateManagerFactory = persistentStateManagerFactory;
        this.pointOfInterestStorage = new PointOfInterestStorage(new StorageKey(session.getDirectoryName(), world.getRegistryKey(), "poi"), path.resolve("poi"), dataFixer, dsync, dynamicRegistryManager, world.getServer(), world);
        this.setViewDistance(viewDistance);
        this.generationContext = new ChunkGenerationContext(world, chunkGenerator, structureTemplateManager, this.lightingProvider, this.mainExecutor);
    }

    protected ChunkGenerator getChunkGenerator() {
        return this.generationContext.generator();
    }

    protected StructurePlacementCalculator getStructurePlacementCalculator() {
        return this.structurePlacementCalculator;
    }

    protected NoiseConfig getNoiseConfig() {
        return this.noiseConfig;
    }

    private static double getSquaredDistance(ChunkPos pos, Entity entity) {
        double d = ChunkSectionPos.getOffsetPos(pos.x, 8);
        double e = ChunkSectionPos.getOffsetPos(pos.z, 8);
        double f = d - entity.getX();
        double g = e - entity.getZ();
        return f * f + g * g;
    }

    boolean isTracked(ServerPlayerEntity player, int chunkX, int chunkZ) {
        return player.getChunkFilter().isWithinDistance(chunkX, chunkZ) && !player.networkHandler.chunkDataSender.isInNextBatch(ChunkPos.toLong(chunkX, chunkZ));
    }

    private boolean isOnTrackEdge(ServerPlayerEntity player, int chunkX, int chunkZ) {
        if (!this.isTracked(player, chunkX, chunkZ)) {
            return false;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0 || this.isTracked(player, chunkX + i, chunkZ + j)) continue;
                return true;
            }
        }
        return false;
    }

    protected ServerLightingProvider getLightingProvider() {
        return this.lightingProvider;
    }

    @Nullable
    protected ChunkHolder getCurrentChunkHolder(long pos) {
        return this.currentChunkHolders.get(pos);
    }

    @Nullable
    protected ChunkHolder getChunkHolder(long pos) {
        return this.chunkHolders.get(pos);
    }

    protected IntSupplier getCompletedLevelSupplier(long pos) {
        return () -> {
            ChunkHolder chunkHolder = this.getChunkHolder(pos);
            if (chunkHolder == null) {
                return LevelPrioritizedQueue.LEVEL_COUNT - 1;
            }
            return Math.min(chunkHolder.getCompletedLevel(), LevelPrioritizedQueue.LEVEL_COUNT - 1);
        };
    }

    public String getChunkLoadingDebugInfo(ChunkPos chunkPos) {
        ChunkHolder chunkHolder = this.getChunkHolder(chunkPos.toLong());
        if (chunkHolder == null) {
            return "null";
        }
        String string = chunkHolder.getLevel() + "\n";
        ChunkStatus chunkStatus = chunkHolder.getLatestStatus();
        Chunk chunk = chunkHolder.getLatest();
        if (chunkStatus != null) {
            string = string + "St: \u00a7" + chunkStatus.getIndex() + String.valueOf(chunkStatus) + "\u00a7r\n";
        }
        if (chunk != null) {
            string = string + "Ch: \u00a7" + chunk.getStatus().getIndex() + String.valueOf(chunk.getStatus()) + "\u00a7r\n";
        }
        ChunkLevelType chunkLevelType = chunkHolder.getLevelType();
        string = string + String.valueOf('\u00a7') + chunkLevelType.ordinal() + String.valueOf((Object)chunkLevelType);
        return string + "\u00a7r";
    }

    private CompletableFuture<OptionalChunk<List<Chunk>>> getRegion(ChunkHolder centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus) {
        if (margin == 0) {
            ChunkStatus chunkStatus = distanceToStatus.apply(0);
            return centerChunk.load(chunkStatus, this).thenApply(optionalChunk -> optionalChunk.map(List::of));
        }
        ArrayList<CompletableFuture<OptionalChunk<Chunk>>> list2 = new ArrayList<CompletableFuture<OptionalChunk<Chunk>>>();
        ChunkPos chunkPos = centerChunk.getPos();
        for (int i = -margin; i <= margin; ++i) {
            for (int j = -margin; j <= margin; ++j) {
                int k = Math.max(Math.abs(j), Math.abs(i));
                long l = ChunkPos.toLong(chunkPos.x + j, chunkPos.z + i);
                ChunkHolder chunkHolder = this.getCurrentChunkHolder(l);
                if (chunkHolder == null) {
                    return UNLOADED_CHUNKS_FUTURE;
                }
                ChunkStatus chunkStatus2 = distanceToStatus.apply(k);
                list2.add(chunkHolder.load(chunkStatus2, this));
            }
        }
        return Util.combineSafe(list2).thenApply(list -> {
            ArrayList<Chunk> list2 = Lists.newArrayList();
            for (OptionalChunk optionalChunk : list) {
                if (optionalChunk == null) {
                    throw this.crash(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }
                Chunk chunk = optionalChunk.orElse(null);
                if (chunk == null) {
                    return UNLOADED_CHUNKS;
                }
                list2.add(chunk);
            }
            return OptionalChunk.of(list2);
        });
    }

    public CrashException crash(IllegalStateException exception, String details) {
        StringBuilder stringBuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = chunkHolder -> chunkHolder.enumerateFutures().forEach(pair -> {
            ChunkStatus chunkStatus = (ChunkStatus)pair.getFirst();
            CompletableFuture completableFuture = (CompletableFuture)pair.getSecond();
            if (completableFuture != null && completableFuture.isDone() && completableFuture.join() == null) {
                stringBuilder.append(chunkHolder.getPos()).append(" - status: ").append(chunkStatus).append(" future: ").append(completableFuture).append(System.lineSeparator());
            }
        });
        stringBuilder.append("Updating:").append(System.lineSeparator());
        this.currentChunkHolders.values().forEach(consumer);
        stringBuilder.append("Visible:").append(System.lineSeparator());
        this.chunkHolders.values().forEach(consumer);
        CrashReport crashReport = CrashReport.create(exception, "Chunk loading");
        CrashReportSection crashReportSection = crashReport.addElement("Chunk loading");
        crashReportSection.add("Details", details);
        crashReportSection.add("Futures", stringBuilder);
        return new CrashException(crashReport);
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkEntitiesTickable(ChunkHolder holder) {
        return this.getRegion(holder, 2, distance -> ChunkStatus.FULL).thenApplyAsync(chunk -> chunk.map(chunks -> (WorldChunk)chunks.get(chunks.size() / 2)), (Executor)this.mainThreadExecutor);
    }

    /**
     * Sets the loading level of {@code ChunkHolder}s. Nonexistent {@code ChunkHolder}s will be created automatically
     * if their loading level is 45 or lower, and chunks whose loading levels are 46 or higher will be scheduled to be removed.
     */
    @Nullable
    ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
        if (!ChunkLevels.isAccessible(i) && !ChunkLevels.isAccessible(level)) {
            return holder;
        }
        if (holder != null) {
            holder.setLevel(level);
        }
        if (holder != null) {
            if (!ChunkLevels.isAccessible(level)) {
                this.unloadedChunks.add(pos);
            } else {
                this.unloadedChunks.remove(pos);
            }
        }
        if (ChunkLevels.isAccessible(level) && holder == null) {
            holder = this.chunksToUnload.remove(pos);
            if (holder != null) {
                holder.setLevel(level);
            } else {
                holder = new ChunkHolder(new ChunkPos(pos), level, this.world, this.lightingProvider, this.chunkTaskPrioritySystem, this);
            }
            this.currentChunkHolders.put(pos, holder);
            this.chunkHolderListDirty = true;
        }
        return holder;
    }

    @Override
    public void close() throws IOException {
        try {
            this.chunkTaskPrioritySystem.close();
            this.pointOfInterestStorage.close();
        } finally {
            super.close();
        }
    }

    protected void save(boolean flush) {
        if (flush) {
            List<ChunkHolder> list = this.chunkHolders.values().stream().filter(ChunkHolder::isAccessible).peek(ChunkHolder::updateAccessibleStatus).toList();
            MutableBoolean mutableBoolean = new MutableBoolean();
            do {
                mutableBoolean.setFalse();
                list.stream().map(holder -> {
                    this.mainThreadExecutor.runTasks(holder::isSavable);
                    return holder.getLatest();
                }).filter(chunk -> chunk instanceof WrapperProtoChunk || chunk instanceof WorldChunk).filter(this::save).forEach(chunk -> mutableBoolean.setTrue());
            } while (mutableBoolean.isTrue());
            this.unloadChunks(() -> true);
            this.completeAll();
        } else {
            this.chunkHolders.values().forEach(this::save);
        }
    }

    protected void tick(BooleanSupplier shouldKeepTicking) {
        Profiler profiler = this.world.getProfiler();
        profiler.push("poi");
        this.pointOfInterestStorage.tick(shouldKeepTicking);
        profiler.swap("chunk_unload");
        if (!this.world.isSavingDisabled()) {
            this.unloadChunks(shouldKeepTicking);
        }
        profiler.pop();
    }

    /**
     * {@return whether the server shutdown should be delayed to process some tasks}
     */
    public boolean shouldDelayShutdown() {
        return this.lightingProvider.hasUpdates() || !this.chunksToUnload.isEmpty() || !this.currentChunkHolders.isEmpty() || this.pointOfInterestStorage.hasUnsavedElements() || !this.unloadedChunks.isEmpty() || !this.unloadTaskQueue.isEmpty() || this.chunkTaskPrioritySystem.shouldDelayShutdown() || this.ticketManager.shouldDelayShutdown();
    }

    private void unloadChunks(BooleanSupplier shouldKeepTicking) {
        Runnable runnable;
        LongIterator longIterator = this.unloadedChunks.iterator();
        int i = 0;
        while (longIterator.hasNext() && (shouldKeepTicking.getAsBoolean() || i < 200 || this.unloadedChunks.size() > 2000)) {
            long l = longIterator.nextLong();
            ChunkHolder chunkHolder = this.currentChunkHolders.get(l);
            if (chunkHolder != null) {
                if (chunkHolder.getRefCount() != 0) continue;
                this.currentChunkHolders.remove(l);
                this.chunksToUnload.put(l, chunkHolder);
                this.chunkHolderListDirty = true;
                ++i;
                this.tryUnloadChunk(l, chunkHolder);
            }
            longIterator.remove();
        }
        for (int j = Math.max(0, this.unloadTaskQueue.size() - 2000); (shouldKeepTicking.getAsBoolean() || j > 0) && (runnable = this.unloadTaskQueue.poll()) != null; --j) {
            runnable.run();
        }
        int k = 0;
        Iterator objectIterator = this.chunkHolders.values().iterator();
        while (k < 20 && shouldKeepTicking.getAsBoolean() && objectIterator.hasNext()) {
            if (!this.save((ChunkHolder)objectIterator.next())) continue;
            ++k;
        }
    }

    private void tryUnloadChunk(long pos, ChunkHolder holder) {
        ((CompletableFuture)holder.getSavingFuture().thenRunAsync(() -> {
            if (!holder.isSavable()) {
                this.tryUnloadChunk(pos, holder);
                return;
            }
            Chunk chunk = holder.getLatest();
            if (this.chunksToUnload.remove(pos, (Object)holder) && chunk != null) {
                WorldChunk worldChunk;
                if (chunk instanceof WorldChunk) {
                    worldChunk = (WorldChunk)chunk;
                    worldChunk.setLoadedToWorld(false);
                }
                this.save(chunk);
                if (chunk instanceof WorldChunk) {
                    worldChunk = (WorldChunk)chunk;
                    this.world.unloadEntities(worldChunk);
                }
                this.lightingProvider.updateChunkStatus(chunk.getPos());
                this.lightingProvider.tick();
                this.worldGenerationProgressListener.setChunkStatus(chunk.getPos(), null);
                this.chunkToNextSaveTimeMs.remove(chunk.getPos().toLong());
            }
        }, this.unloadTaskQueue::add)).whenComplete((void_, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to save chunk {}", (Object)holder.getPos(), throwable);
            }
        });
    }

    protected boolean updateHolderMap() {
        if (!this.chunkHolderListDirty) {
            return false;
        }
        this.chunkHolders = this.currentChunkHolders.clone();
        this.chunkHolderListDirty = false;
        return true;
    }

    private CompletableFuture<Chunk> loadChunk(ChunkPos pos) {
        return ((CompletableFuture)((CompletableFuture)this.getUpdatedChunkNbt(pos).thenApply(nbt -> nbt.filter(nbt2 -> {
            boolean bl = ServerChunkLoadingManager.containsStatus(nbt2);
            if (!bl) {
                LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)pos);
            }
            return bl;
        }))).thenApplyAsync(nbt -> {
            this.world.getProfiler().visit("chunkLoad");
            if (nbt.isPresent()) {
                ProtoChunk chunk = ChunkSerializer.deserialize(this.world, this.pointOfInterestStorage, this.getStorageKey(), pos, (NbtCompound)nbt.get());
                this.mark(pos, ((Chunk)chunk).getStatus().getChunkType());
                return chunk;
            }
            return this.getProtoChunk(pos);
        }, (Executor)this.mainThreadExecutor)).exceptionallyAsync(throwable -> this.recoverFromException((Throwable)throwable, pos), (Executor)this.mainThreadExecutor);
    }

    private static boolean containsStatus(NbtCompound nbt) {
        return nbt.contains("Status", NbtElement.STRING_TYPE);
    }

    private Chunk recoverFromException(Throwable throwable, ChunkPos chunkPos) {
        boolean bl2;
        Throwable throwable2;
        Throwable throwable22;
        if (throwable instanceof CompletionException) {
            CompletionException completionException = (CompletionException)throwable;
            v0 = completionException.getCause();
        } else {
            v0 = throwable22 = throwable;
        }
        if (throwable22 instanceof CrashException) {
            CrashException crashException = (CrashException)throwable22;
            throwable2 = crashException.getCause();
        } else {
            throwable2 = throwable22;
        }
        Throwable throwable3 = throwable2;
        boolean bl = throwable3 instanceof Error;
        boolean bl3 = bl2 = throwable3 instanceof IOException || throwable3 instanceof NbtException;
        if (!bl) {
            if (!bl2) {
                // empty if block
            }
        } else {
            CrashReport crashReport = CrashReport.create(throwable, "Exception loading chunk");
            CrashReportSection crashReportSection = crashReport.addElement("Chunk being loaded");
            crashReportSection.add("pos", chunkPos);
            this.markAsProtoChunk(chunkPos);
            throw new CrashException(crashReport);
        }
        this.world.getServer().onChunkLoadFailure(throwable3, this.getStorageKey(), chunkPos);
        return this.getProtoChunk(chunkPos);
    }

    private Chunk getProtoChunk(ChunkPos chunkPos) {
        this.markAsProtoChunk(chunkPos);
        return new ProtoChunk(chunkPos, UpgradeData.NO_UPGRADE_DATA, this.world, this.world.getRegistryManager().get(RegistryKeys.BIOME), null);
    }

    private void markAsProtoChunk(ChunkPos pos) {
        this.chunkToType.put(pos.toLong(), (byte)-1);
    }

    private byte mark(ChunkPos pos, ChunkType type) {
        return this.chunkToType.put(pos.toLong(), type == ChunkType.PROTOCHUNK ? (byte)-1 : 1);
    }

    @Override
    public AbstractChunkHolder acquire(long pos) {
        ChunkHolder chunkHolder = this.currentChunkHolders.get(pos);
        chunkHolder.incrementRefCount();
        return chunkHolder;
    }

    @Override
    public void release(AbstractChunkHolder chunkHolder) {
        chunkHolder.decrementRefCount();
    }

    @Override
    public CompletableFuture<Chunk> generate(AbstractChunkHolder chunkHolder, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks) {
        ChunkPos chunkPos = chunkHolder.getPos();
        if (step.targetStatus() == ChunkStatus.EMPTY) {
            return this.loadChunk(chunkPos);
        }
        try {
            AbstractChunkHolder abstractChunkHolder = chunks.get(chunkPos.x, chunkPos.z);
            Chunk chunk = abstractChunkHolder.getUncheckedOrNull(step.targetStatus().getPrevious());
            if (chunk == null) {
                throw new IllegalStateException("Parent chunk missing");
            }
            CompletableFuture<Chunk> completableFuture = step.run(this.generationContext, chunks, chunk);
            this.worldGenerationProgressListener.setChunkStatus(chunkPos, step.targetStatus());
            return completableFuture;
        } catch (Exception exception) {
            exception.getStackTrace();
            CrashReport crashReport = CrashReport.create(exception, "Exception generating new chunk");
            CrashReportSection crashReportSection = crashReport.addElement("Chunk to be generated");
            crashReportSection.add("Status being generated", () -> step.targetStatus().getId());
            crashReportSection.add("Location", String.format(Locale.ROOT, "%d,%d", chunkPos.x, chunkPos.z));
            crashReportSection.add("Position hash", ChunkPos.toLong(chunkPos.x, chunkPos.z));
            crashReportSection.add("Generator", this.getChunkGenerator());
            this.mainThreadExecutor.execute(() -> {
                throw new CrashException(crashReport);
            });
            throw new CrashException(crashReport);
        }
    }

    @Override
    public ChunkLoader createLoader(ChunkStatus requestedStatus, ChunkPos pos) {
        ChunkLoader chunkLoader = ChunkLoader.create(this, requestedStatus, pos);
        this.loaders.add(chunkLoader);
        return chunkLoader;
    }

    private void schedule(ChunkLoader chunkLoader) {
        this.worldGenExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkLoader.getHolder(), () -> {
            CompletableFuture<?> completableFuture = chunkLoader.run();
            if (completableFuture == null) {
                return;
            }
            completableFuture.thenRun(() -> this.schedule(chunkLoader));
        }));
    }

    @Override
    public void updateChunks() {
        this.loaders.forEach(this::schedule);
        this.loaders.clear();
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkTickable(ChunkHolder holder) {
        CompletableFuture<OptionalChunk<List<Chunk>>> completableFuture = this.getRegion(holder, 1, distance -> ChunkStatus.FULL);
        CompletionStage completableFuture2 = ((CompletableFuture)completableFuture.thenApplyAsync(optionalChunks -> optionalChunks.map(cs -> (WorldChunk)cs.get(cs.size() / 2)), task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task)))).thenApplyAsync(optionalChunk -> optionalChunk.ifPresent(chunk -> {
            chunk.runPostProcessing();
            this.world.disableTickSchedulers((WorldChunk)chunk);
            CompletableFuture<?> completableFuture = holder.getPostProcessingFuture();
            if (completableFuture.isDone()) {
                this.sendToPlayers((WorldChunk)chunk);
            } else {
                completableFuture.thenAcceptAsync(v -> this.sendToPlayers((WorldChunk)chunk), (Executor)this.mainThreadExecutor);
            }
        }), (Executor)this.mainThreadExecutor);
        ((CompletableFuture)completableFuture2).handle((chunk, throwable) -> {
            this.totalChunksLoadedCount.getAndIncrement();
            return null;
        });
        return completableFuture2;
    }

    private void sendToPlayers(WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        for (ServerPlayerEntity serverPlayerEntity : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!serverPlayerEntity.getChunkFilter().isWithinDistance(chunkPos)) continue;
            ServerChunkLoadingManager.track(serverPlayerEntity, chunk);
        }
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> makeChunkAccessible(ChunkHolder holder) {
        return this.getRegion(holder, 1, ChunkLevels::getStatusForAdditionalLevel).thenApplyAsync(optionalChunks -> optionalChunks.map(chunks -> (WorldChunk)chunks.get(chunks.size() / 2)), task -> this.mainExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, task)));
    }

    public int getTotalChunksLoadedCount() {
        return this.totalChunksLoadedCount.get();
    }

    private boolean save(ChunkHolder chunkHolder) {
        if (!chunkHolder.isAccessible() || !chunkHolder.isSavable()) {
            return false;
        }
        Chunk chunk = chunkHolder.getLatest();
        if (chunk instanceof WrapperProtoChunk || chunk instanceof WorldChunk) {
            long l = chunk.getPos().toLong();
            long m = this.chunkToNextSaveTimeMs.getOrDefault(l, -1L);
            long n = System.currentTimeMillis();
            if (n < m) {
                return false;
            }
            boolean bl = this.save(chunk);
            chunkHolder.updateAccessibleStatus();
            if (bl) {
                this.chunkToNextSaveTimeMs.put(l, n + 10000L);
            }
            return bl;
        }
        return false;
    }

    private boolean save(Chunk chunk) {
        this.pointOfInterestStorage.saveChunk(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        }
        chunk.setNeedsSaving(false);
        ChunkPos chunkPos = chunk.getPos();
        try {
            ChunkStatus chunkStatus = chunk.getStatus();
            if (chunkStatus.getChunkType() != ChunkType.LEVELCHUNK) {
                if (this.isLevelChunk(chunkPos)) {
                    return false;
                }
                if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                    return false;
                }
            }
            this.world.getProfiler().visit("chunkSave");
            NbtCompound nbtCompound = ChunkSerializer.serialize(this.world, chunk);
            this.setNbt(chunkPos, nbtCompound).exceptionally(throwable -> {
                this.world.getServer().onChunkSaveFailure((Throwable)throwable, this.getStorageKey(), chunkPos);
                return null;
            });
            this.mark(chunkPos, chunkStatus.getChunkType());
            return true;
        } catch (Exception exception) {
            this.world.getServer().onChunkSaveFailure(exception, this.getStorageKey(), chunkPos);
            return false;
        }
    }

    private boolean isLevelChunk(ChunkPos pos) {
        NbtCompound nbtCompound;
        byte b = this.chunkToType.get(pos.toLong());
        if (b != 0) {
            return b == 1;
        }
        try {
            nbtCompound = this.getUpdatedChunkNbt(pos).join().orElse(null);
            if (nbtCompound == null) {
                this.markAsProtoChunk(pos);
                return false;
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to read chunk {}", (Object)pos, (Object)exception);
            this.markAsProtoChunk(pos);
            return false;
        }
        ChunkType chunkType = ChunkSerializer.getChunkType(nbtCompound);
        return this.mark(pos, chunkType) == 1;
    }

    protected void setViewDistance(int watchDistance) {
        int i = MathHelper.clamp(watchDistance, 2, 32);
        if (i != this.watchDistance) {
            this.watchDistance = i;
            this.ticketManager.setWatchDistance(this.watchDistance);
            for (ServerPlayerEntity serverPlayerEntity : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
                this.sendWatchPackets(serverPlayerEntity);
            }
        }
    }

    int getViewDistance(ServerPlayerEntity player) {
        return MathHelper.clamp(player.getViewDistance(), 2, this.watchDistance);
    }

    private void track(ServerPlayerEntity player, ChunkPos pos) {
        WorldChunk worldChunk = this.getPostProcessedChunk(pos.toLong());
        if (worldChunk != null) {
            ServerChunkLoadingManager.track(player, worldChunk);
        }
    }

    private static void track(ServerPlayerEntity player, WorldChunk chunk) {
        player.networkHandler.chunkDataSender.add(chunk);
    }

    private static void untrack(ServerPlayerEntity player, ChunkPos pos) {
        player.networkHandler.chunkDataSender.unload(player, pos);
    }

    @Nullable
    public WorldChunk getPostProcessedChunk(long pos) {
        ChunkHolder chunkHolder = this.getChunkHolder(pos);
        if (chunkHolder == null) {
            return null;
        }
        return chunkHolder.getPostProcessedChunk();
    }

    public int getLoadedChunkCount() {
        return this.chunkHolders.size();
    }

    public ChunkTicketManager getTicketManager() {
        return this.ticketManager;
    }

    protected Iterable<ChunkHolder> entryIterator() {
        return Iterables.unmodifiableIterable(this.chunkHolders.values());
    }

    void dump(Writer writer) throws IOException {
        CsvWriter csvWriter = CsvWriter.makeHeader().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").startBody(writer);
        SimulationDistanceLevelPropagator simulationDistanceLevelPropagator = this.ticketManager.getSimulationDistanceTracker();
        for (Long2ObjectMap.Entry entry : this.chunkHolders.long2ObjectEntrySet()) {
            long l = entry.getLongKey();
            ChunkPos chunkPos = new ChunkPos(l);
            ChunkHolder chunkHolder = (ChunkHolder)entry.getValue();
            Optional<Chunk> optional = Optional.ofNullable(chunkHolder.getLatest());
            Optional<Object> optional2 = optional.flatMap(chunk -> chunk instanceof WorldChunk ? Optional.of((WorldChunk)chunk) : Optional.empty());
            csvWriter.printRow(chunkPos.x, chunkPos.z, chunkHolder.getLevel(), optional.isPresent(), optional.map(Chunk::getStatus).orElse(null), optional2.map(WorldChunk::getLevelType).orElse(null), ServerChunkLoadingManager.getFutureStatus(chunkHolder.getAccessibleFuture()), ServerChunkLoadingManager.getFutureStatus(chunkHolder.getTickingFuture()), ServerChunkLoadingManager.getFutureStatus(chunkHolder.getEntityTickingFuture()), this.ticketManager.getTicket(l), this.shouldTick(chunkPos), optional2.map(chunk -> chunk.getBlockEntities().size()).orElse(0), simulationDistanceLevelPropagator.getTickingTicket(l), simulationDistanceLevelPropagator.getLevel(l), optional2.map(chunk -> chunk.getBlockTickScheduler().getTickCount()).orElse(0), optional2.map(chunk -> chunk.getFluidTickScheduler().getTickCount()).orElse(0));
        }
    }

    private static String getFutureStatus(CompletableFuture<OptionalChunk<WorldChunk>> future) {
        try {
            OptionalChunk optionalChunk = future.getNow(null);
            if (optionalChunk != null) {
                return optionalChunk.isPresent() ? "done" : "unloaded";
            }
            return "not completed";
        } catch (CompletionException completionException) {
            return "failed " + completionException.getCause().getMessage();
        } catch (CancellationException cancellationException) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<NbtCompound>> getUpdatedChunkNbt(ChunkPos chunkPos) {
        return this.getNbt(chunkPos).thenApplyAsync(nbt -> nbt.map(this::updateChunkNbt), (Executor)Util.getMainWorkerExecutor());
    }

    private NbtCompound updateChunkNbt(NbtCompound nbt) {
        return this.updateChunkNbt(this.world.getRegistryKey(), this.persistentStateManagerFactory, nbt, this.getChunkGenerator().getCodecKey());
    }

    boolean shouldTick(ChunkPos pos) {
        if (!this.ticketManager.shouldTick(pos.toLong())) {
            return false;
        }
        for (ServerPlayerEntity serverPlayerEntity : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!this.canTickChunk(serverPlayerEntity, pos)) continue;
            return true;
        }
        return false;
    }

    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos pos) {
        long l = pos.toLong();
        if (!this.ticketManager.shouldTick(l)) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity serverPlayerEntity : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            if (!this.canTickChunk(serverPlayerEntity, pos)) continue;
            builder.add(serverPlayerEntity);
        }
        return builder.build();
    }

    /**
     * {@return whether the {@code player} can tick the chunk at {@code pos}}
     * 
     * @implNote Spectators cannot tick chunks. Additionally, only chunks within 128
     * block radius of that player can be ticked.
     * 
     * @apiNote This controls monster spawning and block random ticks.
     */
    private boolean canTickChunk(ServerPlayerEntity player, ChunkPos pos) {
        if (player.isSpectator()) {
            return false;
        }
        double d = ServerChunkLoadingManager.getSquaredDistance(pos, player);
        return d < 16384.0;
    }

    private boolean doesNotGenerateChunks(ServerPlayerEntity player) {
        return player.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.SPECTATORS_GENERATE_CHUNKS);
    }

    void handlePlayerAddedOrRemoved(ServerPlayerEntity player, boolean added) {
        boolean bl = this.doesNotGenerateChunks(player);
        boolean bl2 = this.playerChunkWatchingManager.isWatchInactive(player);
        if (added) {
            this.playerChunkWatchingManager.add(player, bl);
            this.updateWatchedSection(player);
            if (!bl) {
                this.ticketManager.handleChunkEnter(ChunkSectionPos.from(player), player);
            }
            player.setChunkFilter(ChunkFilter.IGNORE_ALL);
            this.sendWatchPackets(player);
        } else {
            ChunkSectionPos chunkSectionPos = player.getWatchedSection();
            this.playerChunkWatchingManager.remove(player);
            if (!bl2) {
                this.ticketManager.handleChunkLeave(chunkSectionPos, player);
            }
            this.sendWatchPackets(player, ChunkFilter.IGNORE_ALL);
        }
    }

    /**
     * Updates the watched chunk section position for the {@code player}, and sends a
     * render distance update packet to the client.
     */
    private void updateWatchedSection(ServerPlayerEntity player) {
        ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(player);
        player.setWatchedSection(chunkSectionPos);
    }

    /**
     * Updates the chunk section position of the {@code player}. This updates the player
     * position for both entity tracking and chunk loading (watching) logic.
     * 
     * @see ServerChunkManager#updatePosition(ServerPlayerEntity)
     */
    public void updatePosition(ServerPlayerEntity player) {
        boolean bl3;
        for (EntityTracker entityTracker : this.entityTrackers.values()) {
            if (entityTracker.entity == player) {
                entityTracker.updateTrackedStatus(this.world.getPlayers());
                continue;
            }
            entityTracker.updateTrackedStatus(player);
        }
        ChunkSectionPos chunkSectionPos = player.getWatchedSection();
        ChunkSectionPos chunkSectionPos2 = ChunkSectionPos.from(player);
        boolean bl = this.playerChunkWatchingManager.isWatchDisabled(player);
        boolean bl2 = this.doesNotGenerateChunks(player);
        boolean bl4 = bl3 = chunkSectionPos.asLong() != chunkSectionPos2.asLong();
        if (bl3 || bl != bl2) {
            this.updateWatchedSection(player);
            if (!bl) {
                this.ticketManager.handleChunkLeave(chunkSectionPos, player);
            }
            if (!bl2) {
                this.ticketManager.handleChunkEnter(chunkSectionPos2, player);
            }
            if (!bl && bl2) {
                this.playerChunkWatchingManager.disableWatch(player);
            }
            if (bl && !bl2) {
                this.playerChunkWatchingManager.enableWatch(player);
            }
            this.sendWatchPackets(player);
        }
    }

    private void sendWatchPackets(ServerPlayerEntity player) {
        ChunkFilter.Cylindrical cylindrical;
        ChunkPos chunkPos = player.getChunkPos();
        int i = this.getViewDistance(player);
        ChunkFilter chunkFilter = player.getChunkFilter();
        if (chunkFilter instanceof ChunkFilter.Cylindrical && (cylindrical = (ChunkFilter.Cylindrical)chunkFilter).center().equals(chunkPos) && cylindrical.viewDistance() == i) {
            return;
        }
        this.sendWatchPackets(player, ChunkFilter.cylindrical(chunkPos, i));
    }

    private void sendWatchPackets(ServerPlayerEntity player, ChunkFilter chunkFilter) {
        if (player.getWorld() != this.world) {
            return;
        }
        ChunkFilter chunkFilter2 = player.getChunkFilter();
        if (chunkFilter instanceof ChunkFilter.Cylindrical) {
            ChunkFilter.Cylindrical cylindrical2;
            ChunkFilter.Cylindrical cylindrical = (ChunkFilter.Cylindrical)chunkFilter;
            if (!(chunkFilter2 instanceof ChunkFilter.Cylindrical) || !(cylindrical2 = (ChunkFilter.Cylindrical)chunkFilter2).center().equals(cylindrical.center())) {
                player.networkHandler.sendPacket(new ChunkRenderDistanceCenterS2CPacket(cylindrical.center().x, cylindrical.center().z));
            }
        }
        ChunkFilter.forEachChangedChunk(chunkFilter2, chunkFilter, chunkPos -> this.track(player, (ChunkPos)chunkPos), chunkPos -> ServerChunkLoadingManager.untrack(player, chunkPos));
        player.setChunkFilter(chunkFilter);
    }

    @Override
    public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos chunkPos, boolean onlyOnWatchDistanceEdge) {
        Set<ServerPlayerEntity> set = this.playerChunkWatchingManager.getPlayersWatchingChunk();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (ServerPlayerEntity serverPlayerEntity : set) {
            if ((!onlyOnWatchDistanceEdge || !this.isOnTrackEdge(serverPlayerEntity, chunkPos.x, chunkPos.z)) && (onlyOnWatchDistanceEdge || !this.isTracked(serverPlayerEntity, chunkPos.x, chunkPos.z))) continue;
            builder.add(serverPlayerEntity);
        }
        return builder.build();
    }

    protected void loadEntity(Entity entity) {
        if (entity instanceof EnderDragonPart) {
            return;
        }
        EntityType<?> entityType = entity.getType();
        int i = entityType.getMaxTrackDistance() * 16;
        if (i == 0) {
            return;
        }
        int j = entityType.getTrackTickInterval();
        if (this.entityTrackers.containsKey(entity.getId())) {
            throw Util.throwOrPause(new IllegalStateException("Entity is already tracked!"));
        }
        EntityTracker entityTracker = new EntityTracker(entity, i, j, entityType.alwaysUpdateVelocity());
        this.entityTrackers.put(entity.getId(), entityTracker);
        entityTracker.updateTrackedStatus(this.world.getPlayers());
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(serverPlayerEntity, true);
            for (EntityTracker entityTracker2 : this.entityTrackers.values()) {
                if (entityTracker2.entity == serverPlayerEntity) continue;
                entityTracker2.updateTrackedStatus(serverPlayerEntity);
            }
        }
    }

    protected void unloadEntity(Entity entity) {
        EntityTracker entityTracker2;
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            this.handlePlayerAddedOrRemoved(serverPlayerEntity, false);
            for (EntityTracker entityTracker : this.entityTrackers.values()) {
                entityTracker.stopTracking(serverPlayerEntity);
            }
        }
        if ((entityTracker2 = (EntityTracker)this.entityTrackers.remove(entity.getId())) != null) {
            entityTracker2.stopTracking();
        }
    }

    /**
     * Ticks and updates the tracked status of each tracker.
     * 
     * <p>This first checks if entities have changed chunk sections, and updates
     * tracking status of those entities to all players. It then checks if any player
     * has changed chunk sections, and updates all entities tracking status to those
     * players. This ensures all possible updates are accounted for.
     */
    protected void tickEntityMovement() {
        for (ServerPlayerEntity serverPlayerEntity : this.playerChunkWatchingManager.getPlayersWatchingChunk()) {
            this.sendWatchPackets(serverPlayerEntity);
        }
        ArrayList<ServerPlayerEntity> list = Lists.newArrayList();
        List<ServerPlayerEntity> list2 = this.world.getPlayers();
        for (EntityTracker entityTracker : this.entityTrackers.values()) {
            boolean bl;
            ChunkSectionPos chunkSectionPos = entityTracker.trackedSection;
            ChunkSectionPos chunkSectionPos2 = ChunkSectionPos.from(entityTracker.entity);
            boolean bl2 = bl = !Objects.equals(chunkSectionPos, chunkSectionPos2);
            if (bl) {
                entityTracker.updateTrackedStatus(list2);
                Entity entity = entityTracker.entity;
                if (entity instanceof ServerPlayerEntity) {
                    list.add((ServerPlayerEntity)entity);
                }
                entityTracker.trackedSection = chunkSectionPos2;
            }
            if (!bl && !this.ticketManager.shouldTickEntities(chunkSectionPos2.toChunkPos().toLong())) continue;
            entityTracker.entry.tick();
        }
        if (!list.isEmpty()) {
            for (EntityTracker entityTracker : this.entityTrackers.values()) {
                entityTracker.updateTrackedStatus(list);
            }
        }
    }

    public void sendToOtherNearbyPlayers(Entity entity, Packet<?> packet) {
        EntityTracker entityTracker = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (entityTracker != null) {
            entityTracker.sendToOtherNearbyPlayers(packet);
        }
    }

    protected void sendToNearbyPlayers(Entity entity, Packet<?> packet) {
        EntityTracker entityTracker = (EntityTracker)this.entityTrackers.get(entity.getId());
        if (entityTracker != null) {
            entityTracker.sendToNearbyPlayers(packet);
        }
    }

    public void sendChunkBiomePackets(List<Chunk> chunks) {
        HashMap<ServerPlayerEntity, List> map = new HashMap<ServerPlayerEntity, List>();
        for (Chunk chunk : chunks) {
            WorldChunk worldChunk;
            ChunkPos chunkPos = chunk.getPos();
            WorldChunk worldChunk2 = chunk instanceof WorldChunk ? (worldChunk = (WorldChunk)chunk) : this.world.getChunk(chunkPos.x, chunkPos.z);
            for (ServerPlayerEntity serverPlayerEntity : this.getPlayersWatchingChunk(chunkPos, false)) {
                map.computeIfAbsent(serverPlayerEntity, player -> new ArrayList()).add(worldChunk2);
            }
        }
        map.forEach((player, chunksx) -> player.networkHandler.sendPacket(ChunkBiomeDataS2CPacket.create(chunksx)));
    }

    protected PointOfInterestStorage getPointOfInterestStorage() {
        return this.pointOfInterestStorage;
    }

    public String getSaveDir() {
        return this.saveDir;
    }

    void onChunkStatusChange(ChunkPos chunkPos, ChunkLevelType levelType) {
        this.chunkStatusChangeListener.onChunkStatusChange(chunkPos, levelType);
    }

    public void forceLighting(ChunkPos centerPos, int radius) {
        int i = radius + 1;
        ChunkPos.stream(centerPos, i).forEach(pos -> {
            ChunkHolder chunkHolder = this.getChunkHolder(pos.toLong());
            if (chunkHolder != null) {
                chunkHolder.combinePostProcessingFuture(this.lightingProvider.enqueue(pos.x, pos.z));
            }
        });
    }

    class TicketManager
    extends ChunkTicketManager {
        protected TicketManager(Executor workerExecutor, Executor mainThreadExecutor) {
            super(workerExecutor, mainThreadExecutor);
        }

        @Override
        protected boolean isUnloaded(long pos) {
            return ServerChunkLoadingManager.this.unloadedChunks.contains(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder getChunkHolder(long pos) {
            return ServerChunkLoadingManager.this.getCurrentChunkHolder(pos);
        }

        @Override
        @Nullable
        protected ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
            return ServerChunkLoadingManager.this.setLevel(pos, level, holder, i);
        }
    }

    class EntityTracker {
        final EntityTrackerEntry entry;
        final Entity entity;
        private final int maxDistance;
        ChunkSectionPos trackedSection;
        private final Set<PlayerAssociatedNetworkHandler> listeners = Sets.newIdentityHashSet();

        public EntityTracker(Entity entity, int maxDistance, int tickInterval, boolean alwaysUpdateVelocity) {
            this.entry = new EntityTrackerEntry(ServerChunkLoadingManager.this.world, entity, tickInterval, alwaysUpdateVelocity, this::sendToOtherNearbyPlayers);
            this.entity = entity;
            this.maxDistance = maxDistance;
            this.trackedSection = ChunkSectionPos.from(entity);
        }

        public boolean equals(Object o) {
            if (o instanceof EntityTracker) {
                return ((EntityTracker)o).entity.getId() == this.entity.getId();
            }
            return false;
        }

        public int hashCode() {
            return this.entity.getId();
        }

        public void sendToOtherNearbyPlayers(Packet<?> packet) {
            for (PlayerAssociatedNetworkHandler playerAssociatedNetworkHandler : this.listeners) {
                playerAssociatedNetworkHandler.sendPacket(packet);
            }
        }

        public void sendToNearbyPlayers(Packet<?> packet) {
            this.sendToOtherNearbyPlayers(packet);
            if (this.entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
            }
        }

        public void stopTracking() {
            for (PlayerAssociatedNetworkHandler playerAssociatedNetworkHandler : this.listeners) {
                this.entry.stopTracking(playerAssociatedNetworkHandler.getPlayer());
            }
        }

        public void stopTracking(ServerPlayerEntity player) {
            if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
            }
        }

        public void updateTrackedStatus(ServerPlayerEntity player) {
            boolean bl;
            if (player == this.entity) {
                return;
            }
            Vec3d vec3d = player.getPos().subtract(this.entity.getPos());
            int i = ServerChunkLoadingManager.this.getViewDistance(player);
            double e = vec3d.x * vec3d.x + vec3d.z * vec3d.z;
            double d = Math.min(this.getMaxTrackDistance(), i * 16);
            double f = d * d;
            boolean bl2 = bl = e <= f && this.entity.canBeSpectated(player) && ServerChunkLoadingManager.this.isTracked(player, this.entity.getChunkPos().x, this.entity.getChunkPos().z);
            if (bl) {
                if (this.listeners.add(player.networkHandler)) {
                    this.entry.startTracking(player);
                }
            } else if (this.listeners.remove(player.networkHandler)) {
                this.entry.stopTracking(player);
            }
        }

        private int adjustTrackingDistance(int initialDistance) {
            return ServerChunkLoadingManager.this.world.getServer().adjustTrackingDistance(initialDistance);
        }

        private int getMaxTrackDistance() {
            int i = this.maxDistance;
            for (Entity entity : this.entity.getPassengersDeep()) {
                int j = entity.getType().getMaxTrackDistance() * 16;
                if (j <= i) continue;
                i = j;
            }
            return this.adjustTrackingDistance(i);
        }

        public void updateTrackedStatus(List<ServerPlayerEntity> players) {
            for (ServerPlayerEntity serverPlayerEntity : players) {
                this.updateTrackedStatus(serverPlayerEntity);
            }
        }
    }
}

