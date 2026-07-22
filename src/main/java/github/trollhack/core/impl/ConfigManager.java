package github.trollhack.core.impl;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.trollhack.settings.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = Paths.get("trollhack", "config");

    private final ModuleManager moduleManager;
    private final FriendManager friendManager;
    private final HudManager hudManager;

    public ConfigManager(ModuleManager moduleManager, FriendManager friendManager, HudManager hudManager) {
        this.moduleManager = moduleManager;
        this.friendManager = friendManager;
        this.hudManager = hudManager;
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
    }

    public void saveConfig() {
        try {
            JsonObject config = new JsonObject();
            JsonObject modules = new JsonObject();

            for (Module module : moduleManager.getModules()) {
                JsonObject moduleObject = new JsonObject();
                moduleObject.addProperty("enabled", module.isEnabled());
                JsonObject settings = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    saveSetting(settings, setting);
                }
                moduleObject.add("settings", settings);
                modules.add(module.getName(), moduleObject);
            }
            config.add("modules", modules);

            JsonObject hudModules = new JsonObject();
            for (HudModule hud : hudManager.hudModules) {
                JsonObject hudObject = new JsonObject();
                hudObject.addProperty("enabled", hud.isEnabled());
                hudObject.addProperty("posX", hud.getPosX());
                hudObject.addProperty("posY", hud.getPosY());
                JsonObject settings = new JsonObject();
                for (Setting<?> setting : hud.getSettings()) {
                    saveSetting(settings, setting);
                }
                hudObject.add("settings", settings);
                hudModules.add(hud.getName(), hudObject);
            }
            config.add("hud", hudModules);

            Path configFile = CONFIG_DIR.resolve("config.json");
            try (FileWriter writer = new FileWriter(configFile.toFile())) {
                GSON.toJson(config, writer);
            }
            saveFriends();
            LOGGER.info("Saved config to {}", configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    private void saveSetting(JsonObject settings, Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            settings.addProperty(setting.getName(), ((BooleanSetting) setting).isEnabled());
        } else if (setting instanceof IntegerSetting) {
            settings.addProperty(setting.getName(), ((IntegerSetting) setting).getValue());
        } else if (setting instanceof FloatSetting) {
            settings.addProperty(setting.getName(), ((FloatSetting) setting).getValue());
        } else if (setting instanceof EnumSetting<?>) {
            settings.addProperty(setting.getName(), setting.getValue().toString());
        } else if (setting instanceof StringSetting) {
            settings.addProperty(setting.getName(), ((StringSetting) setting).getValue());
        } else if (setting instanceof BindSetting) {
            JsonObject bind = new JsonObject();
            bind.addProperty("key", ((BindSetting) setting).getValue());
            bind.addProperty("isHold", ((BindSetting) setting).isHold());
            settings.add(setting.getName(), bind);
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting) setting;
            JsonObject color = new JsonObject();
            color.addProperty("hue", colorSetting.getHue());
            color.addProperty("saturation", colorSetting.getSaturation());
            color.addProperty("brightness", colorSetting.getBrightness());
            color.addProperty("alpha", colorSetting.getAlphaFloat());
            settings.add(setting.getName(), color);
        }
    }

    private void saveFriends() {
        try {
            JsonArray friendsArray = new JsonArray();
            for (String friend : friendManager.getFriends()) {
                friendsArray.add(friend);
            }
            Path friendsFile = CONFIG_DIR.resolve("friends.json");
            try (FileWriter writer = new FileWriter(friendsFile.toFile())) {
                GSON.toJson(friendsArray, writer);
            }
            LOGGER.info("Saved friends to {}", friendsFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save friends", e);
        }
    }

    public void loadConfig() {
        Path configFile = CONFIG_DIR.resolve("config.json");
        if (!Files.exists(configFile)) {
            LOGGER.info("No config file found, creating new one");
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile.toFile())) {
            JsonObject config = GSON.fromJson(reader, JsonObject.class);

            if (config.has("modules")) {
                JsonObject modules = config.getAsJsonObject("modules");
                for (Module module : moduleManager.getModules()) {
                    if (!modules.has(module.getName())) continue;
                    JsonObject moduleObject = modules.getAsJsonObject(module.getName());
                    if (moduleObject.has("enabled")) {
                        module.setEnabled(moduleObject.get("enabled").getAsBoolean());
                    }
                    if (moduleObject.has("settings")) {
                        JsonObject settings = moduleObject.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings()) {
                            loadSetting(settings, setting);
                        }
                    }
                }
            }

            if (config.has("hud")) {
                JsonObject hudModules = config.getAsJsonObject("hud");
                for (HudModule hud : hudManager.hudModules) {
                    if (!hudModules.has(hud.getName())) continue;
                    JsonObject hudObject = hudModules.getAsJsonObject(hud.getName());
                    if (hudObject.has("enabled")) {
                        hud.setEnabled(hudObject.get("enabled").getAsBoolean());
                    }
                    if (hudObject.has("posX")) {
                        hud.setPosX(hudObject.get("posX").getAsFloat());
                    }
                    if (hudObject.has("posY")) {
                        hud.setPosY(hudObject.get("posY").getAsFloat());
                    }
                    if (hudObject.has("settings")) {
                        JsonObject settings = hudObject.getAsJsonObject("settings");
                        for (Setting<?> setting : hud.getSettings()) {
                            loadSetting(settings, setting);
                        }
                    }
                }
            }

            if (config.has("friends")) {
                JsonArray friendsArray = config.getAsJsonArray("friends");
                for (int i = 0; i < friendsArray.size(); i++) {
                    friendManager.addFriend(friendsArray.get(i).getAsString());
                }
                saveFriends();
            }

            Path friendsFile = CONFIG_DIR.resolve("friends.json");
            if (Files.exists(friendsFile)) {
                try (FileReader friendsReader = new FileReader(friendsFile.toFile())) {
                    JsonArray friendsArray = GSON.fromJson(friendsReader, JsonArray.class);
                    for (int i = 0; i < friendsArray.size(); i++) {
                        friendManager.addFriend(friendsArray.get(i).getAsString());
                    }
                    LOGGER.info("Loaded friends from {}", friendsFile);
                } catch (IOException e) {
                    LOGGER.error("Failed to load friends", e);
                }
            }

            LOGGER.info("Loaded config from {}", configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    private void loadSetting(JsonObject settings, Setting<?> setting) {
        if (!settings.has(setting.getName())) return;

        if (setting instanceof BooleanSetting) {
            if (!settings.get(setting.getName()).isJsonPrimitive()) return;
            ((BooleanSetting) setting).setValue(settings.get(setting.getName()).getAsBoolean());
        } else if (setting instanceof IntegerSetting) {
            if (!settings.get(setting.getName()).isJsonPrimitive()) return;
            ((IntegerSetting) setting).setValue(settings.get(setting.getName()).getAsInt());
        } else if (setting instanceof FloatSetting) {
            if (!settings.get(setting.getName()).isJsonPrimitive()) return;
            ((FloatSetting) setting).setValue(settings.get(setting.getName()).getAsFloat());
        } else if (setting instanceof EnumSetting<?>) {
            if (!settings.get(setting.getName()).isJsonPrimitive()) return;
            ((EnumSetting<?>) setting).setValueFromString(settings.get(setting.getName()).getAsString());
        } else if (setting instanceof StringSetting) {
            if (!settings.get(setting.getName()).isJsonPrimitive()) return;
            ((StringSetting) setting).setValue(settings.get(setting.getName()).getAsString());
        } else if (setting instanceof BindSetting) {
            if (!settings.get(setting.getName()).isJsonObject()) return;
            JsonObject bind = settings.getAsJsonObject(setting.getName());
            if (bind.has("key")) {
                ((BindSetting) setting).setValue(bind.get("key").getAsInt());
            }
            if (bind.has("isHold")) {
                ((BindSetting) setting).setHold(bind.get("isHold").getAsBoolean());
            }
        } else if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting) setting;
            if (!settings.get(setting.getName()).isJsonObject()) return;
            JsonObject color = settings.getAsJsonObject(setting.getName());
            if (color.has("hue")) {
                colorSetting.setHue(color.get("hue").getAsFloat());
            }
            if (color.has("saturation")) {
                colorSetting.setSaturation(color.get("saturation").getAsFloat());
            }
            if (color.has("brightness")) {
                colorSetting.setBrightness(color.get("brightness").getAsFloat());
            }
            if (color.has("alpha")) {
                colorSetting.setAlphaFloat(color.get("alpha").getAsFloat());
            }
        }
    }
}
