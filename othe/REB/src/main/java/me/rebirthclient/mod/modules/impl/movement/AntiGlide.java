//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovementInput;

public class AntiGlide extends Module {
   private final Setting<Boolean> onGround = this.add(new Setting<>("OnGround", true));
   private final Setting<Boolean> ice = this.add(new Setting<>("Ice", true));

   public AntiGlide() {
      super("AntiGlide", "Prevents inertial moving", Category.MOVEMENT);
   }

   @Override
   public void onDisable() {
      this.setIceSlipperiness(0.98F);
   }

   @Override
   public void onUpdate() {
      if (!HoleSnap.INSTANCE.isOn() && !AutoCenter.INSTANCE.isOn()) {
         if (!this.onGround.getValue() || mc.player.onGround) {
            MovementInput input = mc.player.movementInput;
            if ((double)input.moveForward == 0.0 && (double)input.moveStrafe == 0.0) {
               mc.player.motionX = 0.0;
               mc.player.motionZ = 0.0;
            }

            if (this.ice.getValue() && mc.player.getRidingEntity() == null) {
               this.setIceSlipperiness(0.6F);
            } else {
               this.setIceSlipperiness(0.98F);
            }
         }
      }
   }

   private void setIceSlipperiness(float in) {
      Blocks.ICE.setDefaultSlipperiness(in);
      Blocks.FROSTED_ICE.setDefaultSlipperiness(in);
      Blocks.PACKED_ICE.setDefaultSlipperiness(in);
   }
}
