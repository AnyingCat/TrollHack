/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.InvalidNbtException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.scanner.ExclusiveNbtCollector;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.SaveLoading;
import net.minecraft.text.Text;
import net.minecraft.util.DateTimeFormatters;
import net.minecraft.util.Identifier;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashMemoryReserve;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.path.AllowedSymlinkPathMatcher;
import net.minecraft.util.path.SymlinkEntry;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ParsedSaveProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.level.storage.SessionLock;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LevelStorage {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatters.create();
    private static final String DATA_KEY = "Data";
    private static final PathMatcher DEFAULT_ALLOWED_SYMLINK_MATCHER = path -> false;
    public static final String ALLOWED_SYMLINKS_FILE_NAME = "allowed_symlinks.txt";
    private static final int MAX_LEVEL_DATA_SIZE = 0x6400000;
    private static final int RECOMMENDED_USABLE_SPACE_BYTES = 0x4000000;
    private final Path savesDirectory;
    private final Path backupsDirectory;
    final DataFixer dataFixer;
    private final SymlinkFinder symlinkFinder;

    public LevelStorage(Path savesDirectory, Path backupsDirectory, SymlinkFinder symlinkFinder, DataFixer dataFixer) {
        this.dataFixer = dataFixer;
        try {
            PathUtil.createDirectories(savesDirectory);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
        this.savesDirectory = savesDirectory;
        this.backupsDirectory = backupsDirectory;
        this.symlinkFinder = symlinkFinder;
    }

    public static SymlinkFinder createSymlinkFinder(Path allowedSymlinksFile) {
        if (Files.exists(allowedSymlinksFile, new LinkOption[0])) {
            SymlinkFinder symlinkFinder;
            block9: {
                BufferedReader bufferedReader = Files.newBufferedReader(allowedSymlinksFile);
                try {
                    symlinkFinder = new SymlinkFinder(AllowedSymlinkPathMatcher.fromReader(bufferedReader));
                    if (bufferedReader == null) break block9;
                } catch (Throwable throwable) {
                    try {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    } catch (Exception exception) {
                        LOGGER.error("Failed to parse {}, disallowing all symbolic links", (Object)ALLOWED_SYMLINKS_FILE_NAME, (Object)exception);
                    }
                }
                bufferedReader.close();
            }
            return symlinkFinder;
        }
        return new SymlinkFinder(DEFAULT_ALLOWED_SYMLINK_MATCHER);
    }

    public static LevelStorage create(Path path) {
        SymlinkFinder symlinkFinder = LevelStorage.createSymlinkFinder(path.resolve(ALLOWED_SYMLINKS_FILE_NAME));
        return new LevelStorage(path, path.resolve("../backups"), symlinkFinder, Schemas.getFixer());
    }

    public static DataConfiguration parseDataPackSettings(Dynamic<?> dynamic) {
        return DataConfiguration.CODEC.parse(dynamic).resultOrPartial(LOGGER::error).orElse(DataConfiguration.SAFE_MODE);
    }

    public static SaveLoading.DataPacks parseDataPacks(Dynamic<?> dynamic, ResourcePackManager dataPackManager, boolean safeMode) {
        return new SaveLoading.DataPacks(dataPackManager, LevelStorage.parseDataPackSettings(dynamic), safeMode, false);
    }

    public static ParsedSaveProperties parseSaveProperties(Dynamic<?> dynamic, DataConfiguration dataConfiguration, Registry<DimensionOptions> dimensionsRegistry, DynamicRegistryManager.Immutable registryManager) {
        Dynamic<?> dynamic2 = RegistryOps.withRegistry(dynamic, registryManager);
        Dynamic<?> dynamic3 = dynamic2.get("WorldGenSettings").orElseEmptyMap();
        WorldGenSettings worldGenSettings = (WorldGenSettings)WorldGenSettings.CODEC.parse(dynamic3).getOrThrow();
        LevelInfo levelInfo = LevelInfo.fromDynamic(dynamic2, dataConfiguration);
        DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = worldGenSettings.dimensionOptionsRegistryHolder().toConfig(dimensionsRegistry);
        Lifecycle lifecycle = dimensionsConfig.getLifecycle().add(registryManager.getRegistryLifecycle());
        LevelProperties levelProperties = LevelProperties.readProperties(dynamic2, levelInfo, dimensionsConfig.specialWorldProperty(), worldGenSettings.generatorOptions(), lifecycle);
        return new ParsedSaveProperties(levelProperties, dimensionsConfig);
    }

    public String getFormatName() {
        return "Anvil";
    }

    public LevelList getLevelList() throws LevelStorageException {
        LevelList levelList;
        block9: {
            if (!Files.isDirectory(this.savesDirectory, new LinkOption[0])) {
                throw new LevelStorageException(Text.translatable("selectWorld.load_folder_access"));
            }
            Stream<Path> stream = Files.list(this.savesDirectory);
            try {
                List<LevelSave> list = stream.filter(path -> Files.isDirectory(path, new LinkOption[0])).map(LevelSave::new).filter(levelSave -> Files.isRegularFile(levelSave.getLevelDatPath(), new LinkOption[0]) || Files.isRegularFile(levelSave.getLevelDatOldPath(), new LinkOption[0])).toList();
                levelList = new LevelList(list);
                if (stream == null) break block9;
            } catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    throw new LevelStorageException(Text.translatable("selectWorld.load_folder_access"));
                }
            }
            stream.close();
        }
        return levelList;
    }

    public CompletableFuture<List<LevelSummary>> loadSummaries(LevelList levels) {
        ArrayList<CompletableFuture<LevelSummary>> list = new ArrayList<CompletableFuture<LevelSummary>>(levels.levels.size());
        for (LevelSave levelSave : levels.levels) {
            list.add(CompletableFuture.supplyAsync(() -> {
                boolean bl;
                try {
                    bl = SessionLock.isLocked(levelSave.path());
                } catch (Exception exception) {
                    LOGGER.warn("Failed to read {} lock", (Object)levelSave.path(), (Object)exception);
                    return null;
                }
                try {
                    return this.readSummary(levelSave, bl);
                } catch (OutOfMemoryError outOfMemoryError) {
                    CrashMemoryReserve.releaseMemory();
                    System.gc();
                    String string = "Ran out of memory trying to read summary of world folder \"" + levelSave.getRootPath() + "\"";
                    LOGGER.error(LogUtils.FATAL_MARKER, string);
                    OutOfMemoryError outOfMemoryError2 = new OutOfMemoryError("Ran out of memory reading level data");
                    outOfMemoryError2.initCause(outOfMemoryError);
                    CrashReport crashReport = CrashReport.create(outOfMemoryError2, string);
                    CrashReportSection crashReportSection = crashReport.addElement("World details");
                    crashReportSection.add("Folder Name", levelSave.getRootPath());
                    try {
                        long l = Files.size(levelSave.getLevelDatPath());
                        crashReportSection.add("level.dat size", l);
                    } catch (IOException iOException) {
                        crashReportSection.add("level.dat size", iOException);
                    }
                    throw new CrashException(crashReport);
                }
            }, Util.getMainWorkerExecutor()));
        }
        return Util.combineCancellable(list).thenApply(summaries -> summaries.stream().filter(Objects::nonNull).sorted().toList());
    }

    private int getCurrentVersion() {
        return 19133;
    }

    static NbtCompound readLevelProperties(Path path) throws IOException {
        return NbtIo.readCompressed(path, NbtSizeTracker.of(0x6400000L));
    }

    static Dynamic<?> readLevelProperties(Path path, DataFixer dataFixer) throws IOException {
        NbtCompound nbtCompound = LevelStorage.readLevelProperties(path);
        NbtCompound nbtCompound2 = nbtCompound.getCompound(DATA_KEY);
        int i = NbtHelper.getDataVersion(nbtCompound2, -1);
        Dynamic<NbtCompound> dynamic2 = DataFixTypes.LEVEL.update(dataFixer, new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbtCompound2), i);
        dynamic2 = dynamic2.update("Player", dynamic -> DataFixTypes.PLAYER.update(dataFixer, dynamic, i));
        dynamic2 = dynamic2.update("WorldGenSettings", dynamic -> DataFixTypes.WORLD_GEN_SETTINGS.update(dataFixer, dynamic, i));
        return dynamic2;
    }

    private LevelSummary readSummary(LevelSave save, boolean locked) {
        Path path = save.getLevelDatPath();
        if (Files.exists(path, new LinkOption[0])) {
            try {
                List<SymlinkEntry> list;
                if (Files.isSymbolicLink(path) && !(list = this.symlinkFinder.validate(path)).isEmpty()) {
                    LOGGER.warn("{}", (Object)SymlinkValidationException.getMessage(path, list));
                    return new LevelSummary.SymlinkLevelSummary(save.getRootPath(), save.getIconPath());
                }
                NbtElement nbtElement = LevelStorage.loadCompactLevelData(path);
                if (nbtElement instanceof NbtCompound) {
                    NbtCompound nbtCompound = (NbtCompound)nbtElement;
                    NbtCompound nbtCompound2 = nbtCompound.getCompound(DATA_KEY);
                    int i = NbtHelper.getDataVersion(nbtCompound2, -1);
                    Dynamic<NbtCompound> dynamic = DataFixTypes.LEVEL.update(this.dataFixer, new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbtCompound2), i);
                    return this.parseSummary(dynamic, save, locked);
                }
                LOGGER.warn("Invalid root tag in {}", (Object)path);
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", (Object)path, (Object)exception);
            }
        }
        return new LevelSummary.RecoveryWarning(save.getRootPath(), save.getIconPath(), LevelStorage.getLastModifiedTime(save));
    }

    private static long getLastModifiedTime(LevelSave save) {
        Instant instant = LevelStorage.getLastModifiedTime(save.getLevelDatPath());
        if (instant == null) {
            instant = LevelStorage.getLastModifiedTime(save.getLevelDatOldPath());
        }
        return instant == null ? -1L : instant.toEpochMilli();
    }

    @Nullable
    static Instant getLastModifiedTime(Path path) {
        try {
            return Files.getLastModifiedTime(path, new LinkOption[0]).toInstant();
        } catch (IOException iOException) {
            return null;
        }
    }

    LevelSummary parseSummary(Dynamic<?> dynamic, LevelSave save, boolean locked) {
        SaveVersionInfo saveVersionInfo = SaveVersionInfo.fromDynamic(dynamic);
        int i = saveVersionInfo.getLevelFormatVersion();
        if (i == 19132 || i == 19133) {
            boolean bl = i != this.getCurrentVersion();
            Path path = save.getIconPath();
            DataConfiguration dataConfiguration = LevelStorage.parseDataPackSettings(dynamic);
            LevelInfo levelInfo = LevelInfo.fromDynamic(dynamic, dataConfiguration);
            FeatureSet featureSet = LevelStorage.parseEnabledFeatures(dynamic);
            boolean bl2 = FeatureFlags.isNotVanilla(featureSet);
            return new LevelSummary(levelInfo, saveVersionInfo, save.getRootPath(), bl, locked, bl2, path);
        }
        throw new InvalidNbtException("Unknown data version: " + Integer.toHexString(i));
    }

    private static FeatureSet parseEnabledFeatures(Dynamic<?> levelData) {
        Set<Identifier> set = levelData.get("enabled_features").asStream().flatMap(featureFlag -> featureFlag.asString().result().map(Identifier::tryParse).stream()).collect(Collectors.toSet());
        return FeatureFlags.FEATURE_MANAGER.featureSetOf(set, id -> {});
    }

    /**
     * {@return the compact version of the NBT for the level data {@code file}}
     * 
     * <p>The returned NBT will not have {@code Player} and {@code WorldGenSettings} keys.
     */
    @Nullable
    private static NbtElement loadCompactLevelData(Path path) throws IOException {
        ExclusiveNbtCollector exclusiveNbtCollector = new ExclusiveNbtCollector(new NbtScanQuery(DATA_KEY, NbtCompound.TYPE, "Player"), new NbtScanQuery(DATA_KEY, NbtCompound.TYPE, "WorldGenSettings"));
        NbtIo.scanCompressed(path, (NbtScanner)exclusiveNbtCollector, NbtSizeTracker.of(0x6400000L));
        return exclusiveNbtCollector.getRoot();
    }

    public boolean isLevelNameValid(String name) {
        try {
            Path path = this.resolve(name);
            Files.createDirectory(path, new FileAttribute[0]);
            Files.deleteIfExists(path);
            return true;
        } catch (IOException iOException) {
            return false;
        }
    }

    public boolean levelExists(String name) {
        try {
            return Files.isDirectory(this.resolve(name), new LinkOption[0]);
        } catch (InvalidPathException invalidPathException) {
            return false;
        }
    }

    public Path resolve(String name) {
        return this.savesDirectory.resolve(name);
    }

    public Path getSavesDirectory() {
        return this.savesDirectory;
    }

    public Path getBackupsDirectory() {
        return this.backupsDirectory;
    }

    public Session createSession(String directoryName) throws IOException, SymlinkValidationException {
        Path path = this.resolve(directoryName);
        List<SymlinkEntry> list = this.symlinkFinder.collect(path, true);
        if (!list.isEmpty()) {
            throw new SymlinkValidationException(path, list);
        }
        return new Session(directoryName, path);
    }

    public Session createSessionWithoutSymlinkCheck(String directoryName) throws IOException {
        Path path = this.resolve(directoryName);
        return new Session(directoryName, path);
    }

    public SymlinkFinder getSymlinkFinder() {
        return this.symlinkFinder;
    }

    public record LevelList(List<LevelSave> levels) implements Iterable<LevelSave>
    {
        public boolean isEmpty() {
            return this.levels.isEmpty();
        }

        @Override
        public Iterator<LevelSave> iterator() {
            return this.levels.iterator();
        }
    }

    public record LevelSave(Path path) {
        public String getRootPath() {
            return this.path.getFileName().toString();
        }

        public Path getLevelDatPath() {
            return this.getPath(WorldSavePath.LEVEL_DAT);
        }

        public Path getLevelDatOldPath() {
            return this.getPath(WorldSavePath.LEVEL_DAT_OLD);
        }

        public Path getCorruptedLevelDatPath(LocalDateTime dateTime) {
            return this.path.resolve(WorldSavePath.LEVEL_DAT.getRelativePath() + "_corrupted_" + dateTime.format(TIME_FORMATTER));
        }

        public Path getRawLevelDatPath(LocalDateTime dateTime) {
            return this.path.resolve(WorldSavePath.LEVEL_DAT.getRelativePath() + "_raw_" + dateTime.format(TIME_FORMATTER));
        }

        public Path getIconPath() {
            return this.getPath(WorldSavePath.ICON_PNG);
        }

        public Path getSessionLockPath() {
            return this.getPath(WorldSavePath.SESSION_LOCK);
        }

        public Path getPath(WorldSavePath savePath) {
            return this.path.resolve(savePath.getRelativePath());
        }
    }

    public class Session
    implements AutoCloseable {
        final SessionLock lock;
        final LevelSave directory;
        private final String directoryName;
        private final Map<WorldSavePath, Path> paths = Maps.newHashMap();

        Session(String directoryName, Path path) throws IOException {
            this.directoryName = directoryName;
            this.directory = new LevelSave(path);
            this.lock = SessionLock.create(path);
        }

        public long getUsableSpace() {
            try {
                return Files.getFileStore(this.directory.path).getUsableSpace();
            } catch (Exception exception) {
                return Long.MAX_VALUE;
            }
        }

        public boolean shouldShowLowDiskSpaceWarning() {
            return this.getUsableSpace() < 0x4000000L;
        }

        public void tryClose() {
            try {
                this.close();
            } catch (IOException iOException) {
                LOGGER.warn("Failed to unlock access to level {}", (Object)this.getDirectoryName(), (Object)iOException);
            }
        }

        public LevelStorage getLevelStorage() {
            return LevelStorage.this;
        }

        public LevelSave getDirectory() {
            return this.directory;
        }

        public String getDirectoryName() {
            return this.directoryName;
        }

        public Path getDirectory(WorldSavePath savePath) {
            return this.paths.computeIfAbsent(savePath, this.directory::getPath);
        }

        public Path getWorldDirectory(RegistryKey<World> key) {
            return DimensionType.getSaveDirectory(key, this.directory.path());
        }

        private void checkValid() {
            if (!this.lock.isValid()) {
                throw new IllegalStateException("Lock is no longer valid");
            }
        }

        public PlayerSaveHandler createSaveHandler() {
            this.checkValid();
            return new PlayerSaveHandler(this, LevelStorage.this.dataFixer);
        }

        public LevelSummary getLevelSummary(Dynamic<?> dynamic) {
            this.checkValid();
            return LevelStorage.this.parseSummary(dynamic, this.directory, false);
        }

        public Dynamic<?> readLevelProperties() throws IOException {
            return this.readLevelProperties(false);
        }

        public Dynamic<?> readOldLevelProperties() throws IOException {
            return this.readLevelProperties(true);
        }

        private Dynamic<?> readLevelProperties(boolean old) throws IOException {
            this.checkValid();
            return LevelStorage.readLevelProperties(old ? this.directory.getLevelDatOldPath() : this.directory.getLevelDatPath(), LevelStorage.this.dataFixer);
        }

        public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties) {
            this.backupLevelDataFile(registryManager, saveProperties, null);
        }

        public void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties, @Nullable NbtCompound nbt) {
            NbtCompound nbtCompound = saveProperties.cloneWorldNbt(registryManager, nbt);
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put(LevelStorage.DATA_KEY, nbtCompound);
            this.save(nbtCompound2);
        }

        private void save(NbtCompound nbt) {
            Path path = this.directory.path();
            try {
                Path path2 = Files.createTempFile(path, "level", ".dat", new FileAttribute[0]);
                NbtIo.writeCompressed(nbt, path2);
                Path path3 = this.directory.getLevelDatOldPath();
                Path path4 = this.directory.getLevelDatPath();
                Util.backupAndReplace(path4, path2, path3);
            } catch (Exception exception) {
                LOGGER.error("Failed to save level {}", (Object)path, (Object)exception);
            }
        }

        public Optional<Path> getIconFile() {
            if (!this.lock.isValid()) {
                return Optional.empty();
            }
            return Optional.of(this.directory.getIconPath());
        }

        public void deleteSessionLock() throws IOException {
            this.checkValid();
            final Path path = this.directory.getSessionLockPath();
            LOGGER.info("Deleting level {}", (Object)this.directoryName);
            for (int i = 1; i <= 5; ++i) {
                LOGGER.info("Attempt {}...", (Object)i);
                try {
                    Files.walkFileTree(this.directory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                        @Override
                        public FileVisitResult visitFile(Path path2, BasicFileAttributes basicFileAttributes) throws IOException {
                            if (!path2.equals(path)) {
                                LOGGER.debug("Deleting {}", (Object)path2);
                                Files.delete(path2);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path path2, @Nullable IOException iOException) throws IOException {
                            if (iOException != null) {
                                throw iOException;
                            }
                            if (path2.equals(Session.this.directory.path())) {
                                Session.this.lock.close();
                                Files.deleteIfExists(path);
                            }
                            Files.delete(path2);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public /* synthetic */ FileVisitResult postVisitDirectory(Object path2, @Nullable IOException exception) throws IOException {
                            return this.postVisitDirectory((Path)path2, exception);
                        }

                        @Override
                        public /* synthetic */ FileVisitResult visitFile(Object path2, BasicFileAttributes attributes) throws IOException {
                            return this.visitFile((Path)path2, attributes);
                        }
                    });
                    break;
                } catch (IOException iOException) {
                    if (i < 5) {
                        LOGGER.warn("Failed to delete {}", (Object)this.directory.path(), (Object)iOException);
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException interruptedException) {}
                        continue;
                    }
                    throw iOException;
                }
            }
        }

        public void save(String name) throws IOException {
            this.save((NbtCompound nbt) -> nbt.putString("LevelName", name.trim()));
        }

        public void removePlayerAndSave(String name) throws IOException {
            this.save((NbtCompound nbt) -> {
                nbt.putString("LevelName", name.trim());
                nbt.remove("Player");
            });
        }

        private void save(Consumer<NbtCompound> nbtProcessor) throws IOException {
            this.checkValid();
            NbtCompound nbtCompound = LevelStorage.readLevelProperties(this.directory.getLevelDatPath());
            nbtProcessor.accept(nbtCompound.getCompound(LevelStorage.DATA_KEY));
            this.save(nbtCompound);
        }

        public long createBackup() throws IOException {
            this.checkValid();
            String string = LocalDateTime.now().format(TIME_FORMATTER) + "_" + this.directoryName;
            Path path = LevelStorage.this.getBackupsDirectory();
            try {
                PathUtil.createDirectories(path);
            } catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            Path path2 = path.resolve(PathUtil.getNextUniqueName(path, string, ".zip"));
            try (final ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(path2, new OpenOption[0])));){
                final Path path3 = Paths.get(this.directoryName, new String[0]);
                Files.walkFileTree(this.directory.path(), (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (path.endsWith("session.lock")) {
                            return FileVisitResult.CONTINUE;
                        }
                        String string = path3.resolve(Session.this.directory.path().relativize(path)).toString().replace('\\', '/');
                        ZipEntry zipEntry = new ZipEntry(string);
                        zipOutputStream.putNextEntry(zipEntry);
                        com.google.common.io.Files.asByteSource(path.toFile()).copyTo(zipOutputStream);
                        zipOutputStream.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public /* synthetic */ FileVisitResult visitFile(Object path, BasicFileAttributes attributes) throws IOException {
                        return this.visitFile((Path)path, attributes);
                    }
                });
            }
            return Files.size(path2);
        }

        public boolean levelDatExists() {
            return Files.exists(this.directory.getLevelDatPath(), new LinkOption[0]) || Files.exists(this.directory.getLevelDatOldPath(), new LinkOption[0]);
        }

        @Override
        public void close() throws IOException {
            this.lock.close();
        }

        public boolean tryRestoreBackup() {
            return Util.backupAndReplace(this.directory.getLevelDatPath(), this.directory.getLevelDatOldPath(), this.directory.getCorruptedLevelDatPath(LocalDateTime.now()), true);
        }

        @Nullable
        public Instant getLastModifiedTime(boolean old) {
            return LevelStorage.getLastModifiedTime(old ? this.directory.getLevelDatOldPath() : this.directory.getLevelDatPath());
        }
    }
}

