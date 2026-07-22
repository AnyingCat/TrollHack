//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.BlockClip;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoCenter extends Module {
   public static AutoCenter INSTANCE;
   private final Setting<Boolean> reset = this.add(new Setting<>("Reset", true));

   public AutoCenter() {
      super("AutoCenter", "move center", Category.MOVEMENT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      this.doCenter();
   }

   @Override
   public void onEnable() {
      this.doCenter();
   }

   private void doCenter() {
      if (!Clip.INSTANCE.isOn() && !BlockClip.INSTANCE.isOn() && !this.isFlying(mc.player)) {
         BlockPos blockPos = EntityUtil.getPlayerPos();
         if (mc.player.posX - (double)blockPos.getX() - 0.5 <= 0.2
            && mc.player.posX - (double)blockPos.getX() - 0.5 >= -0.2
            && mc.player.posZ - (double)blockPos.getZ() - 0.5 <= 0.2
            && mc.player.posZ - 0.5 - (double)blockPos.getZ() >= -0.2) {
            this.disable();
         } else {
            mc.player.motionX = ((double)blockPos.getX() + 0.5 - mc.player.posX) / 2.0;
            mc.player.motionZ = ((double)blockPos.getZ() + 0.5 - mc.player.posZ) / 2.0;
         }
      } else {
         this.disable();
      }
   }

   private boolean isFlying(EntityPlayer player) {
      return player.isElytraFlying() || player.capabilities.isFlying;
   }

   @Override
   public void onDisable() {
      if (this.reset.getValue()) {
         mc.player.motionX = 0.0;
         mc.player.motionZ = 0.0;
      }
   }

   @SubscribeEvent
   public void onInput(InputUpdateEvent event) {
      if (event.getMovementInput() instanceof MovementInputFromOptions) {
         MovementInput movementInput = event.getMovementInput();
         movementInput.moveForward = 0.0F;
         movementInput.moveStrafe = 0.0F;
         movementInput.forwardKeyDown = false;
         movementInput.backKeyDown = false;
         movementInput.leftKeyDown = false;
         movementInput.rightKeyDown = false;
      }
   }
}
