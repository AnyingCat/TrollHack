//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import java.util.Objects;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

public class MovementUtil implements Wrapper {
   public static boolean isMoving() {
      return (double)mc.player.moveForward != 0.0 || (double)mc.player.moveStrafing != 0.0;
   }

   public static boolean isJumping() {
      return mc.gameSettings.keyBindJump.isKeyDown()
         || InventoryMove.INSTANCE.isOn() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !(mc.currentScreen instanceof GuiChat);
   }

   public static double[] directionSpeed(double speed) {
      float forward = MathUtil.mc.player.movementInput.moveForward;
      float side = MathUtil.mc.player.movementInput.moveStrafe;
      float yaw = MathUtil.mc.player.prevRotationYaw
         + (MathUtil.mc.player.rotationYaw - MathUtil.mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (side < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)side * speed * sin;
      double posZ = (double)forward * speed * sin - (double)side * speed * cos;
      return new double[]{posX, posZ};
   }

   public static boolean anyMovementKeys() {
      return mc.player.movementInput.forwardKeyDown
         || mc.player.movementInput.backKeyDown
         || mc.player.movementInput.leftKeyDown
         || mc.player.movementInput.rightKeyDown
         || mc.player.movementInput.jump
         || mc.player.movementInput.sneak;
   }

   public static boolean noMovementKeys() {
      return !mc.player.movementInput.forwardKeyDown
         && !mc.player.movementInput.backKeyDown
         && !mc.player.movementInput.rightKeyDown
         && !mc.player.movementInput.leftKeyDown;
   }

   public static void setMoveSpeed(double speed) {
      double forward = (double)mc.player.movementInput.moveForward;
      double strafe = (double)mc.player.movementInput.moveStrafe;
      float yaw = mc.player.rotationYaw;
      if (forward == 0.0 && strafe == 0.0) {
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

         mc.player.motionX = forward * speed * -Math.sin(Math.toRadians((double)yaw)) + strafe * speed * Math.cos(Math.toRadians((double)yaw));
         mc.player.motionZ = forward * speed * Math.cos(Math.toRadians((double)yaw)) - strafe * speed * -Math.sin(Math.toRadians((double)yaw));
      }
   }

   public static void strafe(MoveEvent event, double speed) {
      if (isMoving()) {
         double[] strafe = strafe(speed);
         event.setX(strafe[0]);
         event.setZ(strafe[1]);
      } else {
         event.setX(0.0);
         event.setZ(0.0);
      }
   }

   public static double[] strafe(double speed) {
      return strafe(mc.player, speed);
   }

   public static double[] strafe(Entity entity, double speed) {
      return strafe(entity, mc.player.movementInput, speed);
   }

   public static double[] strafe(Entity entity, MovementInput movementInput, double speed) {
      float moveForward = movementInput.moveForward;
      float moveStrafe = movementInput.moveStrafe;
      float rotationYaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * mc.getRenderPartialTicks();
      if (moveForward != 0.0F) {
         if (moveStrafe > 0.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? -45 : 45);
         } else if (moveStrafe < 0.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? 45 : -45);
         }

         moveStrafe = 0.0F;
         if (moveForward > 0.0F) {
            moveForward = 1.0F;
         } else if (moveForward < 0.0F) {
            moveForward = -1.0F;
         }
      }

      double posX = (double)moveForward * speed * -Math.sin(Math.toRadians((double)rotationYaw))
         + (double)moveStrafe * speed * Math.cos(Math.toRadians((double)rotationYaw));
      double posZ = (double)moveForward * speed * Math.cos(Math.toRadians((double)rotationYaw))
         - (double)moveStrafe * speed * -Math.sin(Math.toRadians((double)rotationYaw));
      return new double[]{posX, posZ};
   }

   public static MovementInput inverse(Entity entity, double speed) {
      MovementInput input = new MovementInput();
      input.sneak = entity.isSneaking();

      for(float d = -1.0F; d <= 1.0F; ++d) {
         for(float e = -1.0F; e <= 1.0F; ++e) {
            MovementInput dummyInput = new MovementInput();
            dummyInput.moveForward = d;
            dummyInput.moveStrafe = e;
            dummyInput.sneak = entity.isSneaking();
            double[] moveVec = strafe(entity, dummyInput, speed);
            if (entity.isSneaking()) {
               moveVec[0] *= 0.3F;
               moveVec[1] *= 0.3F;
            }

            double targetMotionX = moveVec[0];
            double targetMotionZ = moveVec[1];
            if ((targetMotionX < 0.0 ? entity.motionX <= targetMotionX : entity.motionX >= targetMotionX)
               && (targetMotionZ < 0.0 ? entity.motionZ <= targetMotionZ : entity.motionZ >= targetMotionZ)) {
               input.moveForward = d;
               input.moveStrafe = e;
               break;
            }
         }
      }

      return input;
   }

   public static double getDistance2D() {
      double xDist = mc.player.posX - mc.player.prevPosX;
      double zDist = mc.player.posZ - mc.player.prevPosZ;
      return Math.sqrt(xDist * xDist + zDist * zDist);
   }

   public static double getDistance3D() {
      double xDist = mc.player.posX - mc.player.prevPosX;
      double yDist = mc.player.posY - mc.player.prevPosY;
      double zDist = mc.player.posZ - mc.player.prevPosZ;
      return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
   }

   public static double getSpeed() {
      return getSpeed(false);
   }

   public static double getSpeed(boolean slowness, double defaultSpeed) {
      if (mc.player.isPotionActive(MobEffects.SPEED)) {
         int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))).getAmplifier();
         defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (slowness && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
         int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SLOWNESS))).getAmplifier();
         defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (SneakManager.isSneaking) {
         defaultSpeed /= 5.0;
      }

      return defaultSpeed;
   }

   public static double getSpeed(boolean slowness) {
      double defaultSpeed = 0.2873;
      if (mc.player.isPotionActive(MobEffects.SPEED)) {
         int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))).getAmplifier();
         defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (slowness && mc.player.isPotionActive(MobEffects.SLOWNESS)) {
         int amplifier = ((PotionEffect)Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SLOWNESS))).getAmplifier();
         defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
      }

      if (SneakManager.isSneaking) {
         defaultSpeed /= 5.0;
      }

      return defaultSpeed;
   }

   public static double getJumpSpeed() {
      double defaultSpeed = 0.0;
      if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
         int amplifier = mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier();
         defaultSpeed += (double)(amplifier + 1) * 0.1;
      }

      return defaultSpeed;
   }

   public static boolean isInMovementDirection(double x, double y, double z) {
      if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
         return true;
      } else {
         BlockPos movingPos = new BlockPos(mc.player)
            .add(mc.player.motionX * 10000.0, 0.0, mc.player.motionZ * 10000.0);
         BlockPos antiPos = new BlockPos(mc.player)
            .add(mc.player.motionX * -10000.0, 0.0, mc.player.motionY * -10000.0);
         return movingPos.distanceSq(x, y, z) < antiPos.distanceSq(x, y, z);
      }
   }
}
