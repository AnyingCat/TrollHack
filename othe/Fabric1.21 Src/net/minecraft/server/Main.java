/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ParsedSaveProperties;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.updater.WorldUpdater;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("recreateRegionFiles");
        OptionSpecBuilder optionSpec8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec9 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<Path> optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec<String> optionSpec16 = optionParser.nonOptions();
        try {
            SaveLoader saveLoader;
            Dynamic<?> dynamic;
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(optionSpec9)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            Path path = optionSet.valueOf(optionSpec15);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.initCrashReport();
            if (optionSet.has(optionSpec14)) {
                FlightProfiler.INSTANCE.start(InstanceType.SERVER);
            }
            Bootstrap.initialize();
            Bootstrap.logMissing();
            Util.startTimerHack();
            Path path2 = Paths.get("server.properties", new String[0]);
            ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(path2);
            serverPropertiesLoader.store();
            ChunkCompressionFormat.setCurrentFormat(serverPropertiesLoader.getPropertiesHandler().regionFileCompression);
            Path path3 = Paths.get("eula.txt", new String[0]);
            EulaReader eulaReader = new EulaReader(path3);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!eulaReader.isEulaAgreedTo()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File(optionSet.valueOf(optionSpec10));
            ApiServices apiServices = ApiServices.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec11)).orElse(serverPropertiesLoader.getPropertiesHandler().levelName);
            LevelStorage levelStorage = LevelStorage.create(file.toPath());
            LevelStorage.Session session = levelStorage.createSession(string);
            if (session.levelDatExists()) {
                LevelSummary levelSummary;
                try {
                    dynamic = session.readLevelProperties();
                    levelSummary = session.getLevelSummary(dynamic);
                } catch (IOException | NbtCrashException | NbtException exception) {
                    LevelStorage.LevelSave levelSave = session.getDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)levelSave.getLevelDatPath(), (Object)exception);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        dynamic = session.readOldLevelProperties();
                        levelSummary = session.getLevelSummary(dynamic);
                    } catch (IOException | NbtCrashException | NbtException exception2) {
                        LOGGER.error("Failed to load world data from {}", (Object)levelSave.getLevelDatOldPath(), (Object)exception2);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)levelSave.getLevelDatPath(), (Object)levelSave.getLevelDatOldPath());
                        return;
                    }
                    session.tryRestoreBackup();
                }
                if (levelSummary.requiresConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!levelSummary.isVersionAvailable()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }
            Dynamic<?> dynamic2 = dynamic;
            boolean bl = optionSet.has(optionSpec8);
            if (bl) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            ResourcePackManager resourcePackManager = VanillaDataPackProvider.createManager(session);
            try {
                SaveLoading.ServerConfig serverConfig = Main.createServerConfig(serverPropertiesLoader.getPropertiesHandler(), dynamic2, bl, resourcePackManager);
                saveLoader = (SaveLoader)Util.waitAndApply(applyExecutor -> SaveLoading.load(serverConfig, context -> {
                    DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder;
                    GeneratorOptions generatorOptions;
                    LevelInfo levelInfo;
                    Registry<DimensionOptions> registry = context.dimensionsRegistryManager().get(RegistryKeys.DIMENSION);
                    if (dynamic2 != null) {
                        ParsedSaveProperties parsedSaveProperties = LevelStorage.parseSaveProperties(dynamic2, context.dataConfiguration(), registry, context.worldGenRegistryManager());
                        return new SaveLoading.LoadContext<SaveProperties>(parsedSaveProperties.properties(), parsedSaveProperties.dimensions().toDynamicRegistryManager());
                    }
                    LOGGER.info("No existing world data, creating new world");
                    if (optionSet.has(optionSpec3)) {
                        levelInfo = MinecraftServer.DEMO_LEVEL_INFO;
                        generatorOptions = GeneratorOptions.DEMO_OPTIONS;
                        dimensionOptionsRegistryHolder = WorldPresets.createDemoOptions(context.worldGenRegistryManager());
                    } else {
                        ServerPropertiesHandler serverPropertiesHandler = serverPropertiesLoader.getPropertiesHandler();
                        levelInfo = new LevelInfo(serverPropertiesHandler.levelName, serverPropertiesHandler.gameMode, serverPropertiesHandler.hardcore, serverPropertiesHandler.difficulty, false, new GameRules(), context.dataConfiguration());
                        generatorOptions = optionSet.has(optionSpec4) ? serverPropertiesHandler.generatorOptions.withBonusChest(true) : serverPropertiesHandler.generatorOptions;
                        dimensionOptionsRegistryHolder = serverPropertiesHandler.createDimensionsRegistryHolder(context.worldGenRegistryManager());
                    }
                    DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = dimensionOptionsRegistryHolder.toConfig(registry);
                    Lifecycle lifecycle = dimensionsConfig.getLifecycle().add(context.worldGenRegistryManager().getRegistryLifecycle());
                    return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(levelInfo, generatorOptions, dimensionsConfig.specialWorldProperty(), lifecycle), dimensionsConfig.toDynamicRegistryManager());
                }, SaveLoader::new, Util.getMainWorkerExecutor(), applyExecutor)).get();
            } catch (Exception exception3) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", exception3);
                return;
            }
            DynamicRegistryManager.Immutable immutable = saveLoader.combinedDynamicRegistries().getCombinedRegistryManager();
            boolean bl2 = optionSet.has(optionSpec7);
            if (optionSet.has(optionSpec5) || bl2) {
                Main.forceUpgradeWorld(session, Schemas.getFixer(), optionSet.has(optionSpec6), () -> true, immutable, bl2);
            }
            SaveProperties saveProperties = saveLoader.saveProperties();
            session.backupLevelDataFile(immutable, saveProperties);
            final MinecraftDedicatedServer minecraftDedicatedServer = MinecraftServer.startServer(thread -> {
                boolean bl;
                MinecraftDedicatedServer minecraftDedicatedServer = new MinecraftDedicatedServer((Thread)thread, session, resourcePackManager, saveLoader, serverPropertiesLoader, Schemas.getFixer(), apiServices, WorldGenerationProgressLogger::create);
                minecraftDedicatedServer.setServerPort((Integer)optionSet.valueOf(optionSpec12));
                minecraftDedicatedServer.setDemo(optionSet.has(optionSpec3));
                minecraftDedicatedServer.setServerId((String)optionSet.valueOf(optionSpec13));
                boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec16).contains("nogui");
                if (bl && !GraphicsEnvironment.isHeadless()) {
                    minecraftDedicatedServer.createGui();
                }
                return minecraftDedicatedServer;
            });
            Thread thread2 = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    minecraftDedicatedServer.stop(true);
                }
            };
            thread2.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread2);
        } catch (Exception exception4) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", exception4);
        }
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(l), new OpenOption[0]);
        } catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static SaveLoading.ServerConfig createServerConfig(ServerPropertiesHandler serverPropertiesHandler, @Nullable Dynamic<?> dynamic, boolean safeMode, ResourcePackManager dataPackManager) {
        DataConfiguration dataConfiguration2;
        boolean bl;
        if (dynamic != null) {
            DataConfiguration dataConfiguration = LevelStorage.parseDataPackSettings(dynamic);
            bl = false;
            dataConfiguration2 = dataConfiguration;
        } else {
            bl = true;
            dataConfiguration2 = new DataConfiguration(serverPropertiesHandler.dataPackSettings, FeatureFlags.DEFAULT_ENABLED_FEATURES);
        }
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(dataPackManager, dataConfiguration2, safeMode, bl);
        return new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.DEDICATED, serverPropertiesHandler.functionPermissionLevel);
    }

    private static void forceUpgradeWorld(LevelStorage.Session session, DataFixer dataFixer, boolean eraseCache, BooleanSupplier continueCheck, DynamicRegistryManager dynamicRegistryManager, boolean recreateRegionFiles) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpdater worldUpdater = new WorldUpdater(session, dataFixer, dynamicRegistryManager, eraseCache, recreateRegionFiles);
        Text text = null;
        while (!worldUpdater.isDone()) {
            int i;
            Text text2 = worldUpdater.getStatus();
            if (text != text2) {
                text = text2;
                LOGGER.info(worldUpdater.getStatus().getString());
            }
            if ((i = worldUpdater.getTotalChunkCount()) > 0) {
                int j = worldUpdater.getUpgradedChunkCount() + worldUpdater.getSkippedChunkCount();
                LOGGER.info("{}% completed ({} / {} chunks)...", MathHelper.floor((float)j / (float)i * 100.0f), j, i);
            }
            if (!continueCheck.getAsBoolean()) {
                worldUpdater.cancel();
                continue;
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException interruptedException) {}
        }
    }
}

