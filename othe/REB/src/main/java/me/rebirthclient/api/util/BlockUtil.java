//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;

public class BlockUtil implements Wrapper {
   public static final List<Block> canUseList = Arrays.asList(
      Blocks.ENDER_CHEST,
      Blocks.CHEST,
      Blocks.TRAPPED_CHEST,
      Blocks.CRAFTING_TABLE,
      Blocks.ANVIL,
      Blocks.BREWING_STAND,
      Blocks.HOPPER,
      Blocks.DROPPER,
      Blocks.DISPENSER,
      Blocks.TRAPDOOR,
      Blocks.ENCHANTING_TABLE
   );
   public static final List<Block> shulkerList = Arrays.asList(
      Blocks.WHITE_SHULKER_BOX,
      Blocks.ORANGE_SHULKER_BOX,
      Blocks.MAGENTA_SHULKER_BOX,
      Blocks.LIGHT_BLUE_SHULKER_BOX,
      Blocks.YELLOW_SHULKER_BOX,
      Blocks.LIME_SHULKER_BOX,
      Blocks.PINK_SHULKER_BOX,
      Blocks.GRAY_SHULKER_BOX,
      Blocks.SILVER_SHULKER_BOX,
      Blocks.CYAN_SHULKER_BOX,
      Blocks.PURPLE_SHULKER_BOX,
      Blocks.BLUE_SHULKER_BOX,
      Blocks.BROWN_SHULKER_BOX,
      Blocks.GREEN_SHULKER_BOX,
      Blocks.RED_SHULKER_BOX,
      Blocks.BLACK_SHULKER_BOX
   );

   public static boolean canBlockFacing(BlockPos pos) {
      boolean airCheck = false;

      for(EnumFacing side : EnumFacing.values()) {
         if (canClick(pos.offset(side))) {
            airCheck = true;
         }
      }

      return airCheck;
   }

   public static boolean canPlaceEnum(BlockPos pos) {
      return !canBlockFacing(pos) ? false : strictPlaceCheck(pos);
   }

   public static boolean posHasCrystal(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityEnderCrystal && new BlockPos(entity.posX, entity.posY, entity.posZ).equals(pos)) {
            return true;
         }
      }

      return false;
   }

   public static boolean strictPlaceCheck(BlockPos pos) {
      if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
         return true;
      } else {
         for(EnumFacing side : getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }

   public static List<EnumFacing> getPlacableFacings(BlockPos pos, boolean strictDirection, boolean rayTrace) {
      ArrayList<EnumFacing> validFacings = new ArrayList();

      for(EnumFacing side : EnumFacing.values()) {
         if (!getRaytrace(pos, side)) {
            getPlaceFacing(pos, strictDirection, validFacings, side);
         }
      }

      for(EnumFacing side : EnumFacing.values()) {
         if (!rayTrace || !getRaytrace(pos, side)) {
            getPlaceFacing(pos, strictDirection, validFacings, side);
         }
      }

      return validFacings;
   }

   private static boolean getRaytrace(BlockPos pos, EnumFacing side) {
      Vec3d testVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
      RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1.0F), testVec);
      return result != null && result.typeOfHit != Type.MISS;
   }

   private static void getPlaceFacing(BlockPos pos, boolean strictDirection, ArrayList<EnumFacing> validFacings, EnumFacing side) {
      BlockPos neighbour = pos.offset(side);
      if (strictDirection) {
         Vec3d eyePos = mc.player.getPositionEyes(1.0F);
         Vec3d blockCenter = new Vec3d(
            (double)neighbour.getX() + 0.5, (double)neighbour.getY() + 0.5, (double)neighbour.getZ() + 0.5
         );
         IBlockState blockState = mc.world.getBlockState(neighbour);
         boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullBlock();
         ArrayList<EnumFacing> validAxis = new ArrayList();
         validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
         validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
         validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
         if (!validAxis.contains(side.getOpposite())) {
            return;
         }
      }

      IBlockState blockState = mc.world.getBlockState(neighbour);
      if (blockState.getBlock().canCollideCheck(blockState, false) && !blockState.getMaterial().isReplaceable()) {
         validFacings.add(side);
      }
   }

   public static ArrayList<EnumFacing> checkAxis(double diff, EnumFacing negativeSide, EnumFacing positiveSide, boolean bothIfInRange) {
      ArrayList<EnumFacing> valid = new ArrayList();
      if (diff < -0.5) {
         valid.add(negativeSide);
      }

      if (diff > 0.5) {
         valid.add(positiveSide);
      }

      if (bothIfInRange) {
         if (!valid.contains(negativeSide)) {
            valid.add(negativeSide);
         }

         if (!valid.contains(positiveSide)) {
            valid.add(positiveSide);
         }
      }

      return valid;
   }

   public static boolean isAir(BlockPos pos) {
      return mc.world.isAirBlock(pos);
   }

   public static void placeBlock(BlockPos pos, boolean packet) {
      placeBlock(pos, EnumHand.MAIN_HAND, false, packet);
   }

   public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      ArrayList<BlockPos> circleblocks = new ArrayList();
      int cx = pos.getX();
      int cy = pos.getY();
      int cz = pos.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            int y = sphere ? cy - (int)r : cy;

            while(true) {
               float f = (float)y;
               float f2 = sphere ? (float)cy + r : (float)(cy + h);
               if (!(f < f2)) {
                  break;
               }

               double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));
               if (dist < (double)(r * r) && (!hollow || dist >= (double)((r - 1.0F) * (r - 1.0F)))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleblocks.add(l);
               }

               ++y;
            }
         }
      }

      return circleblocks;
   }

   public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
      return shouldCheck
         && mc.world
               .rayTraceBlocks(
                  new Vec3d(
                     mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ
                  ),
                  new Vec3d((double)pos.getX(), (double)((float)pos.getY() + height), (double)pos.getZ()),
                  false,
                  true,
                  false
               )
            != null;
   }

   public static double distanceToXZ(double x, double z) {
      double dx = mc.player.posX - x;
      double dz = mc.player.posZ - z;
      return Math.sqrt(dx * dx + dz * dz);
   }

   public static boolean canClick(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock().canCollideCheck(mc.world.getBlockState(pos), false);
   }

   public static EnumFacing getRayTraceFacing(BlockPos pos) {
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
         );
      return result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
   }

   public static boolean canPlace(BlockPos pos, double distance) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
         return false;
      } else if (!canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else if (!strictPlaceCheck(pos)) {
         return false;
      } else {
         return !checkEntity(pos);
      }
   }

   public static boolean canPlace(BlockPos pos) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
         return false;
      } else if (!canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else if (!strictPlaceCheck(pos)) {
         return false;
      } else {
         return !checkEntity(pos);
      }
   }

   public static boolean canPlace2(BlockPos pos) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !checkPlayer(pos);
      }
   }

   public static boolean canPlace2(BlockPos pos, double distance) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else {
         return !checkPlayer(pos);
      }
   }

   public static boolean canPlace3(BlockPos pos) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
         return false;
      } else if (!canBlockFacing(pos)) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else if (!strictPlaceCheck(pos)) {
         return false;
      } else {
         return !checkPlayer(pos);
      }
   }

   public static boolean canPlaceShulker(BlockPos pos) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
         return false;
      } else if (canBlockReplace(pos.down())) {
         return false;
      } else if (!canReplace(pos)) {
         return false;
      } else if (checkEntity(pos)) {
         return false;
      } else if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
         return true;
      } else {
         for(EnumFacing side : getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (canClick(pos.offset(side)) && side == EnumFacing.DOWN) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean canBlockReplace(BlockPos pos) {
      return mc.world.isAirBlock(pos)
         || getBlock(pos) == Blocks.FIRE
         || getBlock(pos) == Blocks.LAVA
         || getBlock(pos) == Blocks.FLOWING_LAVA
         || getBlock(pos) == Blocks.WATER
         || getBlock(pos) == Blocks.FLOWING_WATER;
   }

   public static boolean checkPlayer(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)
            && !(entity instanceof EntityEnderCrystal)) {
            return true;
         }
      }

      return false;
   }

   public static boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (!entity.isDead
            && !(entity instanceof EntityItem)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)) {
            return true;
         }
      }

      return false;
   }

   public static NonNullList<BlockPos> getBox(float range) {
      NonNullList positions = NonNullList.create();
      positions.addAll(
         getSphere(
            new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)),
            range,
            0,
            false,
            true,
            0
         )
      );
      return positions;
   }

   public static NonNullList<BlockPos> getBox(float range, BlockPos pos) {
      NonNullList positions = NonNullList.create();
      positions.addAll(getSphere(pos, range, 0, false, true, 0));
      return positions;
   }

   public static BlockPos vec3toBlockPos(Vec3d vec3d) {
      return new BlockPos(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
   }

   public static Vec3d blockPosToVec3(BlockPos pos) {
      return new Vec3d((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
   }

   public static BlockPos getFlooredPosition(Entity entity) {
      return new BlockPos(Math.floor(entity.posX), (double)Math.round(entity.posY), Math.floor(entity.posZ));
   }

   public static IBlockState getState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   public static Block getBlock(BlockPos pos) {
      return getState(pos).getBlock();
   }

   public static void placeCrystal(BlockPos pos, boolean rotate) {
      boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
      BlockPos obsPos = pos.down();
      RayTraceResult result = mc.world
         .rayTraceBlocks(
            new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ),
            new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5)
         );
      EnumFacing facing = result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
      Vec3d vec = new Vec3d(obsPos).add(0.5, 0.5, 0.5).add(new Vec3d(facing.getDirectionVec()).scale(0.5));
      if (rotate) {
         EntityUtil.faceVector(vec);
      }

      mc.player
         .connection
         .sendPacket(new CPacketPlayerTryUseItemOnBlock(obsPos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
      mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
   }

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean attackEntity, boolean eatingPause, boolean placeRender) {
      if (attackEntity) {
         CombatUtil.attackCrystal(pos, rotate, eatingPause);
      }

      placeBlock(pos, hand, rotate, packet, placeRender);
   }

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean attackEntity, boolean eatingPause) {
      if (attackEntity) {
         CombatUtil.attackCrystal(pos, rotate, eatingPause);
      }

      placeBlock(pos, hand, rotate, packet);
   }

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
      placeBlock(pos, hand, rotate, packet, true);
   }

   public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean placeRender) {
      EnumFacing side = getFirstFacing(pos);
      if (side != null) {
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
         boolean sneaking = false;
         if (!SneakManager.isSneaking && (canUseList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            sneaking = true;
         }

         if (rotate) {
            EntityUtil.faceVector(hitVec);
         }

         if (placeRender) {
            PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
         }

         rightClickBlock(neighbour, hitVec, hand, opposite, packet);
         if (sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
         }
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
      if (packet) {
         float f = (float)(vec.x - (double)pos.getX());
         float f2 = (float)(vec.y - (double)pos.getY());
         float f3 = (float)(vec.z - (double)pos.getZ());
         mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f2, f3));
      } else {
         mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
      }

      mc.player.swingArm(hand);
      mc.rightClickDelayTimer = 4;
   }

   public static EnumFacing getFirstFacing(BlockPos pos) {
      if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
         Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
         if (iterator.hasNext()) {
            return (EnumFacing)iterator.next();
         }
      } else {
         for(EnumFacing side : getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (canClick(pos.offset(side))) {
               return side;
            }
         }
      }

      return null;
   }

   public static BlockPos[] getHorizontalOffsets(BlockPos pos) {
      return new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west(), pos.down()};
   }

   public static int getPlaceAbility(BlockPos pos, boolean raytrace) {
      return getPlaceAbility(pos, raytrace, true);
   }

   public static int getPlaceAbility(BlockPos pos, boolean raytrace, boolean checkForEntities) {
      Block block = getBlock(pos);
      if (!(block instanceof BlockAir)
         && !(block instanceof BlockLiquid)
         && !(block instanceof BlockTallGrass)
         && !(block instanceof BlockFire)
         && !(block instanceof BlockDeadBush)
         && !(block instanceof BlockSnow)) {
         return 0;
      } else if (raytrace && !raytraceCheck(pos, 0.0F)) {
         return -1;
      } else if (checkForEntities && checkForEntities(pos)) {
         return 1;
      } else {
         for(EnumFacing side : getPossibleSides(pos)) {
            if (canBeClicked(pos.offset(side))) {
               return 3;
            }
         }

         return 2;
      }
   }

   public static List<EnumFacing> getPossibleSides(BlockPos pos) {
      ArrayList<EnumFacing> facings = new ArrayList();

      for(EnumFacing side : EnumFacing.values()) {
         BlockPos neighbor = pos.offset(side);
         if (getBlock(neighbor).canCollideCheck(getState(neighbor), false) && !canReplace(neighbor)) {
            facings.add(side);
         }
      }

      return facings;
   }

   public static boolean canReplace(BlockPos pos) {
      return getState(pos).getMaterial().isReplaceable();
   }

   public static boolean canPlaceCrystal(BlockPos pos) {
      BlockPos obsPos = pos.down();
      BlockPos boost = obsPos.up();
      BlockPos boost2 = obsPos.up(2);
      return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
         && (getBlock(boost) == Blocks.AIR || getBlock(boost) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue())
         && getBlock(boost2) == Blocks.AIR
         && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
         && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
   }

   public static boolean canPlaceCrystal(BlockPos pos, double distance) {
      if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
         return false;
      } else {
         BlockPos obsPos = pos.down();
         BlockPos boost = obsPos.up();
         BlockPos boost2 = obsPos.up(2);
         return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN)
            && (getBlock(boost) == Blocks.AIR || getBlock(boost) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue())
            && getBlock(boost2) == Blocks.AIR
            && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
            && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
      }
   }

   public static boolean canBeClicked(BlockPos pos) {
      return getBlock(pos).canCollideCheck(getState(pos), false);
   }

   public static boolean checkForEntities(BlockPos blockPos) {
      for(Entity entity : mc.world.loadedEntityList) {
         if (!(entity instanceof EntityItem)
            && !(entity instanceof EntityEnderCrystal)
            && !(entity instanceof EntityXPOrb)
            && !(entity instanceof EntityExpBottle)
            && !(entity instanceof EntityArrow)
            && new AxisAlignedBB(blockPos).intersects(entity.getEntityBoundingBox())) {
            return true;
         }
      }

      return false;
   }

   public static boolean raytraceCheck(BlockPos pos, float height) {
      return mc.world
            .rayTraceBlocks(
               new Vec3d(
                  mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ
               ),
               new Vec3d((double)pos.getX(), (double)((float)pos.getY() + height), (double)pos.getZ()),
               false,
               true,
               false
            )
         == null;
   }

   public static EnumFacing getBestNeighboring(BlockPos pos, EnumFacing facing) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if ((facing == null || !pos.offset(i).equals(pos.offset(facing, -1))) && i != EnumFacing.DOWN) {
            for(EnumFacing side : getPlacableFacings(pos.offset(i), true, true)) {
               if (canClick(pos.offset(i).offset(side))) {
                  return i;
               }
            }
         }
      }

      EnumFacing bestFacing = null;
      double distance = 0.0;

      for(EnumFacing i : EnumFacing.VALUES) {
         if ((facing == null || !pos.offset(i).equals(pos.offset(facing, -1))) && i != EnumFacing.DOWN) {
            for(EnumFacing side : getPlacableFacings(pos.offset(i), true, false)) {
               if (canClick(pos.offset(i).offset(side))
                  && (bestFacing == null || mc.player.getDistanceSq(pos.offset(i)) < distance)) {
                  bestFacing = i;
                  distance = mc.player.getDistanceSq(pos.offset(i));
               }
            }
         }
      }

      return null;
   }

   public static boolean isHole(BlockPos posIn) {
      for(BlockPos pos : getHorizontalOffsets(posIn)) {
         if (getBlock(pos) == Blocks.AIR
            || getBlock(pos) != Blocks.BEDROCK && getBlock(pos) != Blocks.OBSIDIAN && getBlock(pos) != Blocks.ENDER_CHEST) {
            return false;
         }
      }

      return true;
   }

   public static boolean isSafe(Block block) {
      List<Block> safeBlocks = Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.ENDER_CHEST, Blocks.ANVIL);
      return !safeBlocks.contains(block);
   }

   public static boolean isSlab(Block block) {
      return block instanceof BlockSlab || block instanceof BlockCarpet || block instanceof BlockCake;
   }

   public static boolean isStair(Block block) {
      return block instanceof BlockStairs;
   }

   public static boolean isFence(Block block) {
      return block instanceof BlockFence || block instanceof BlockFenceGate;
   }
}
