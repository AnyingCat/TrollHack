//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public final class Flight extends Module {
   public static Flight INSTANCE = new Flight();
   private final Setting<Float> speed = this.add(new Setting<>("Speed", 1.0F, 0.1F, 10.0F));
   private final Setting<Float> verticalSpeed = this.add(new Setting<>("VerticalSpeed", 1.0F, 0.1F, 10.0F));
   private final Setting<Boolean> glide = this.add(new Setting<>("Glide", true));

   public Flight() {
      super("Flight", "Allows you to fly", Category.MOVEMENT);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onMove(MoveEvent event) {
      if (!fullNullCheck()) {
         mc.player.capabilities.isFlying = false;
         mc.player.motionY = 0.0;
         this.setMoveSpeed(event, (double)this.speed.getValue().floatValue());
         if (this.glide.getValue() && !mc.player.onGround) {
            event.setY(-0.0315F);
            mc.player.motionY = -0.0315F;
         }

         if (MovementUtil.isJumping()) {
            event.setY(mc.player.motionY + (double)this.verticalSpeed.getValue().floatValue());
            mc.player.motionY += (double)this.verticalSpeed.getValue().floatValue();
         }

         if (mc.gameSettings.keyBindSneak.isKeyDown()
            || InventoryMove.INSTANCE.isOn() && InventoryMove.INSTANCE.sneak.getValue() && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            event.setY(mc.player.motionY - (double)this.verticalSpeed.getValue().floatValue());
            mc.player.motionY -= (double)this.verticalSpeed.getValue().floatValue();
         }
      }
   }

   private void setMoveSpeed(MoveEvent event, double speed) {
      double forward = (double)mc.player.movementInput.moveForward;
      double strafe = (double)mc.player.movementInput.moveStrafe;
      float yaw = mc.player.rotationYaw;
      if (forward == 0.0 && strafe == 0.0) {
         event.setX(0.0);
         event.setZ(0.0);
         mc.player.motionX = 0.0;
         mc.player.motionZ = 0.0;
      } else {
         if (forward != 0.0) {
            if (strafe > 0.0) {
               yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
               yaw += (float)(forward > 0.0 ? 45 : -45);
            }

            strafe = 0.0;
            if (forward > 0.0) {
               forward = 1.0;
            } else if (forward < 0.0) {
               forward = -1.0;
            }
         }

         double x = forward * speed * -Math.sin(Math.toRadians((double)yaw)) + strafe * speed * Math.cos(Math.toRadians((double)yaw));
         double z = forward * speed * Math.cos(Math.toRadians((double)yaw)) - strafe * speed * -Math.sin(Math.toRadians((double)yaw));
         event.setX(x);
         event.setZ(z);
         mc.player.motionX = x;
         mc.player.motionZ = z;
      }
   }
}
