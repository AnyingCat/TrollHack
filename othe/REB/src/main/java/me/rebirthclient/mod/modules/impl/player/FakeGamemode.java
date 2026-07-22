//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.world.GameType;

public class FakeGamemode extends Module {
   GameType gamemode;

   public FakeGamemode() {
      super("FakeGamemode", "fake gamemode", Category.PLAYER);
   }

   @Override
   public void onTick() {
      if (mc.player != null) {
         mc.playerController.setGameType(GameType.CREATIVE);
      }
   }

   @Override
   public void onEnable() {
      this.gamemode = mc.playerController.getCurrentGameType();
   }

   @Override
   public void onDisable() {
      if (mc.player != null) {
         mc.playerController.setGameType(this.gamemode);
      }
   }
}
