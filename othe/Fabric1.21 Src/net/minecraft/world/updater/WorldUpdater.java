/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.updater;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.ChunkPosKeyedStorage;
import net.minecraft.world.storage.RecreatedChunkStorage;
import net.minecraft.world.storage.RecreationStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageKey;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class WorldUpdater {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory UPDATE_THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private static final String NEW_PREFIX = "new_";
    static final MutableText UPGRADING_POI_TEXT = Text.translatable("optimizeWorld.stage.upgrading.poi");
    static final MutableText FINISHED_POI_TEXT = Text.translatable("optimizeWorld.stage.finished.poi");
    static final MutableText UPGRADING_ENTITIES_TEXT = Text.translatable("optimizeWorld.stage.upgrading.entities");
    static final MutableText FINISHED_ENTITIES_TEXT = Text.translatable("optimizeWorld.stage.finished.entities");
    static final MutableText UPGRADING_CHUNKS_TEXT = Text.translatable("optimizeWorld.stage.upgrading.chunks");
    static final MutableText FINISHED_CHUNKS_TEXT = Text.translatable("optimizeWorld.stage.finished.chunks");
    final Registry<DimensionOptions> dimensionOptionsRegistry;
    final Set<RegistryKey<World>> worldKeys;
    final boolean eraseCache;
    final boolean recreateRegionFiles;
    final LevelStorage.Session session;
    private final Thread updateThread;
    final DataFixer dataFixer;
    volatile boolean keepUpgradingChunks = true;
    private volatile boolean done;
    volatile float progress;
    volatile int totalChunkCount;
    volatile int totalRegionCount;
    volatile int upgradedChunkCount;
    volatile int skippedChunkCount;
    final Reference2FloatMap<RegistryKey<World>> dimensionProgress = Reference2FloatMaps.synchronize(new Reference2FloatOpenHashMap());
    volatile Text status = Text.translatable("optimizeWorld.stage.counting");
    static final Pattern REGION_FILE_PATTERN = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    final PersistentStateManager persistentStateManager;

    public WorldUpdater(LevelStorage.Session session, DataFixer dataFixer, DynamicRegistryManager dynamicRegistryManager, boolean eraseCache, boolean recreateRegionFiles) {
        this.dimensionOptionsRegistry = dynamicRegistryManager.get(RegistryKeys.DIMENSION);
        this.worldKeys = this.dimensionOptionsRegistry.getKeys().stream().map(RegistryKeys::toWorldKey).collect(Collectors.toUnmodifiableSet());
        this.eraseCache = eraseCache;
        this.dataFixer = dataFixer;
        this.session = session;
        this.persistentStateManager = new PersistentStateManager(this.session.getWorldDirectory(World.OVERWORLD).resolve("data").toFile(), dataFixer, dynamicRegistryManager);
        this.recreateRegionFiles = recreateRegionFiles;
        this.updateThread = UPDATE_THREAD_FACTORY.newThread(this::updateWorld);
        this.updateThread.setUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.error("Error upgrading world", throwable);
            this.status = Text.translatable("optimizeWorld.stage.failed");
            this.done = true;
        });
        this.updateThread.start();
    }

    public void cancel() {
        this.keepUpgradingChunks = false;
        try {
            this.updateThread.join();
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    private void updateWorld() {
        long l = Util.getMeasuringTimeMs();
        LOGGER.info("Upgrading entities");
        new EntitiesUpdate(this).update();
        LOGGER.info("Upgrading POIs");
        new PoiUpdate(this).update();
        LOGGER.info("Upgrading blocks");
        new RegionUpdate().update();
        this.persistentStateManager.save();
        l = Util.getMeasuringTimeMs() - l;
        LOGGER.info("World optimizaton finished after {} seconds", (Object)(l / 1000L));
        this.done = true;
    }

    public boolean isDone() {
        return this.done;
    }

    public Set<RegistryKey<World>> getWorlds() {
        return this.worldKeys;
    }

    public float getProgress(RegistryKey<World> world) {
        return this.dimensionProgress.getFloat(world);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunkCount() {
        return this.totalChunkCount;
    }

    public int getUpgradedChunkCount() {
        return this.upgradedChunkCount;
    }

    public int getSkippedChunkCount() {
        return this.skippedChunkCount;
    }

    public Text getStatus() {
        return this.status;
    }

    static Path getNewDirectoryPath(Path current) {
        return current.resolveSibling(NEW_PREFIX + current.getFileName().toString());
    }

    class EntitiesUpdate
    extends ChunkPosKeyedStorageUpdate {
        EntitiesUpdate(WorldUpdater worldUpdater) {
            super(DataFixTypes.ENTITY_CHUNK, "entities", UPGRADING_ENTITIES_TEXT, FINISHED_ENTITIES_TEXT);
        }

        @Override
        protected NbtCompound updateNbt(ChunkPosKeyedStorage storage, NbtCompound nbt) {
            return storage.update(nbt, -1);
        }
    }

    class PoiUpdate
    extends ChunkPosKeyedStorageUpdate {
        PoiUpdate(WorldUpdater worldUpdater) {
            super(DataFixTypes.POI_CHUNK, "poi", UPGRADING_POI_TEXT, FINISHED_POI_TEXT);
        }

        @Override
        protected NbtCompound updateNbt(ChunkPosKeyedStorage storage, NbtCompound nbt) {
            return storage.update(nbt, 1945);
        }
    }

    class RegionUpdate
    extends Update<VersionedChunkStorage> {
        RegionUpdate() {
            super(DataFixTypes.CHUNK, "chunk", "region", UPGRADING_CHUNKS_TEXT, FINISHED_CHUNKS_TEXT);
        }

        @Override
        protected boolean update(VersionedChunkStorage versionedChunkStorage, ChunkPos chunkPos, RegistryKey<World> registryKey) {
            NbtCompound nbtCompound = versionedChunkStorage.getNbt(chunkPos).join().orElse(null);
            if (nbtCompound != null) {
                boolean bl;
                int i = VersionedChunkStorage.getDataVersion(nbtCompound);
                ChunkGenerator chunkGenerator = WorldUpdater.this.dimensionOptionsRegistry.getOrThrow(RegistryKeys.toDimensionKey(registryKey)).chunkGenerator();
                NbtCompound nbtCompound2 = versionedChunkStorage.updateChunkNbt(registryKey, () -> WorldUpdater.this.persistentStateManager, nbtCompound, chunkGenerator.getCodecKey());
                ChunkPos chunkPos2 = new ChunkPos(nbtCompound2.getInt("xPos"), nbtCompound2.getInt("zPos"));
                if (!chunkPos2.equals(chunkPos)) {
                    LOGGER.warn("Chunk {} has invalid position {}", (Object)chunkPos, (Object)chunkPos2);
                }
                boolean bl2 = bl = i < SharedConstants.getGameVersion().getSaveVersion().getId();
                if (WorldUpdater.this.eraseCache) {
                    bl = bl || nbtCompound2.contains("Heightmaps");
                    nbtCompound2.remove("Heightmaps");
                    bl = bl || nbtCompound2.contains("isLightOn");
                    nbtCompound2.remove("isLightOn");
                    NbtList nbtList = nbtCompound2.getList("sections", NbtElement.COMPOUND_TYPE);
                    for (int j = 0; j < nbtList.size(); ++j) {
                        NbtCompound nbtCompound3 = nbtList.getCompound(j);
                        bl = bl || nbtCompound3.contains("BlockLight");
                        nbtCompound3.remove("BlockLight");
                        bl = bl || nbtCompound3.contains("SkyLight");
                        nbtCompound3.remove("SkyLight");
                    }
                }
                if (bl || WorldUpdater.this.recreateRegionFiles) {
                    if (this.pendingUpdateFuture != null) {
                        this.pendingUpdateFuture.join();
                    }
                    this.pendingUpdateFuture = versionedChunkStorage.setNbt(chunkPos, nbtCompound2);
                    return true;
                }
            }
            return false;
        }

        @Override
        protected VersionedChunkStorage openStorage(StorageKey storageKey, Path path) {
            return WorldUpdater.this.recreateRegionFiles ? new RecreatedChunkStorage(storageKey.withSuffix("source"), path, storageKey.withSuffix("target"), WorldUpdater.getNewDirectoryPath(path), WorldUpdater.this.dataFixer, true) : new VersionedChunkStorage(storageKey, path, WorldUpdater.this.dataFixer, true);
        }

        @Override
        protected /* synthetic */ AutoCloseable openStorage(StorageKey key, Path worldDirectory) {
            return this.openStorage(key, worldDirectory);
        }
    }

    abstract class ChunkPosKeyedStorageUpdate
    extends Update<ChunkPosKeyedStorage> {
        ChunkPosKeyedStorageUpdate(DataFixTypes dataFixTypes, String targetName, MutableText upgradingText, MutableText finishedText) {
            super(dataFixTypes, targetName, targetName, upgradingText, finishedText);
        }

        @Override
        protected ChunkPosKeyedStorage openStorage(StorageKey storageKey, Path path) {
            return WorldUpdater.this.recreateRegionFiles ? new RecreationStorage(storageKey.withSuffix("source"), path, storageKey.withSuffix("target"), WorldUpdater.getNewDirectoryPath(path), WorldUpdater.this.dataFixer, true, this.dataFixTypes) : new ChunkPosKeyedStorage(storageKey, path, WorldUpdater.this.dataFixer, true, this.dataFixTypes);
        }

        @Override
        protected boolean update(ChunkPosKeyedStorage chunkPosKeyedStorage, ChunkPos chunkPos, RegistryKey<World> registryKey) {
            NbtCompound nbtCompound = chunkPosKeyedStorage.read(chunkPos).join().orElse(null);
            if (nbtCompound != null) {
                boolean bl;
                int i = VersionedChunkStorage.getDataVersion(nbtCompound);
                NbtCompound nbtCompound2 = this.updateNbt(chunkPosKeyedStorage, nbtCompound);
                boolean bl2 = bl = i < SharedConstants.getGameVersion().getSaveVersion().getId();
                if (bl || WorldUpdater.this.recreateRegionFiles) {
                    if (this.pendingUpdateFuture != null) {
                        this.pendingUpdateFuture.join();
                    }
                    this.pendingUpdateFuture = chunkPosKeyedStorage.set(chunkPos, nbtCompound2);
                    return true;
                }
            }
            return false;
        }

        protected abstract NbtCompound updateNbt(ChunkPosKeyedStorage var1, NbtCompound var2);

        @Override
        protected /* synthetic */ AutoCloseable openStorage(StorageKey key, Path worldDirectory) {
            return this.openStorage(key, worldDirectory);
        }
    }

    abstract class Update<T extends AutoCloseable> {
        private final MutableText upgradingText;
        private final MutableText finishedText;
        private final String name;
        private final String targetName;
        @Nullable
        protected CompletableFuture<Void> pendingUpdateFuture;
        protected final DataFixTypes dataFixTypes;

        Update(DataFixTypes dataFixTypes, String name, String targetName, MutableText upgradingText, MutableText finishedText) {
            this.dataFixTypes = dataFixTypes;
            this.name = name;
            this.targetName = targetName;
            this.upgradingText = upgradingText;
            this.finishedText = finishedText;
        }

        public void update() {
            WorldUpdater.this.totalRegionCount = 0;
            WorldUpdater.this.totalChunkCount = 0;
            WorldUpdater.this.upgradedChunkCount = 0;
            WorldUpdater.this.skippedChunkCount = 0;
            List<WorldData<T>> list = this.listWoldData();
            if (WorldUpdater.this.totalChunkCount == 0) {
                return;
            }
            float f = WorldUpdater.this.totalRegionCount;
            WorldUpdater.this.status = this.upgradingText;
            while (WorldUpdater.this.keepUpgradingChunks) {
                boolean bl = false;
                float g = 0.0f;
                for (WorldData<T> worldData : list) {
                    RegistryKey<World> registryKey = worldData.dimensionKey;
                    ListIterator<Region> listIterator = worldData.files;
                    AutoCloseable autoCloseable = (AutoCloseable)worldData.storage;
                    if (listIterator.hasNext()) {
                        Region region = listIterator.next();
                        boolean bl2 = true;
                        for (ChunkPos chunkPos : region.chunksToUpgrade) {
                            bl2 = bl2 && this.update(registryKey, autoCloseable, chunkPos);
                            bl = true;
                        }
                        if (WorldUpdater.this.recreateRegionFiles) {
                            if (bl2) {
                                this.recreate(region.file);
                            } else {
                                LOGGER.error("Failed to convert region file {}", (Object)region.file.getPath());
                            }
                        }
                    }
                    float h = (float)listIterator.nextIndex() / f;
                    WorldUpdater.this.dimensionProgress.put(registryKey, h);
                    g += h;
                }
                WorldUpdater.this.progress = g;
                if (bl) continue;
                break;
            }
            WorldUpdater.this.status = this.finishedText;
            for (WorldData<T> worldData2 : list) {
                try {
                    ((AutoCloseable)worldData2.storage).close();
                } catch (Exception exception) {
                    LOGGER.error("Error upgrading chunk", exception);
                }
            }
        }

        private List<WorldData<T>> listWoldData() {
            ArrayList<WorldData<T>> list = Lists.newArrayList();
            for (RegistryKey<World> registryKey : WorldUpdater.this.worldKeys) {
                StorageKey storageKey = new StorageKey(WorldUpdater.this.session.getDirectoryName(), registryKey, this.name);
                Path path = WorldUpdater.this.session.getWorldDirectory(registryKey).resolve(this.targetName);
                T autoCloseable = this.openStorage(storageKey, path);
                ListIterator<Region> listIterator = this.enumerateRegions(storageKey, path);
                list.add(new WorldData<T>(registryKey, autoCloseable, listIterator));
            }
            return list;
        }

        protected abstract T openStorage(StorageKey var1, Path var2);

        private ListIterator<Region> enumerateRegions(StorageKey key, Path regionDirectory) {
            List<Region> list = Update.listRegions(key, regionDirectory);
            WorldUpdater.this.totalRegionCount += list.size();
            WorldUpdater.this.totalChunkCount += list.stream().mapToInt(region -> region.chunksToUpgrade.size()).sum();
            return list.listIterator();
        }

        private static List<Region> listRegions(StorageKey key, Path regionDirectory) {
            File[] files = regionDirectory.toFile().listFiles((file, name) -> name.endsWith(".mca"));
            if (files == null) {
                return List.of();
            }
            ArrayList<Region> list = Lists.newArrayList();
            for (File file2 : files) {
                Matcher matcher = REGION_FILE_PATTERN.matcher(file2.getName());
                if (!matcher.matches()) continue;
                int i = Integer.parseInt(matcher.group(1)) << 5;
                int j = Integer.parseInt(matcher.group(2)) << 5;
                ArrayList<ChunkPos> list2 = Lists.newArrayList();
                try (RegionFile regionFile = new RegionFile(key, file2.toPath(), regionDirectory, true);){
                    for (int k = 0; k < 32; ++k) {
                        for (int l = 0; l < 32; ++l) {
                            ChunkPos chunkPos = new ChunkPos(k + i, l + j);
                            if (!regionFile.isChunkValid(chunkPos)) continue;
                            list2.add(chunkPos);
                        }
                    }
                    if (list2.isEmpty()) continue;
                    list.add(new Region(regionFile, list2));
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to read chunks from region file {}", (Object)file2.toPath(), (Object)throwable);
                }
            }
            return list;
        }

        private boolean update(RegistryKey<World> worldKey, T storage, ChunkPos chunkPos) {
            boolean bl = false;
            try {
                bl = this.update(storage, chunkPos, worldKey);
            } catch (CompletionException | CrashException runtimeException) {
                Throwable throwable = runtimeException.getCause();
                if (throwable instanceof IOException) {
                    LOGGER.error("Error upgrading chunk {}", (Object)chunkPos, (Object)throwable);
                }
                throw runtimeException;
            }
            if (bl) {
                ++WorldUpdater.this.upgradedChunkCount;
            } else {
                ++WorldUpdater.this.skippedChunkCount;
            }
            return bl;
        }

        protected abstract boolean update(T var1, ChunkPos var2, RegistryKey<World> var3);

        private void recreate(RegionFile regionFile) {
            if (!WorldUpdater.this.recreateRegionFiles) {
                return;
            }
            if (this.pendingUpdateFuture != null) {
                this.pendingUpdateFuture.join();
            }
            Path path = regionFile.getPath();
            Path path2 = path.getParent();
            Path path3 = WorldUpdater.getNewDirectoryPath(path2).resolve(path.getFileName().toString());
            try {
                if (path3.toFile().exists()) {
                    Files.delete(path);
                    Files.move(path3, path, new CopyOption[0]);
                } else {
                    LOGGER.error("Failed to replace an old region file. New file {} does not exist.", (Object)path3);
                }
            } catch (IOException iOException) {
                LOGGER.error("Failed to replace an old region file", iOException);
            }
        }
    }

    record Region(RegionFile file, List<ChunkPos> chunksToUpgrade) {
    }

    record WorldData<T>(RegistryKey<World> dimensionKey, T storage, ListIterator<Region> files) {
    }
}

