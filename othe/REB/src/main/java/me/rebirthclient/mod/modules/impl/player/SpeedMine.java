//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class SpeedMine extends Module {
   private final Setting<Float> startDamage = this.register(new Setting<>("StartDamage", 0.1F, 0.0F, 1.0F));
   private final Setting<Float> endDamage = this.register(new Setting<>("EndDamage", 0.9F, 0.0F, 1.0F));

   public SpeedMine() {
      super("SpeedMine", "Allows you to dig-quickly", Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (!mc.player.capabilities.isCreativeMode) {
         if (mc.playerController.curBlockDamageMP < this.startDamage.getValue()) {
            mc.playerController.curBlockDamageMP = this.startDamage.getValue();
         }

         if (mc.playerController.curBlockDamageMP >= this.endDamage.getValue()) {
            mc.playerController.curBlockDamageMP = 1.0F;
         }
      }
   }
}
