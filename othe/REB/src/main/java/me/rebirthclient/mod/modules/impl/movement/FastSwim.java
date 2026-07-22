//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public final class FastSwim extends Module {
   public static FastSwim INSTANCE = new FastSwim();
   private final Setting<Float> speed = this.add(new Setting<>("Speed", 5.0F, 1.0F, 10.0F));
   private final Setting<Float> verticalSpeed = this.add(new Setting<>("VerticalSpeed", 2.0F, 1.0F, 10.0F));
   private final Setting<Boolean> glide = this.add(new Setting<>("Glide", true).setParent());
   private final Setting<Float> glideSpeed = this.add(new Setting<>("GlideSpeed", 2.0F, 1.0F, 10.0F, v -> this.glide.isOpen()));

   public FastSwim() {
      super("FastSwim", "Allows you fast swim", Category.MOVEMENT);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onMove(MoveEvent event) {
      if (!fullNullCheck()) {
         if (mc.player.isInLava() || mc.player.isInWater()) {
            mc.player.capabilities.isFlying = false;
            mc.player.motionY = 0.0;
            event.setY(0.0);
            this.setMoveSpeed(event, (double)(this.speed.getValue() / 20.0F));
            if (this.glide.getValue() && !mc.player.onGround) {
               event.setY((double)(-0.007875F * this.glideSpeed.getValue()));
               mc.player.motionY = (double)(-0.007875F * this.glideSpeed.getValue());
            }

            if (MovementUtil.isJumping()) {
               event.setY(mc.player.motionY + (double)this.verticalSpeed.getValue().floatValue() / 20.0);
               mc.player.motionY += (double)this.verticalSpeed.getValue().floatValue() / 20.0;
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown()
               || InventoryMove.INSTANCE.isOn()
                  && InventoryMove.INSTANCE.sneak.getValue()
                  && Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
               event.setY(mc.player.motionY - (double)this.verticalSpeed.getValue().floatValue() / 20.0);
               mc.player.motionY -= (double)this.verticalSpeed.getValue().floatValue() / 20.0;
            }
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
