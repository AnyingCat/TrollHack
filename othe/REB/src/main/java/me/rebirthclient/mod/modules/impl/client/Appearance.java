//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;

public class Appearance extends Module {
   public Appearance() {
      super("HUDEditor", "Drag HUD elements all over your screen", Category.CLIENT);
   }

   @Override
   public void onEnable() {
      mc.displayGuiScreen(me.rebirthclient.mod.gui.screen.Appearance.getClickGui());
   }

   @Override
   public void onTick() {
      if (!(mc.currentScreen instanceof me.rebirthclient.mod.gui.screen.Appearance)) {
         this.disable();
      }
   }
}
