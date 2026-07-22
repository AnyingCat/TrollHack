//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.movement;

import java.text.DecimalFormat;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.BlockClip;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer.Position;

public class Step extends Module {
   public static Step INSTANCE;
   private final Setting<Double> height = this.add(new Setting<>("Height", 2.5, 0.5, 3.5));
   private final Setting<Step.Mode> mode = this.add(new Setting<>("Mode", Step.Mode.Vanilla));
   private final Setting<Boolean> pauseBurrow = this.add(new Setting<>("PauseBurrow", true));
   private final Setting<Boolean> pauseSneak = this.add(new Setting<>("PauseSneak", true));
   private final Setting<Boolean> pauseWeb = this.add(new Setting<>("PauseWeb", true));
   private final Setting<Boolean> onlyMoving = this.add(new Setting<>("OnlyMoving", true));

   public Step() {
      super("Step", "step", Category.MOVEMENT);
      INSTANCE = this;
   }

   public static double[] forward(double speed) {
      float forward = mc.player.movementInput.moveForward;
      float side = mc.player.movementInput.moveStrafe;
      float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
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

   @Override
   public void onUpdate() {
      if ((
            mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.PISTON_HEAD
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.ENDER_CHEST
               || mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.BEDROCK
         )
         && this.pauseBurrow.getValue()) {
         mc.player.stepHeight = 0.1F;
      } else if ((
            mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.PISTON_HEAD
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.OBSIDIAN
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.ENDER_CHEST
               || mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.BEDROCK
         )
         && this.pauseBurrow.getValue()) {
         mc.player.stepHeight = 0.1F;
      } else if (this.pauseWeb.getValue() && mc.player.isInWeb) {
         mc.player.stepHeight = 0.1F;
      } else if (SneakManager.isSneaking && this.pauseSneak.getValue()) {
         mc.player.stepHeight = 0.1F;
      } else if (this.onlyMoving.getValue() && !MovementUtil.isMoving() && HoleSnap.INSTANCE.isOff()) {
         mc.player.stepHeight = 0.1F;
      } else if (Clip.INSTANCE.isOn() || BlockClip.INSTANCE.isOn()) {
         mc.player.stepHeight = 0.1F;
      } else if (!mc.player.isInWater()
         && !mc.player.isInLava()
         && !mc.player.isOnLadder()
         && !mc.gameSettings.keyBindJump.isKeyDown()) {
         if (this.mode.getValue() == Step.Mode.Normal) {
            mc.player.stepHeight = 0.6F;
            double[] dir = forward(0.1);
            boolean twofive = false;
            boolean two = false;
            boolean onefive = false;
            boolean one = false;
            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 2.6, dir[1])).isEmpty()
               && !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 2.4, dir[1])).isEmpty()) {
               twofive = true;
            }

            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 2.1, dir[1])).isEmpty()
               && !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 1.9, dir[1])).isEmpty()) {
               two = true;
            }

            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 1.6, dir[1])).isEmpty()
               && !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 1.4, dir[1])).isEmpty()) {
               onefive = true;
            }

            if (mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 1.0, dir[1])).isEmpty()
               && !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(dir[0], 0.6, dir[1])).isEmpty()) {
               one = true;
            }

            if (mc.player.collidedHorizontally
               && (mc.player.moveForward != 0.0F || mc.player.moveStrafing != 0.0F)
               && mc.player.onGround) {
               if (one && this.height.getValue() >= 1.0) {
                  double[] oneOffset = new double[]{0.42, 0.753};

                  for(int i = 0; i < oneOffset.length; ++i) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + oneOffset[i],
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                  }

                  mc.player.setPosition(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ);
               }

               if (onefive && this.height.getValue() >= 1.5) {
                  double[] oneFiveOffset = new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};

                  for(int i = 0; i < oneFiveOffset.length; ++i) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + oneFiveOffset[i],
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                  }

                  mc.player.setPosition(mc.player.posX, mc.player.posY + 1.5, mc.player.posZ);
               }

               if (two && this.height.getValue() >= 2.0) {
                  double[] twoOffset = new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};

                  for(int i = 0; i < twoOffset.length; ++i) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + twoOffset[i],
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                  }

                  mc.player.setPosition(mc.player.posX, mc.player.posY + 2.0, mc.player.posZ);
               }

               if (twofive && this.height.getValue() >= 2.5) {
                  double[] twoFiveOffset = new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};

                  for(int i = 0; i < twoFiveOffset.length; ++i) {
                     mc.player
                        .connection
                        .sendPacket(
                           new Position(
                              mc.player.posX,
                              mc.player.posY + twoFiveOffset[i],
                              mc.player.posZ,
                              mc.player.onGround
                           )
                        );
                  }

                  mc.player.setPosition(mc.player.posX, mc.player.posY + 2.5, mc.player.posZ);
               }
            }
         }

         if (this.mode.getValue() == Step.Mode.Vanilla) {
            DecimalFormat df = new DecimalFormat("#");
            mc.player.stepHeight = Float.parseFloat(df.format(this.height.getValue()));
         }
      } else {
         mc.player.stepHeight = 0.1F;
      }
   }

   @Override
   public String getInfo() {
      return this.mode.getValue().name();
   }

   @Override
   public void onDisable() {
      mc.player.stepHeight = 0.6F;
   }

   public static enum Mode {
      Vanilla,
      Normal;
   }
}
