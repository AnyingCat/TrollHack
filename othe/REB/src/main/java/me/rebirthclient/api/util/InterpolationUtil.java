//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class InterpolationUtil implements Wrapper {
   public static Vec3d getInterpolatedPos(Entity entity, float partialTicks, boolean wrap) {
      Vec3d amount = new Vec3d(
         (entity.posX - entity.lastTickPosX) * (double)partialTicks,
         (entity.posY - entity.lastTickPosY) * (double)partialTicks,
         (entity.posZ - entity.lastTickPosZ) * (double)partialTicks
      );
      Vec3d vec = new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(amount);
      return wrap ? vec.subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ) : vec;
   }

   public static AxisAlignedBB getAxisAlignedBB(BlockPos pos, double size) {
      AxisAlignedBB bb = mc.world.getBlockState(pos).getSelectedBoundingBox(mc.world, pos);
      Vec3d center = bb.getCenter();
      return new AxisAlignedBB(
         center.x - (bb.maxX - bb.minX) * size,
         center.y - (bb.maxY - bb.minX) * size,
         center.z - (bb.maxZ - bb.minZ) * size,
         center.x + (bb.maxX - bb.minX) * size,
         center.y + (bb.maxY - bb.minY) * size,
         center.z + (bb.maxZ - bb.minZ) * size
      );
   }

   public static AxisAlignedBB getInterpolatedAxis(AxisAlignedBB bb) {
      return new AxisAlignedBB(
         bb.minX - mc.getRenderManager().viewerPosX,
         bb.minY - mc.getRenderManager().viewerPosY,
         bb.minZ - mc.getRenderManager().viewerPosZ,
         bb.maxX - mc.getRenderManager().viewerPosX,
         bb.maxY - mc.getRenderManager().viewerPosY,
         bb.maxZ - mc.getRenderManager().viewerPosZ
      );
   }

   public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
      return interpolateEntity(entity, ticks)
         .subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
   }

   public static Vec3d interpolateEntity(Entity entity, float time) {
      return new Vec3d(
         entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)time,
         entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)time,
         entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)time
      );
   }

   public static double getInterpolatedDouble(double pre, double current, float partialTicks) {
      return pre + (current - pre) * (double)partialTicks;
   }

   public static float getInterpolatedFloat(float pre, float current, float partialTicks) {
      return pre + (current - pre) * partialTicks;
   }
}
