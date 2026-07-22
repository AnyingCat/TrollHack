//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class FastWeb extends Module {
   private final Setting<FastWeb.Mode> mode = this.add(new Setting<>("Mode", FastWeb.Mode.FAST));
   private final Setting<Float> fastSpeed = this.add(new Setting<>("FastSpeed", 3.0F, 0.0F, 5.0F, v -> this.mode.getValue() == FastWeb.Mode.FAST));
   private final Setting<Boolean> onlySneak = this.add(new Setting<>("OnlySneak", false));

   public FastWeb() {
      super("FastWeb", "So you don't need to keep timer on keybind", Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      Managers.TIMER.reset();
   }

   @Override
   public String getInfo() {
      return Managers.TEXT.normalizeCases(this.mode.getValue());
   }

   @Override
   public void onUpdate() {
      if (mc.player.isInWeb) {
         if ((this.mode.getValue() != FastWeb.Mode.FAST || !mc.gameSettings.keyBindSneak.isKeyDown()) && this.onlySneak.getValue()) {
            if ((this.mode.getValue() != FastWeb.Mode.STRICT || mc.player.onGround || !mc.gameSettings.keyBindSneak.isKeyDown())
               && this.onlySneak.getValue()) {
               if (!NewStep.timer) {
                  Managers.TIMER.reset();
               }
            } else {
               Managers.TIMER.set(8.0F);
            }
         } else {
            Managers.TIMER.reset();
            mc.player.motionY -= (double)this.fastSpeed.getValue().floatValue();
         }
      } else {
         if (!NewStep.timer) {
            Managers.TIMER.reset();
         }
      }
   }

   private static enum Mode {
      FAST,
      STRICT;
   }
}
