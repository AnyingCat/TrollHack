package me.rebirthclient.api.managers.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import me.rebirthclient.Rebirth;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.EnumConverter;
import me.rebirthclient.mod.modules.settings.Setting;

public class ConfigManager implements Wrapper {
   public final ArrayList<Mod> mods = new ArrayList<>();
   public String config = "Rebirth/config/";

   public static void setValueFromJson(Mod mod, Setting setting, JsonElement element) {
      String var4 = setting.getType();
      switch(var4) {
         case "Boolean":
            setting.setValue(element.getAsBoolean());
            return;
         case "Double":
            setting.setValue(element.getAsDouble());
            return;
         case "Float":
            setting.setValue(element.getAsFloat());
            return;
         case "Integer":
            setting.setValue(element.getAsInt());
            return;
         case "String":
            try {
               String str = element.getAsString();
               setting.setValue(str.replace("_", " "));
            } catch (Exception var12) {
            }

            return;
         case "Bind":
            try {
               setting.setValue(new Bind.BindConverter().doBackward(element));
            } catch (Exception var11) {
            }

            return;
         case "Enum":
            try {
               EnumConverter converter = new EnumConverter(((Enum)setting.getValue()).getClass());
               Enum value = converter.doBackward(element);
               setting.setValue(value == null ? setting.getDefaultValue() : value);
            } catch (Exception var10) {
            }

            return;
         case "Color":
            try {
               if (setting.hasBoolean) {
                  setting.injectBoolean(element.getAsBoolean());
               }

               try {
                  setting.setValue(new Color(element.getAsInt(), true));
               } catch (Exception var8) {
               }
            } catch (Exception var9) {
            }

            return;
         default:
            Rebirth.LOGGER.error("Unknown Setting type for: " + mod.getName() + " : " + setting.getName());
      }
   }

   private static void loadFile(JsonObject input, Mod mod) {
      for(Entry<String, JsonElement> entry : input.entrySet()) {
         String settingName = entry.getKey();
         JsonElement element = (JsonElement)entry.getValue();
         if (mod instanceof FriendManager) {
            try {
               Managers.FRIENDS.addFriend(new FriendManager.Friend(element.getAsString(), UUID.fromString(settingName)));
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         } else {
            for(Setting setting : mod.getSettings()) {
               if (settingName.equals(setting.getName())) {
                  try {
                     setValueFromJson(mod, setting, element);
                  } catch (Exception var11) {
                     var11.printStackTrace();
                  }
               }

               if (settingName.equals("should" + setting.getName())) {
                  try {
                     setValueFromJson(mod, setting, element);
                  } catch (Exception var10) {
                  }
               }

               if (settingName.equals("Rainbow" + setting.getName())) {
                  setting.isRainbow = element.getAsBoolean();
               }
            }
         }
      }
   }

   public void loadConfig(String name) {
      List<File> files = Arrays.stream(Objects.requireNonNull(new File("Rebirth").listFiles())).filter(File::isDirectory).collect(Collectors.toList());
      if (files.contains(new File("Rebirth/" + name + "/"))) {
         this.config = "Rebirth/" + name + "/";
      } else {
         this.config = "Rebirth/config/";
      }

      Managers.FRIENDS.onLoad();

      for(Mod mod : this.mods) {
         try {
            this.loadSettings(mod);
         } catch (IOException var6) {
            var6.printStackTrace();
         }
      }

      this.saveCurrentConfig();
   }

   public boolean configExists(String name) {
      List<File> files = Arrays.stream(Objects.requireNonNull(new File("Rebirth").listFiles())).filter(File::isDirectory).collect(Collectors.toList());
      return files.contains(new File("Rebirth/" + name + "/"));
   }

   public void saveConfig(String name) {
      this.config = "Rebirth/" + name + "/";
      File path = new File(this.config);
      if (!path.exists()) {
         path.mkdir();
      }

      Managers.FRIENDS.saveFriends();

      for(Mod mod : this.mods) {
         try {
            this.saveSettings(mod);
         } catch (IOException var6) {
            var6.printStackTrace();
         }
      }

      this.saveCurrentConfig();
   }

   public void saveCurrentConfig() {
      File currentConfig = new File("Rebirth/currentconfig.txt");

      try {
         if (currentConfig.exists()) {
            FileWriter writer = new FileWriter(currentConfig);
            String tempConfig = this.config.replaceAll("/", "");
            writer.write(tempConfig.replaceAll("Rebirth", ""));
            writer.close();
         } else {
            currentConfig.createNewFile();
            FileWriter writer = new FileWriter(currentConfig);
            String tempConfig = this.config.replaceAll("/", "");
            writer.write(tempConfig.replaceAll("Rebirth", ""));
            writer.close();
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public String loadCurrentConfig() {
      File currentConfig = new File("Rebirth/currentconfig.txt");
      String name = "config";

      try {
         if (currentConfig.exists()) {
            Scanner reader = new Scanner(currentConfig);

            while(reader.hasNextLine()) {
               name = reader.nextLine();
            }

            reader.close();
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return name;
   }

   public void saveSettings(Mod mod) throws IOException {
      new JsonObject();
      File directory = new File(this.config + this.getDirectory(mod));
      if (!directory.exists()) {
         directory.mkdir();
      }

      String modName = this.config + this.getDirectory(mod) + mod.getName() + ".json";
      Path outputFile = Paths.get(modName);
      if (!Files.exists(outputFile)) {
         Files.createFile(outputFile);
      }

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson(this.writeSettings(mod));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outputFile)));
      writer.write(json);
      writer.close();
   }

   public void init() {
      this.mods.addAll(Managers.MODULES.modules);
      this.mods.add(Managers.FRIENDS);
      String name = this.loadCurrentConfig();
      this.loadConfig(name);
      Rebirth.LOGGER.info("Config loaded.");
   }

   private void loadSettings(Mod mod) throws IOException {
      String modName = this.config + this.getDirectory(mod) + mod.getName() + ".json";
      Path modPath = Paths.get(modName);
      if (Files.exists(modPath)) {
         this.loadPath(modPath, mod);
      }
   }

   private void loadPath(Path path, Mod mod) throws IOException {
      InputStream stream = Files.newInputStream(path);

      try {
         loadFile(new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject(), mod);
      } catch (IllegalStateException var5) {
         Rebirth.LOGGER.error("Bad Config File for: " + mod.getName() + ". Resetting...");
         loadFile(new JsonObject(), mod);
      }

      stream.close();
   }

   public JsonObject writeSettings(Mod mod) {
      JsonObject object = new JsonObject();
      JsonParser jp = new JsonParser();

      for(Setting setting : mod.getSettings()) {
         if (setting.getValue() instanceof Color) {
            object.add(setting.getName(), jp.parse(String.valueOf(((Color)setting.getValue()).getRGB())));
            if (setting.hasBoolean) {
               object.add("should" + setting.getName(), jp.parse(String.valueOf(setting.booleanValue)));
            }

            object.add("Rainbow" + setting.getName(), jp.parse(String.valueOf(setting.isRainbow)));
         } else if (setting.isEnumSetting()) {
            EnumConverter converter = new EnumConverter(((Enum)setting.getValue()).getClass());
            object.add(setting.getName(), converter.doForward((Enum)setting.getValue()));
         } else {
            if (setting.isStringSetting()) {
               String str = (String)setting.getValue();
               setting.setValue(str.replace(" ", "_"));
            }

            try {
               object.add(setting.getName(), jp.parse(setting.getValue().toString()));
            } catch (Exception var7) {
            }
         }
      }

      return object;
   }

   public String getDirectory(Mod mod) {
      String directory = "";
      if (mod instanceof Module) {
         directory = directory + ((Module)mod).getCategory().getName() + "/";
      }

      return directory;
   }
}
