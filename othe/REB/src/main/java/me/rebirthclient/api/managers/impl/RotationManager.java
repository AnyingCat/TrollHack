//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.asm.accessors.IEntityPlayerSP;
import me.rebirthclient.mod.Mod;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager extends Mod {
   private float yaw;
   private float pitch;

   public static float[] calculateAngle(Vec3d vec3d, Vec3d vec3d2) {
      double d = vec3d2.x - vec3d.x;
      double d2 = (vec3d2.y - vec3d.y) * -1.0;
      double d3 = vec3d2.z - vec3d.z;
      double d4 = (double)MathHelper.sqrt(d * d + d3 * d3);
      float f = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d3, d)) - 90.0);
      float f2 = (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(d2, d4)));
      if (f2 > 90.0F) {
         f2 = 90.0F;
      } else if (f2 < -90.0F) {
         f2 = -90.0F;
      }

      return new float[]{f, f2};
   }

   public void updateRotations() {
      this.yaw = mc.player.rotationYaw;
      this.pitch = mc.player.rotationPitch;
   }

   public void setPlayerRotations(float yaw, float pitch) {
      mc.player.rotationYaw = yaw;
      mc.player.rotationYawHead = yaw;
      mc.player.rotationPitch = pitch;
   }

   public void resetRotations() {
      mc.player.rotationYaw = this.yaw;
      mc.player.rotationYawHead = this.yaw;
      mc.player.rotationPitch = this.pitch;
   }

   public void setRotations(float yaw, float pitch) {
      mc.player.rotationYaw = yaw;
      mc.player.rotationYawHead = yaw;
      mc.player.rotationPitch = pitch;
   }

   public void lookAtPos(BlockPos pos) {
      float[] angle = MathUtil.calcAngle(
         mc.player.getPositionEyes(Wrapper.mc.getRenderPartialTicks()),
         new Vec3d((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() - 0.5F), (double)((float)pos.getZ() + 0.5F))
      );
      this.setRotations(angle[0], angle[1]);
   }

   public void lookAtVec3d(Vec3d vec3d) {
      float[] angle = MathUtil.calcAngle(
         mc.player.getPositionEyes(Wrapper.mc.getRenderPartialTicks()), new Vec3d(vec3d.x, vec3d.y, vec3d.z)
      );
      this.setRotations(angle[0], angle[1]);
   }

   public void lookAtVec3dPacket(Vec3d vec, boolean update) {
      float[] angle = this.getAngle(vec);
      mc.player.connection.sendPacket(new Rotation(angle[0], angle[1], mc.player.onGround));
      if (update) {
         ((IEntityPlayerSP)mc.player).setLastReportedYaw(angle[0]);
         ((IEntityPlayerSP)mc.player).setLastReportedPitch(angle[1]);
      }
   }

   public void lookAtVec3dPacket(Vec3d vec) {
      float[] angle = this.getAngle(vec);
      mc.player.connection.sendPacket(new Rotation(angle[0], angle[1], mc.player.onGround));
   }

   public void resetRotationsPacket() {
      float[] angle = new float[]{mc.player.rotationYaw, mc.player.rotationPitch};
      mc.player.connection.sendPacket(new Rotation(angle[0], angle[1], mc.player.onGround));
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float[] getAngle(Vec3d vec) {
      Vec3d eyesPos = new Vec3d(
         mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ
      );
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{
         mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
         mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
      };
   }

   public float[] injectYawStep(float[] angle, float steps) {
      if (steps < 0.1F) {
         steps = 0.1F;
      }

      if (steps > 1.0F) {
         steps = 1.0F;
      }

      if (steps < 1.0F && angle != null) {
         float packetYaw = ((IEntityPlayerSP)mc.player).getLastReportedYaw();
         float diff = MathHelper.wrapDegrees(angle[0] - packetYaw);
         if (Math.abs(diff) > 180.0F * steps) {
            angle[0] = packetYaw + diff * (180.0F * steps / Math.abs(diff));
         }
      }

      return new float[]{angle[0], angle[1]};
   }

   public int getYaw4D() {
      return MathHelper.floor((double)(mc.player.rotationYaw * 4.0F / 360.0F) + 0.5) & 3;
   }

   public String getDirection4D(boolean northRed) {
      int yaw = this.getYaw4D();
      if (yaw == 0) {
         return "South (+Z)";
      } else if (yaw == 1) {
         return "West (-X)";
      } else if (yaw == 2) {
         return (northRed ? "Â§c" : "") + "North (-Z)";
      } else {
         return yaw == 3 ? "East (+X)" : "Loading...";
      }
   }

   public boolean isInFov(BlockPos pos) {
      int yaw = this.getYaw4D();
      if (yaw == 0 && (double)pos.getZ() - mc.player.getPositionVector().z < 0.0) {
         return false;
      } else if (yaw == 1 && (double)pos.getX() - mc.player.getPositionVector().x > 0.0) {
         return false;
      } else if (yaw == 2 && (double)pos.getZ() - mc.player.getPositionVector().z > 0.0) {
         return false;
      } else {
         return yaw != 3 || (double)pos.getX() - mc.player.getPositionVector().x >= 0.0;
      }
   }
}
