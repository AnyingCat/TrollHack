package github.trollhack.core;

import github.trollhack.core.impl.*;
import net.minecraft.client.MinecraftClient;

public class Managers {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static ModuleManager MODULE;
    public static ConfigManager CONFIG;
    public static ShaderManager SHADER;
    public static FriendManager FRIEND;
    public static RotationManager ROTATION;
    public static HudManager HUD;
    public static PopManager POP;

    public static void init() {
        MODULE = new ModuleManager();
        SHADER = new ShaderManager();
        FRIEND = new FriendManager();
        ROTATION = new RotationManager();
        HUD = new HudManager();
        POP = new PopManager();
        CONFIG = new ConfigManager(MODULE, FRIEND, HUD);
        CONFIG.loadConfig();
    }

    public static void onShutdown() {
        if (CONFIG != null) {
            CONFIG.saveConfig();
        }
    }
}
