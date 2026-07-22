//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import me.rebirthclient.api.util.Vector3f;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil implements Wrapper {
   public static int[] toRGBAArray(int colorBuffer) {
      return new int[]{colorBuffer >> 16 & 0xFF, colorBuffer >> 8 & 0xFF, colorBuffer & 0xFF};
   }

   public static float random(float min, float max) {
      return (float)(Math.random() * (double)(max - min) + (double)min);
   }

   public static Vec3d extrapolatePlayerPosition(EntityPlayer player, int ticks) {
      Vec3d lastPos = new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
      Vec3d currentPos = new Vec3d(player.posX, player.posY, player.posZ);
      double distance = multiply(player.motionX) + multiply(player.motionY) + multiply(player.motionZ);
      Vec3d tempVec = calculateLine(lastPos, currentPos, distance * (double)ticks);
      return new Vec3d(tempVec.x, player.posY, tempVec.z);
   }

   public static Vector3f mix(Vector3f first, Vector3f second, float factor) {
      return new Vector3f(
         first.x * (1.0F - factor) + second.x * factor, first.y * (1.0F - factor) + second.y * factor, first.z * (1.0F - factor) + first.z * factor
      );
   }

   public static double multiply(double one) {
      return one * one;
   }

   public static Vec3d calculateLine(Vec3d x1, Vec3d x2, double distance) {
      double length = Math.sqrt(
         multiply(x2.x - x1.x) + multiply(x2.y - x1.y) + multiply(x2.z - x1.z)
      );
      double unitSlopeX = (x2.x - x1.x) / length;
      double unitSlopeY = (x2.y - x1.y) / length;
      double unitSlopeZ = (x2.z - x1.z) / length;
      double x = x1.x + unitSlopeX * distance;
      double y = x1.y + unitSlopeY * distance;
      double z = x1.z + unitSlopeZ * distance;
      return new Vec3d(x, y, z);
   }

   public static float randomBetween(float min, float max) {
      return min + new Random().nextFloat() * (max - min);
   }

   public static int randomBetween(int min, int max) {
      return min + new Random().nextInt() * (max - min);
   }

   public static int clamp(int num, int min, int max) {
      return num < min ? min : Math.min(num, max);
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double clamp(double num, double min, double max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double square(double input) {
      return input * input;
   }

   public static Vec3d roundVec(Vec3d vec3d, int places) {
      return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
   }

   public static double round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = BigDecimal.valueOf(value);
         bd = bd.setScale(places, RoundingMode.FLOOR);
         return bd.doubleValue();
      }
   }

   public static float round(float value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = BigDecimal.valueOf((double)value);
         bd = bd.setScale(places, RoundingMode.FLOOR);
         return bd.floatValue();
      }
   }

   public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
      LinkedList<Entry<K, V>> list = new LinkedList<>(map.entrySet());
      if (descending) {
         list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
      } else {
         list.sort(Entry.comparingByValue());
      }

      LinkedHashMap result = new LinkedHashMap();

      for(Entry entry : list) {
         result.put(entry.getKey(), entry.getValue());
      }

      return result;
   }

   public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difZ = to.z - from.z;
      return new float[]{(float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)};
   }

   public static float[] calcAngle(Vec3d from, Vec3d to) {
      double difX = to.x - from.x;
      double difY = (to.y - from.y) * -1.0;
      double difZ = to.z - from.z;
      double dist = (double)MathHelper.sqrt(difX * difX + difZ * difZ);
      return new float[]{
         (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))
      };
   }

   public static List<Vec3d> getBlockBlocks(Entity entity) {
      ArrayList<Vec3d> vec3ds = new ArrayList();
      AxisAlignedBB bb = entity.getEntityBoundingBox();
      double y = entity.posY;
      double minX = round(bb.minX, 0);
      double minZ = round(bb.minZ, 0);
      double maxX = round(bb.maxX, 0);
      double maxZ = round(bb.maxZ, 0);
      if (minX != maxX) {
         vec3ds.add(new Vec3d(minX, y, minZ));
         vec3ds.add(new Vec3d(maxX, y, minZ));
         if (minZ != maxZ) {
            vec3ds.add(new Vec3d(minX, y, maxZ));
            vec3ds.add(new Vec3d(maxX, y, maxZ));
            return vec3ds;
         }
      } else if (minZ != maxZ) {
         vec3ds.add(new Vec3d(minX, y, minZ));
         vec3ds.add(new Vec3d(minX, y, maxZ));
         return vec3ds;
      }

      vec3ds.add(entity.getPositionVector());
      return vec3ds;
   }

   public static Vec3d[] convertVectors(Vec3d vec3d, Vec3d[] input) {
      Vec3d[] out = new Vec3d[input.length];

      for(int i = 0; i < input.length; ++i) {
         out[i] = vec3d.add(input[i]);
      }

      return out;
   }

   public static double[] directionSpeed(double speed) {
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

   public static float animate(float in, float target, float delta) {
      float out = (target - in) / Math.max((float)Minecraft.getDebugFPS(), 5.0F) * 15.0F;
      if (out > 0.0F) {
         out = Math.max(delta, out);
         out = Math.min(target - in, out);
      } else if (out < 0.0F) {
         out = Math.min(-delta, out);
         out = Math.max(target - in, out);
      }

      return in + out;
   }

   public static double animate(double target, double current, double delta) {
      boolean larger = target > current;
      if (delta < 0.0) {
         delta = 0.0;
      } else if (delta > 1.0) {
         delta = 1.0;
      }

      double dif = Math.max(target, current) - Math.min(target, current);
      double factor = dif * delta;
      if (factor < 0.1) {
         factor = 0.1;
      }

      if (larger) {
         current += factor;
      } else {
         current -= factor;
      }

      return current;
   }

   public static Integer increaseNumber(int input, int target, int delta) {
      return input < target ? input + delta : target;
   }

   public static Integer decreaseNumber(int input, int target, int delta) {
      return input > target ? input - delta : target;
   }

   public static Float increaseNumber(float input, float target, float delta) {
      return input < target ? input + delta : target;
   }

   public static Float decreaseNumber(float input, float target, float delta) {
      return input > target ? input - delta : target;
   }

   public static double normalize(double value, double min, double max) {
      return (value - min) / (max - min);
   }
}
