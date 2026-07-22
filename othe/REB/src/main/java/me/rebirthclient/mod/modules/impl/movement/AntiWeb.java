//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class AntiWeb extends Module {
   public final Setting<AntiWeb.AntiMode> antiModeSetting = this.add(new Setting<>("AntiMode", AntiWeb.AntiMode.Block));
   public static AntiWeb INSTANCE;

   public AntiWeb() {
      super("AntiWeb", "Solid web", Category.MOVEMENT);
      INSTANCE = this;
   }

   @Override
   public void onUpdate() {
      if (this.antiModeSetting.getValue() == AntiWeb.AntiMode.Ignore && mc.player.isInWeb) {
         mc.player.isInWeb = false;
      }
   }

   public static enum AntiMode {
      Block,
      Ignore;
   }
}
