//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CombatUtil implements Wrapper {
   public static final Timer breakTimer = new Timer();

   public static void mineBlock(BlockPos pos) {
      if (!PacketMine.godBlocks.contains(getBlock(pos)) || !PacketMine.INSTANCE.godCancel.getValue()) {
         if (!pos.equals(PacketMine.breakPos)) {
            mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
         }
      }
   }

   public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
      if (breakTimer.passedMs((long)CombatSetting.INSTANCE.attackDelay.getValue().intValue())) {
         if (!eatingPause || !EntityUtil.isEating()) {
            for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
               if (entity instanceof EntityEnderCrystal) {
                  breakTimer.reset();
                  mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                  mc.player.swingArm(EnumHand.MAIN_HAND);
                  if (rotate) {
                     EntityUtil.faceXYZ(entity.posX, entity.posY + 0.25, entity.posZ);
                  }
                  break;
               }
            }
         }
      }
   }

   public static void attackCrystal(Entity entity, boolean rotate, boolean eatingPause) {
      if (breakTimer.passedMs((long)CombatSetting.INSTANCE.attackDelay.getValue().intValue())) {
         if (!eatingPause || !EntityUtil.isEating()) {
            if (entity != null) {
               breakTimer.reset();
               mc.player.connection.sendPacket(new CPacketUseEntity(entity));
               mc.player.swingArm(EnumHand.MAIN_HAND);
               if (rotate) {
                  EntityUtil.faceXYZ(entity.posX, entity.posY + 0.25, entity.posZ);
               }
            }
         }
      }
   }

   public static boolean isHole(BlockPos pos, boolean anyBlock, int blocks, boolean onlyCanStand) {
      int blockProgress = 0;
      if (anyBlock) {
         if (!canBlockReplace(pos.add(0, 0, 1))
            || !canBlockReplace(pos.add(0, 0, 2))
               && !canBlockReplace(pos.add(0, 1, 1))
               && !canBlockReplace(pos.add(1, 0, 1))
               && !canBlockReplace(pos.add(-1, 0, 1))) {
            ++blockProgress;
         }

         if (!canBlockReplace(pos.add(0, 0, -1))
            || !canBlockReplace(pos.add(0, 0, -2))
               && !canBlockReplace(pos.add(0, 1, -1))
               && !canBlockReplace(pos.add(1, 0, -1))
               && !canBlockReplace(pos.add(-1, 0, -1))) {
            ++blockProgress;
         }

         if (!canBlockReplace(pos.add(1, 0, 0))
            || !canBlockReplace(pos.add(2, 0, 0))
               && !canBlockReplace(pos.add(1, 1, 0))
               && !canBlockReplace(pos.add(1, 0, 1))
               && !canBlockReplace(pos.add(1, 0, -1))) {
            ++blockProgress;
         }

         if (!canBlockReplace(pos.add(-1, 0, 0))
            || !canBlockReplace(pos.add(-2, 0, 0))
               && !canBlockReplace(pos.add(-1, 1, 0))
               && !canBlockReplace(pos.add(-1, 0, 1))
               && !canBlockReplace(pos.add(-1, 0, -1))) {
            ++blockProgress;
         }
      } else {
         if (getBlock(pos.add(0, 0, 1)) == Blocks.OBSIDIAN
            || getBlock(pos.add(0, 0, 1)) == Blocks.BEDROCK
            || (getBlock(pos.add(0, 0, 2)) == Blocks.OBSIDIAN || getBlock(pos.add(0, 0, 2)) == Blocks.BEDROCK)
               && (getBlock(pos.add(0, 1, 1)) == Blocks.OBSIDIAN || getBlock(pos.add(0, 1, 1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(1, 0, 1)) == Blocks.OBSIDIAN || getBlock(pos.add(1, 0, 1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(-1, 0, 1)) == Blocks.OBSIDIAN || getBlock(pos.add(-1, 0, 1)) == Blocks.BEDROCK)) {
            ++blockProgress;
         }

         if (getBlock(pos.add(0, 0, -1)) == Blocks.OBSIDIAN
            || getBlock(pos.add(0, 0, -1)) == Blocks.BEDROCK
            || (getBlock(pos.add(0, 0, -2)) == Blocks.OBSIDIAN || getBlock(pos.add(0, 0, -2)) == Blocks.BEDROCK)
               && (getBlock(pos.add(0, 1, -1)) == Blocks.OBSIDIAN || getBlock(pos.add(0, 1, -1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(1, 0, -1)) == Blocks.OBSIDIAN || getBlock(pos.add(1, 0, -1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(-1, 0, -1)) == Blocks.OBSIDIAN || getBlock(pos.add(-1, 0, -1)) == Blocks.BEDROCK)) {
            ++blockProgress;
         }

         if (getBlock(pos.add(1, 0, 0)) == Blocks.OBSIDIAN
            || getBlock(pos.add(1, 0, 0)) == Blocks.BEDROCK
            || (getBlock(pos.add(2, 0, 0)) == Blocks.OBSIDIAN || getBlock(pos.add(2, 0, 0)) == Blocks.BEDROCK)
               && (getBlock(pos.add(1, 1, 0)) == Blocks.OBSIDIAN || getBlock(pos.add(1, 1, 0)) == Blocks.BEDROCK)
               && (getBlock(pos.add(1, 0, 1)) == Blocks.OBSIDIAN || getBlock(pos.add(1, 0, 1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(1, 0, -1)) == Blocks.OBSIDIAN || getBlock(pos.add(1, 0, -1)) == Blocks.BEDROCK)) {
            ++blockProgress;
         }

         if (getBlock(pos.add(-1, 0, 0)) == Blocks.OBSIDIAN
            || getBlock(pos.add(-1, 0, 0)) == Blocks.BEDROCK
            || (getBlock(pos.add(-2, 0, 0)) == Blocks.OBSIDIAN || getBlock(pos.add(-2, 0, 0)) == Blocks.BEDROCK)
               && (getBlock(pos.add(-1, 1, 0)) == Blocks.OBSIDIAN || getBlock(pos.add(-1, 1, 0)) == Blocks.BEDROCK)
               && (getBlock(pos.add(-1, 0, 1)) == Blocks.OBSIDIAN || getBlock(pos.add(-1, 0, 1)) == Blocks.BEDROCK)
               && (getBlock(pos.add(-1, 0, -1)) == Blocks.OBSIDIAN || getBlock(pos.add(-1, 0, -1)) == Blocks.BEDROCK)) {
            ++blockProgress;
         }
      }

      return getBlock(pos) == Blocks.AIR
         && getBlock(pos.add(0, 1, 0)) == Blocks.AIR
         && (getBlock(pos.add(0, -1, 0)) != Blocks.AIR || !onlyCanStand)
         && getBlock(pos.add(0, 2, 0)) == Blocks.AIR
         && blockProgress > blocks - 1;
   }

   public static Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   public static boolean canBlockReplace(BlockPos pos) {
      return mc.world.isAirBlock(pos)
         || getBlock(pos) == Blocks.FIRE
         || getBlock(pos) == Blocks.LAVA
         || getBlock(pos) == Blocks.FLOWING_LAVA
         || getBlock(pos) == Blocks.WATER
         || getBlock(pos) == Blocks.FLOWING_WATER;
   }

   public static EntityPlayer getTarget(double range) {
      EntityPlayer target = null;
      double distance = range;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!EntityUtil.invalid(player, range)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (!(mc.player.getDistanceSq(player) >= distance)) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }

   public static EntityPlayer getTarget(double range, double maxSpeed) {
      EntityPlayer target = null;
      double distance = range;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!(Managers.SPEED.getPlayerSpeed(player) > maxSpeed) && !EntityUtil.invalid(player, range)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (!(mc.player.getDistanceSq(player) >= distance)) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }
}
