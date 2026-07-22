package me.catrix;

import com.mojang.logging.LogUtils;
import me.catrix.api.events.eventbus.EventBus;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.*;
import net.fabricmc.api.ModInitializer;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Catrix implements ModInitializer {

    @Override
    public void onInitialize() {
        load();
    }

    public static final String NAME = "Catrix";
    public static final String VERSION = "0.4";
    public static String PREFIX = ";";
    public static final EventBus EVENT_BUS = new EventBus();
//    public static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    // Systems
    public static HoleManager HOLE;
    public static PlayerManager PLAYER;
    public static TradeManager TRADE;
    public static XrayManager XRAY;
    public static ModuleManager MODULE;
    public static CommandManager COMMAND;
    public static GuiManager GUI;
    public static ConfigManager CONFIG;
    public static RotationManager ROTATION;
    public static BreakManager BREAK;
    public static PopManager POP;
    public static FriendManager FRIEND;
    public static TimerManager TIMER;
    public static ShaderManager SHADER;
    public static SoundManager SOUND;
    public static FPSManager FPS;
    public static ServerManager SERVER;
    public static ThreadManager THREAD;
    public static boolean loaded = false;

    public static void load() {
        EVENT_BUS.registerLambdaFactory((lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        CONFIG = new ConfigManager();

        PREFIX = Catrix.CONFIG.getString("prefix", ";");
        SOUND = new SoundManager();
        THREAD = new ThreadManager();
        HOLE = new HoleManager();
        MODULE = new ModuleManager();
        COMMAND = new CommandManager();
        GUI = new GuiManager();
        FRIEND = new FriendManager();
        XRAY = new XrayManager();
        TRADE = new TradeManager();
        ROTATION = new RotationManager();
        BREAK = new BreakManager();
        PLAYER = new PlayerManager();
        POP = new PopManager();
        TIMER = new TimerManager();
        SHADER = new ShaderManager();
        FPS = new FPSManager();
        SERVER = new ServerManager();
        RenderShadersUtil.init();
        SOUND.registerSounds();
        CONFIG.loadSettings();
        System.out.println("[" + Catrix.NAME + "] loaded");
        LogUtils.getLogger().info("""
                \n
                  ██████╗ █████╗ ████████╗██████╗ ██╗██╗  ██╗
                 ██╔════╝██╔══██╗╚══██╔══╝██╔══██╗██║╚██╗██╔╝
                 ██║     ███████║   ██║   ██████╔╝██║ ╚███╔╝ 
                 ██║     ██╔══██║   ██║   ██╔══██╗██║ ██╔██╗ 
                 ╚██████╗██║  ██║   ██║   ██║  ██║██║██╔╝ ██╗
                  ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝
                """);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loaded) {
                save();
            }
        }));
        loaded = true;
    }

    public static void unload() {
        loaded = false;
        System.out.println("[" + Catrix.NAME + "] Unloading..");
        EVENT_BUS.listenerMap.clear();
        ConfigManager.resetModule();
        System.out.println("[" + Catrix.NAME + "] Unloaded");
    }

    public static void save() {
        System.out.println("[" + Catrix.NAME + "] Saving");
        CONFIG.saveSettings();
        FRIEND.save();
        XRAY.save();
        TRADE.save();
        System.out.println("[" + Catrix.NAME + "] Saved");
    }
}
