//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import java.util.Optional;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class InteractionManager extends Mod {
   private final Timer attackTimer = new Timer();

   public void placeBlock(BlockPos pos, boolean rotate, boolean packet, boolean attackCrystal, boolean ignoreEntities) {
      if (!fullNullCheck()) {
         if (BlockUtil.canReplace(pos)) {
            if (attackCrystal) {
               this.attackCrystals(pos, rotate);
            }

            Optional<InteractionManager.ClickLocation> posCL = this.getClickLocation(pos, ignoreEntities, false, attackCrystal);
            if (posCL.isPresent()) {
               BlockPos currentPos = posCL.get().neighbour;
               EnumFacing currentFace = posCL.get().opposite;
               boolean shouldSneak = this.shouldShiftClick(currentPos);
               if (shouldSneak) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
               }

               Vec3d hitVec = new Vec3d(currentPos).add(0.5, 0.5, 0.5).add(new Vec3d(currentFace.getDirectionVec()).scale(0.5));
               if (rotate) {
                  Managers.ROTATIONS.lookAtVec3dPacket(hitVec, true);
               }

               if (packet) {
                  Vec3d extendedVec = new Vec3d(currentPos).add(0.5, 0.5, 0.5);
                  float x = (float)(extendedVec.x - (double)currentPos.getX());
                  float y = (float)(extendedVec.y - (double)currentPos.getY());
                  float z = (float)(extendedVec.z - (double)currentPos.getZ());
                  mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPos, currentFace, EnumHand.MAIN_HAND, x, y, z));
               } else {
                  mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace, hitVec, EnumHand.MAIN_HAND);
               }

               PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
               mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
               if (shouldSneak) {
                  mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
               }
            }
         }
      }
   }

   public void placeBlock(BlockPos pos, boolean rotate, boolean packet, boolean attackCrystal) {
      this.placeBlock(pos, rotate, packet, attackCrystal, false);
   }

   public void attackEntity(Entity entity, boolean packet, boolean swing) {
      if (packet) {
         mc.player.connection.sendPacket(new CPacketUseEntity(entity));
      } else {
         mc.playerController.attackEntity(mc.player, entity);
      }

      if (swing) {
         mc.player.swingArm(EnumHand.MAIN_HAND);
      }
   }

   public void attackCrystals(BlockPos pos, boolean rotate) {
      boolean sprint = mc.player.isSprinting();
      int ping = Managers.SERVER.getPing();

      for(EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {
         if (this.attackTimer.passedMs(ping <= 50 ? 75L : 100L)) {
            if (rotate) {
               Managers.ROTATIONS.lookAtVec3dPacket(crystal.getPositionVector(), true);
            }

            if (sprint) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SPRINTING));
            }

            mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            if (sprint) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SPRINTING));
            }

            this.attackTimer.reset();
            break;
         }
      }
   }

   public Optional<InteractionManager.ClickLocation> getClickLocation(BlockPos pos, boolean ignoreEntities, boolean noPistons, boolean onlyCrystals) {
      Block block = mc.world.getBlockState(pos).getBlock();
      if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
         return Optional.empty();
      } else {
         if (!ignoreEntities) {
            for(Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
               if ((!onlyCrystals || !(entity instanceof EntityEnderCrystal))
                  && !(entity instanceof EntityItem)
                  && !(entity instanceof EntityXPOrb)
                  && !(entity instanceof EntityArrow)) {
                  return Optional.empty();
               }
            }
         }

         EnumFacing side = null;

         for(EnumFacing blockSide : EnumFacing.values()) {
            BlockPos sidePos = pos.offset(blockSide);
            if ((!noPistons || mc.world.getBlockState(sidePos).getBlock() != Blocks.PISTON)
               && mc.world.getBlockState(sidePos).getBlock().canCollideCheck(mc.world.getBlockState(sidePos), false)) {
               IBlockState blockState = mc.world.getBlockState(sidePos);
               if (!blockState.getMaterial().isReplaceable()) {
                  side = blockSide;
                  break;
               }
            }
         }

         if (side == null) {
            return Optional.empty();
         } else {
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            return !mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)
               ? Optional.empty()
               : Optional.of(new InteractionManager.ClickLocation(neighbour, opposite));
         }
      }
   }

   public boolean shouldShiftClick(BlockPos pos) {
      Block block = mc.world.getBlockState(pos).getBlock();
      TileEntity tileEntity = null;

      for(TileEntity entity : mc.world.loadedTileEntityList) {
         if (entity.getPos().equals(pos)) {
            tileEntity = entity;
            break;
         }
      }

      return tileEntity != null
         || block instanceof BlockBed
         || block instanceof BlockContainer
         || block instanceof BlockDoor
         || block instanceof BlockTrapDoor
         || block instanceof BlockFenceGate
         || block instanceof BlockButton
         || block instanceof BlockAnvil
         || block instanceof BlockWorkbench
         || block instanceof BlockCake
         || block instanceof BlockRedstoneDiode;
   }

   public static class ClickLocation {
      public final BlockPos neighbour;
      public final EnumFacing opposite;

      public ClickLocation(BlockPos neighbour, EnumFacing opposite) {
         this.neighbour = neighbour;
         this.opposite = opposite;
      }
   }
}
