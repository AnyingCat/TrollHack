//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Sprint extends Module {
   private final Setting<Sprint.Mode> mode = this.add(new Setting<>("Mode", Sprint.Mode.RAGE));

   public Sprint() {
      super("Sprint", "Sprints", Category.MOVEMENT);
   }

   public static boolean isMoving() {
      return mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F;
   }

   @Override
   public String getInfo() {
      return Managers.TEXT.normalizeCases(this.mode.getValue());
   }

   @Override
   public void onTick() {
      if (this.mode.getValue() == Sprint.Mode.RAGE && isMoving()) {
         mc.player.setSprinting(true);
      } else if (this.mode.getValue() == Sprint.Mode.LEGIT
         && mc.player.moveForward > 0.1F
         && !mc.player.collidedHorizontally
         && !SneakManager.isSneaking) {
         mc.player.setSprinting(true);
      }
   }

   private static enum Mode {
      RAGE,
      LEGIT;
   }
}
