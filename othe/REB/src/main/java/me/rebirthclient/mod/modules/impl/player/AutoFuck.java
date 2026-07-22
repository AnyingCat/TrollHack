//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.settings.GameSettings;

public class AutoFuck extends Module {
   public AutoFuck() {
      super("AutoFuck", "auto fuck!!", Category.PLAYER);
   }

   @Override
   public void onTick() {
      mc.gameSettings.keyBindSneak.pressed = !mc.player.isSneaking() || GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);
   }

   @Override
   public void onDisable() {
      mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);
   }
}
