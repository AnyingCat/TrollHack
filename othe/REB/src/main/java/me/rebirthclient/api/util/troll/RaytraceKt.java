//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.troll;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public final class RaytraceKt {
   public static RayTraceResult rayTrace(
      World $this$rayTrace, Vec3d start, Vec3d end, int maxAttempt, Function2<? super BlockPos, ? super IBlockState, ? extends RayTraceAction> function
   ) {
      double currentX = start.x;
      double currentY = start.y;
      double currentZ = start.z;
      int currentBlockX = (int)(currentX + 1.07374182E9F) - 1073741824;
      int currentBlockY = (int)(currentY + 1.07374182E9F) - 1073741824;
      int currentBlockZ = (int)(currentZ + 1.07374182E9F) - 1073741824;
      MutableBlockPos blockPos = new MutableBlockPos(currentBlockX, currentBlockY, currentBlockZ);
      IBlockState startBlockState = $this$rayTrace.getBlockState(blockPos);
      double endX = end.x;
      double endY = end.y;
      double endZ = end.z;
      RayTraceAction action = function.invoke(blockPos, startBlockState);
      if (action == RayTraceAction.Null.INSTANCE) {
         return null;
      } else {
         if (action == RayTraceAction.Calc.INSTANCE) {
            RayTraceResult raytrace = raytrace(startBlockState, $this$rayTrace, blockPos, currentX, currentY, currentZ, endX, endY, endZ);
            if (raytrace != null) {
               return raytrace;
            }
         } else if (action instanceof RayTraceAction.Result) {
            return ((RayTraceAction.Result)action).getRayTraceResult();
         }

         int endBlockX = (int)(endX + 1.07374182E9F) - 1073741824;
         int endBlockY = (int)(endY + 1.07374182E9F) - 1073741824;
         int endBlockZ = (int)(endZ + 1.07374182E9F) - 1073741824;
         int count = maxAttempt;

         while(count-- >= 0) {
            if (currentBlockX == endBlockX && currentBlockY == endBlockY && currentBlockZ == endBlockZ) {
               return null;
            }

            int nextX = 999;
            int nextY = 999;
            int nextZ = 999;
            double stepX = 999.0;
            double stepY = 999.0;
            double stepZ = 999.0;
            double diffX = end.x - currentX;
            double diffY = end.y - currentY;
            double diffZ = end.z - currentZ;
            if (endBlockX > currentBlockX) {
               nextX = currentBlockX + 1;
               stepX = ((double)nextX - currentX) / diffX;
            } else if (endBlockX < currentBlockX) {
               nextX = currentBlockX;
               stepX = ((double)currentBlockX - currentX) / diffX;
            }

            if (endBlockY > currentBlockY) {
               nextY = currentBlockY + 1;
               stepY = ((double)nextY - currentY) / diffY;
            } else if (endBlockY < currentBlockY) {
               nextY = currentBlockY;
               stepY = ((double)currentBlockY - currentY) / diffY;
            }

            if (endBlockZ > currentBlockZ) {
               nextZ = currentBlockZ + 1;
               stepZ = ((double)nextZ - currentZ) / diffZ;
            } else if (endBlockZ < currentBlockZ) {
               nextZ = currentBlockZ;
               stepZ = ((double)currentBlockZ - currentZ) / diffZ;
            }

            if (stepX < stepY && stepX < stepZ) {
               currentX = (double)nextX;
               currentY += diffY * stepX;
               currentZ += diffZ * stepX;
               currentBlockX = nextX - (endBlockX - currentBlockX >>> 31);
               currentBlockY = (int)(currentY + 1.07374182E9F) - 1073741824;
               currentBlockZ = (int)(currentZ + 1.07374182E9F) - 1073741824;
            } else if (stepY < stepZ) {
               currentX += diffX * stepY;
               currentY = (double)nextY;
               currentZ += diffZ * stepY;
               currentBlockX = (int)(currentX + 1.07374182E9F) - 1073741824;
               currentBlockY = nextY - (endBlockY - currentBlockY >>> 31);
               currentBlockZ = (int)(currentZ + 1.07374182E9F) - 1073741824;
            } else {
               currentX += diffX * stepZ;
               currentY += diffY * stepZ;
               currentZ = (double)nextZ;
               currentBlockX = (int)(currentX + 1.07374182E9F) - 1073741824;
               currentBlockY = (int)(currentY + 1.07374182E9F) - 1073741824;
               currentBlockZ = nextZ - (endBlockZ - currentBlockZ >>> 31);
            }

            blockPos.setPos(currentBlockX, currentBlockY, currentBlockZ);
            IBlockState blockState = $this$rayTrace.getBlockState(blockPos);
            RayTraceAction action2 = function.invoke(blockPos, blockState);
            if (action2 == RayTraceAction.Null.INSTANCE) {
               return null;
            }

            if (action2 == RayTraceAction.Calc.INSTANCE) {
               RayTraceResult raytrace2 = raytrace(blockState, $this$rayTrace, blockPos, currentX, currentY, currentZ, endX, endY, endZ);
               if (raytrace2 != null) {
                  return raytrace2;
               }
            } else if (action2 instanceof RayTraceAction.Result) {
               return ((RayTraceAction.Result)action2).getRayTraceResult();
            }
         }

         return null;
      }
   }

   private static RayTraceResult raytrace(
      IBlockState $this$raytrace, World world, MutableBlockPos blockPos, double x1, double y1, double z1, double x2, double y2, double z2
   ) {
      float x1f = (float)(x1 - (double)blockPos.getX());
      float y1f = (float)(y1 - (double)blockPos.getY());
      float z1f = (float)(z1 - (double)blockPos.getZ());
      AxisAlignedBB box = $this$raytrace.getBoundingBox(world, blockPos);
      float minX = (float)box.minX;
      float minY = (float)box.minY;
      float minZ = (float)box.minZ;
      float maxX = (float)box.maxX;
      float maxY = (float)box.maxY;
      float maxZ = (float)box.maxZ;
      float xDiff = (float)(x2 - (double)blockPos.getX()) - x1f;
      float yDiff = (float)(y2 - (double)blockPos.getY()) - y1f;
      float zDiff = (float)(z2 - (double)blockPos.getZ()) - z1f;
      float hitVecX = Float.NaN;
      float hitVecY = Float.NaN;
      float hitVecZ = Float.NaN;
      EnumFacing side = EnumFacing.WEST;
      boolean none = true;
      if (xDiff * xDiff >= 1.0E-7F) {
         float factorMin = (minX - x1f) / xDiff;
         if (0.0 <= (double)factorMin && (double)factorMin <= 1.0) {
            float newY = y1f + yDiff * factorMin;
            float newZ = z1f + zDiff * factorMin;
            if (minY <= newY && newY <= maxY && minZ <= newZ && newZ <= maxZ) {
               hitVecX = x1f + xDiff * factorMin;
               hitVecY = newY;
               hitVecZ = newZ;
               none = false;
            }
         } else {
            float factorMax = (maxX - x1f) / xDiff;
            if (0.0 <= (double)factorMax && (double)factorMax <= 1.0) {
               float newY2 = y1f + yDiff * factorMax;
               float newZ2 = z1f + zDiff * factorMax;
               if (minY <= newY2 && newY2 <= maxY && minZ <= newZ2 && newZ2 <= maxZ) {
                  hitVecX = x1f + xDiff * factorMax;
                  hitVecY = newY2;
                  hitVecZ = newZ2;
                  side = EnumFacing.EAST;
                  none = false;
               }
            }
         }
      }

      if (yDiff * yDiff >= 1.0E-7F) {
         float factorMin = (minY - y1f) / yDiff;
         if (0.0F <= factorMin && factorMin <= 1.0F) {
            float newX3 = x1f + xDiff * factorMin;
            float newZ = z1f + zDiff * factorMin;
            label121:
            if (minX <= newX3 && newX3 <= maxX && minZ <= newZ && newZ <= maxZ) {
               float newY3 = y1f + yDiff * factorMin;
               if (!none) {
                  float $this$sq$iv$iv = newX3 - x1f;
                  float n3 = $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = newY3 - y1f;
                  float n4 = n3 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = newZ - z1f;
                  float n5 = n4 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecX - x1f;
                  float n6 = $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecY - y1f;
                  float n7 = n6 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecZ - z1f;
                  if (n5 >= n7 + $this$sq$iv$iv * $this$sq$iv$iv) {
                     break label121;
                  }
               }

               hitVecX = newX3;
               hitVecY = newY3;
               hitVecZ = newZ;
               side = EnumFacing.DOWN;
               none = false;
            }
         } else {
            float factorMax = (maxY - y1f) / yDiff;
            if (0.0F <= factorMax && factorMax <= 1.0F) {
               float newX4 = x1f + xDiff * factorMax;
               float newZ2 = z1f + zDiff * factorMax;
               label111:
               if (minX <= newX4 && newX4 <= maxX && minZ <= newZ2 && newZ2 <= maxZ) {
                  float newY4 = y1f + yDiff * factorMax;
                  if (!none) {
                     float $this$sq$iv$iv2 = newX4 - x1f;
                     float n8 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = newY4 - y1f;
                     float n9 = n8 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = newZ2 - z1f;
                     float n10 = n9 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecX - x1f;
                     float n11 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecY - y1f;
                     float n12 = n11 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecZ - z1f;
                     if (n10 >= n12 + $this$sq$iv$iv2 * $this$sq$iv$iv2) {
                        break label111;
                     }
                  }

                  hitVecX = newX4;
                  hitVecY = newY4;
                  hitVecZ = newZ2;
                  side = EnumFacing.UP;
                  none = false;
               }
            }
         }
      }

      if ((double)(zDiff * zDiff) >= 1.0E-7) {
         float factorMin = (minZ - z1f) / zDiff;
         if (0.0F <= factorMin && factorMin <= 1.0F) {
            float newX3 = x1f + xDiff * factorMin;
            float newY2 = y1f + yDiff * factorMin;
            label97:
            if (minX <= newX3 && newX3 <= maxX && minY <= newY2 && newY2 <= maxY) {
               float newZ2 = z1f + zDiff * factorMin;
               if (!none) {
                  float $this$sq$iv$iv = newX3 - x1f;
                  float n13 = $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = newY2 - y1f;
                  float n14 = n13 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = newZ2 - z1f;
                  float n15 = n14 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecX - x1f;
                  float n16 = $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecY - y1f;
                  float n17 = n16 + $this$sq$iv$iv * $this$sq$iv$iv;
                  $this$sq$iv$iv = hitVecZ - z1f;
                  if (n15 >= n17 + $this$sq$iv$iv * $this$sq$iv$iv) {
                     break label97;
                  }
               }

               hitVecX = newX3;
               hitVecY = newY2;
               hitVecZ = newZ2;
               side = EnumFacing.NORTH;
               none = false;
            }
         } else {
            float factorMax = (maxZ - z1f) / zDiff;
            if (0.0F <= factorMax && factorMax <= 1.0F) {
               float newX4 = x1f + xDiff * factorMax;
               float newY3 = y1f + yDiff * factorMax;
               label87:
               if (minX <= newX4 && newX4 <= maxX && minY <= newY3 && newY3 <= maxY) {
                  float newZ3 = z1f + zDiff * factorMax;
                  if (!none) {
                     float $this$sq$iv$iv2 = newX4 - x1f;
                     float n18 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = newY3 - y1f;
                     float n19 = n18 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = newZ3 - z1f;
                     float n20 = n19 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecX - x1f;
                     float n21 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecY - y1f;
                     float n22 = n21 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                     $this$sq$iv$iv2 = hitVecZ - z1f;
                     if (n20 >= n22 + $this$sq$iv$iv2 * $this$sq$iv$iv2) {
                        break label87;
                     }
                  }

                  hitVecX = newX4;
                  hitVecY = newY3;
                  hitVecZ = newZ3;
                  side = EnumFacing.SOUTH;
                  none = false;
               }
            }
         }
      }

      RayTraceResult rayTraceResult;
      if (!none) {
         Vec3d hitVec = new Vec3d(
            (double)hitVecX + (double)blockPos.getX(),
            (double)hitVecY + (double)blockPos.getY(),
            (double)hitVecZ + (double)blockPos.getZ()
         );
         rayTraceResult = new RayTraceResult(hitVec, side, blockPos.toImmutable());
      } else {
         rayTraceResult = null;
      }

      return rayTraceResult;
   }
}
