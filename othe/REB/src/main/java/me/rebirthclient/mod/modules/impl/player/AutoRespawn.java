//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.gui.GuiGameOver;

public class AutoRespawn extends Module {
   public AutoRespawn() {
      super("AutoRespawn", "Auto Respawn when dead", Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (mc.currentScreen instanceof GuiGameOver) {
         mc.player.respawnPlayer();
         mc.displayGuiScreen(null);
      }
   }
}
