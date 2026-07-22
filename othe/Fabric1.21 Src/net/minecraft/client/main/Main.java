/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.lang.reflect.Type;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.telemetry.GameLoadTimeEvent;
import net.minecraft.client.session.telemetry.TelemetryEventProperty;
import net.minecraft.client.util.GlException;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.WinNativeModuleUtil;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Main {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @DontObfuscate
    public static void main(String[] args) {
        RunArgs runArgs;
        Logger logger;
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("disableMultiplayer");
        optionParser.accepts("disableChat");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpecBuilder optionSpec = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec<String> optionSpec2 = optionParser.accepts("quickPlayPath").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec3 = optionParser.accepts("quickPlaySingleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec4 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec5 = optionParser.accepts("quickPlayRealms").withRequiredArg();
        ArgumentAcceptingOptionSpec<File> optionSpec6 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo(new File("."), (File[])new File[0]);
        ArgumentAcceptingOptionSpec<File> optionSpec7 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<File> optionSpec8 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec10 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo("8080", (String[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec11 = optionParser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec12 = optionParser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec13 = optionParser.accepts("username").withRequiredArg().defaultsTo("Player" + System.currentTimeMillis() % 1000L, (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec14 = optionParser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec15 = optionParser.accepts("xuid").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec16 = optionParser.accepts("clientId").withOptionalArg().defaultsTo("", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec17 = optionParser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> optionSpec18 = optionParser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<Integer> optionSpec19 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(854, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec20 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(480, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<Integer> optionSpec21 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<Integer> optionSpec22 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec<String> optionSpec23 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo("{}", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec24 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo("{}", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec25 = optionParser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec26 = optionParser.accepts("userType").withRequiredArg().defaultsTo("legacy", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec27 = optionParser.accepts("versionType").withRequiredArg().defaultsTo("release", (String[])new String[0]);
        NonOptionArgumentSpec<String> optionSpec28 = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(args);
        File file = Main.getOption(optionSet, optionSpec6);
        String string = Main.getOption(optionSet, optionSpec18);
        String string2 = "Pre-bootstrap";
        try {
            String string3;
            Session.AccountType accountType;
            if (optionSet.has(optionSpec)) {
                FlightProfiler.INSTANCE.start(InstanceType.CLIENT);
            }
            Stopwatch stopwatch = Stopwatch.createStarted(Ticker.systemTicker());
            Stopwatch stopwatch2 = Stopwatch.createStarted(Ticker.systemTicker());
            GameLoadTimeEvent.INSTANCE.addTimer(TelemetryEventProperty.LOAD_TIME_TOTAL_TIME_MS, stopwatch);
            GameLoadTimeEvent.INSTANCE.addTimer(TelemetryEventProperty.LOAD_TIME_PRE_WINDOW_MS, stopwatch2);
            SharedConstants.createGameVersion();
            CompletableFuture<?> completableFuture = Schemas.optimize(DataFixTypes.REQUIRED_TYPES);
            CrashReport.initCrashReport();
            logger = LogUtils.getLogger();
            string2 = "Bootstrap";
            Bootstrap.initialize();
            GameLoadTimeEvent.INSTANCE.setBootstrapTime(Bootstrap.LOAD_TIME.get());
            Bootstrap.logMissing();
            string2 = "Argument parsing";
            List<String> list = optionSet.valuesOf(optionSpec28);
            if (!list.isEmpty()) {
                logger.info("Completely ignored arguments: {}", (Object)list);
            }
            if ((accountType = Session.AccountType.byName(string3 = (String)optionSpec26.value(optionSet))) == null) {
                logger.warn("Unrecognized user type: {}", (Object)string3);
            }
            String string4 = Main.getOption(optionSet, optionSpec9);
            Proxy proxy = Proxy.NO_PROXY;
            if (string4 != null) {
                try {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(string4, (int)Main.getOption(optionSet, optionSpec10)));
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            final String string5 = Main.getOption(optionSet, optionSpec11);
            final String string6 = Main.getOption(optionSet, optionSpec12);
            if (!proxy.equals(Proxy.NO_PROXY) && Main.isNotNullOrEmpty(string5) && Main.isNotNullOrEmpty(string6)) {
                Authenticator.setDefault(new Authenticator(){

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(string5, string6.toCharArray());
                    }
                });
            }
            int i = Main.getOption(optionSet, optionSpec19);
            int j = Main.getOption(optionSet, optionSpec20);
            OptionalInt optionalInt = Main.toOptional(Main.getOption(optionSet, optionSpec21));
            OptionalInt optionalInt2 = Main.toOptional(Main.getOption(optionSet, optionSpec22));
            boolean bl = optionSet.has("fullscreen");
            boolean bl2 = optionSet.has("demo");
            boolean bl3 = optionSet.has("disableMultiplayer");
            boolean bl4 = optionSet.has("disableChat");
            Gson gson = new GsonBuilder().registerTypeAdapter((Type)((Object)PropertyMap.class), new PropertyMap.Serializer()).create();
            PropertyMap propertyMap = JsonHelper.deserialize(gson, Main.getOption(optionSet, optionSpec23), PropertyMap.class);
            PropertyMap propertyMap2 = JsonHelper.deserialize(gson, Main.getOption(optionSet, optionSpec24), PropertyMap.class);
            String string7 = Main.getOption(optionSet, optionSpec27);
            File file2 = optionSet.has(optionSpec7) ? Main.getOption(optionSet, optionSpec7) : new File(file, "assets/");
            File file3 = optionSet.has(optionSpec8) ? Main.getOption(optionSet, optionSpec8) : new File(file, "resourcepacks/");
            UUID uUID = optionSet.has(optionSpec14) ? UndashedUuid.fromStringLenient((String)optionSpec14.value(optionSet)) : Uuids.getOfflinePlayerUuid((String)optionSpec13.value(optionSet));
            String string8 = optionSet.has(optionSpec25) ? (String)optionSpec25.value(optionSet) : null;
            String string9 = optionSet.valueOf(optionSpec15);
            String string10 = optionSet.valueOf(optionSpec16);
            String string11 = Main.getOption(optionSet, optionSpec2);
            String string12 = Main.unescape(Main.getOption(optionSet, optionSpec3));
            String string13 = Main.unescape(Main.getOption(optionSet, optionSpec4));
            String string14 = Main.unescape(Main.getOption(optionSet, optionSpec5));
            Session session = new Session((String)optionSpec13.value(optionSet), uUID, (String)optionSpec17.value(optionSet), Main.toOptional(string9), Main.toOptional(string10), accountType);
            runArgs = new RunArgs(new RunArgs.Network(session, propertyMap, propertyMap2, proxy), new WindowSettings(i, j, optionalInt, optionalInt2, bl), new RunArgs.Directories(file, file3, file2, string8), new RunArgs.Game(bl2, string, string7, bl3, bl4), new RunArgs.QuickPlay(string11, string12, string13, string14));
            Util.startTimerHack();
            completableFuture.join();
        } catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.create(throwable, string2);
            CrashReportSection crashReportSection = crashReport.addElement("Initialization");
            WinNativeModuleUtil.addDetailTo(crashReportSection);
            MinecraftClient.addSystemDetailsToCrashReport(null, null, string, null, crashReport);
            MinecraftClient.printCrashReport(null, file, crashReport);
            return;
        }
        Thread thread = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient == null) {
                    return;
                }
                IntegratedServer integratedServer = minecraftClient.getServer();
                if (integratedServer != null) {
                    integratedServer.stop(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(logger));
        Runtime.getRuntime().addShutdownHook(thread);
        MinecraftClient minecraftClient = null;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraftClient = new MinecraftClient(runArgs);
            RenderSystem.finishInitialization();
        } catch (GlException glException) {
            Util.shutdownExecutors();
            logger.warn("Failed to create window: ", glException);
            return;
        } catch (Throwable throwable2) {
            CrashReport crashReport2 = CrashReport.create(throwable2, "Initializing game");
            CrashReportSection crashReportSection2 = crashReport2.addElement("Initialization");
            WinNativeModuleUtil.addDetailTo(crashReportSection2);
            MinecraftClient.addSystemDetailsToCrashReport(minecraftClient, null, runArgs.game.version, null, crashReport2);
            MinecraftClient.printCrashReport(minecraftClient, runArgs.directories.runDir, crashReport2);
            return;
        }
        MinecraftClient minecraftClient2 = minecraftClient;
        minecraftClient2.run();
        BufferRenderer.reset();
        try {
            minecraftClient2.scheduleStop();
        } finally {
            minecraftClient2.stop();
        }
    }

    @Nullable
    private static String unescape(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava(string);
    }

    private static Optional<String> toOptional(String string) {
        return string.isEmpty() ? Optional.empty() : Optional.of(string);
    }

    private static OptionalInt toOptional(@Nullable Integer i) {
        return i != null ? OptionalInt.of(i) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T getOption(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return optionSet.valueOf(optionSpec);
        } catch (Throwable throwable) {
            ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec;
            List list;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(list = (argumentAcceptingOptionSpec = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)list.get(0);
            }
            throw throwable;
        }
    }

    private static boolean isNotNullOrEmpty(@Nullable String s) {
        return s != null && !s.isEmpty();
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

