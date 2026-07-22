//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod;

import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Mod implements Wrapper {
   public List<Setting> settings = new ArrayList<>();
   private String name;

   public Mod(String name) {
      this.name = name;
   }

   public Mod() {
   }

   public static boolean nullCheck() {
      return mc.player == null;
   }

   public static boolean fullNullCheck() {
      return mc.player == null || mc.world == null;
   }

   public static boolean spawnCheck() {
      return mc.player.ticksExisted > 15;
   }

   public Setting add(Setting setting) {
      setting.setMod(this);
      this.settings.add(setting);
      if (this instanceof Module && mc.currentScreen instanceof Gui) {
         Gui.INSTANCE.updateModule((Module)this);
      }

      return setting;
   }

   public Setting register(Setting setting) {
      setting.setMod(this);
      this.settings.add(setting);
      if (this instanceof Module && mc.currentScreen instanceof Gui) {
         Gui.INSTANCE.updateModule((Module)this);
      }

      return setting;
   }

   public Setting getSettingByName(String name) {
      for(Setting setting : this.settings) {
         if (setting.getName().equalsIgnoreCase(name)) {
            return setting;
         }
      }

      return null;
   }

   public void resetSettings() {
      this.settings = new ArrayList<>();
   }

   public String getName() {
      return this.name;
   }

   public List<Setting> getSettings() {
      return this.settings;
   }
}
