//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PullCrystal extends Module {
   private final Setting<Float> range = this.add(new Setting<>("Range", 5.0F, 1.0F, 8.0F));
   private final Setting<Boolean> pistonPacket = this.add(new Setting<>("PistonPacket", false));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
   public final Setting<Boolean> pauseWeb = this.add(new Setting<>("PauseWeb", true));
   private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
   private final Setting<Boolean> multiPlace = this.add(new Setting<>("MultiPlace", false));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("NoAir", true));
   private final Setting<Boolean> onlyStatic = this.add(new Setting<>("NoMoving", true));
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 100, 0, 500));
   private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 30.0));
   private EntityPlayer target = null;
   public static PullCrystal INSTANCE;
   private final Timer timer = new Timer();
   public static BlockPos crystalPos;
   public static BlockPos powerPos;

   public PullCrystal() {
      super("PullCrystal", "use piston pull crystal and boom", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long)this.updateDelay.getValue().intValue())) {
         if (this.noEating.getValue() && EntityUtil.isEating()) {
            this.target = null;
         } else if (check(this.onlyStatic.getValue(), !mc.player.onGround, this.onlyGround.getValue())) {
            this.target = null;
         } else {
            this.target = this.getTarget((double)this.range.getValue().floatValue(), this.maxTargetSpeed.getMaxValue());
            if (this.target == null) {
               this.target = CombatUtil.getTarget((double)this.range.getValue().floatValue(), this.maxTargetSpeed.getMaxValue());
               if (this.target != null) {
                  this.mineBlock(EntityUtil.getEntityPos(this.target));
               } else if (this.autoDisable.getValue()) {
                  this.disable();
               }
            } else {
               this.timer.reset();
               BlockPos pos = EntityUtil.getEntityPos(this.target);
               if (this.checkCrystal(pos.up(0))) {
                  CombatUtil.attackCrystal(pos.up(0), true, true);
               }

               if (this.checkCrystal(pos.up(1))) {
                  CombatUtil.attackCrystal(pos.up(1), true, true);
               }

               if (this.checkCrystal(pos.up(2))) {
                  CombatUtil.attackCrystal(pos.up(2), true, true);
               }

               if (this.checkCrystal(pos.up(3))) {
                  CombatUtil.attackCrystal(pos.up(3), true, true);
               }

               if (!this.doPullCrystal(pos)) {
                  if (!this.doPullCrystal(new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
                     if (!this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
                        if (!this.doPullCrystal(
                           new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ - 0.1)
                        )) {
                           this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ - 0.1));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean doPullCrystal(BlockPos pos) {
      if (this.pull(pos.up(2))) {
         return true;
      } else if (this.pull(pos.up())) {
         return true;
      } else if (this.crystal(pos.up())) {
         return true;
      } else if (this.power(pos.up(2))) {
         return true;
      } else if (this.power(pos.up())) {
         return true;
      } else {
         return this.piston(pos.up(2)) ? true : this.piston(pos.up());
      }
   }

   public static boolean check(boolean onlyStatic, boolean onGround, boolean onlyGround) {
      if (MovementUtil.isMoving() && onlyStatic) {
         return true;
      } else if (onGround && onlyGround) {
         return true;
      } else if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
         return true;
      } else if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
         return true;
      } else {
         return InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) == -1;
      }
   }

   private EntityPlayer getTarget(double range, double maxSpeed) {
      EntityPlayer target = null;
      double distance = range;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!EntityUtil.invalid(player, range)
            && this.getBlock(EntityUtil.getEntityPos(player)) != Blocks.OBSIDIAN
            && this.getBlock(EntityUtil.getEntityPos(player)) != Blocks.BEDROCK
            && !(Managers.SPEED.getPlayerSpeed(player) > maxSpeed)
            && !EntityUtil.invalid(player, range)) {
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

   private static boolean canFire(BlockPos pos) {
      return BlockUtil.canReplace(pos.down()) ? false : mc.world.isAirBlock(pos);
   }

   private void doFire(BlockPos pos, EnumFacing facing) {
      if (this.fire.getValue()) {
         if (InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL) != -1) {
            int old = mc.player.inventory.currentItem;

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN
                  && i != EnumFacing.UP
                  && !pos.offset(i).equals(pos.offset(facing))
                  && mc.world.getBlockState(pos.offset(i)).getBlock() == Blocks.FIRE) {
                  return;
               }
            }

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN
                  && i != EnumFacing.UP
                  && !pos.offset(i).equals(pos.offset(facing))
                  && (!pos.offset(i).equals(pos.offset(facing, -1)) || BlockUtil.posHasCrystal(pos.offset(facing, -1)))
                  && canFire(pos.offset(i))) {
                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                  BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
                  InventoryUtil.doSwap(old);
                  return;
               }
            }

            if (canFire(pos.offset(facing, 1))) {
               InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
               BlockUtil.placeBlock(pos.offset(facing, 1), EnumHand.MAIN_HAND, true, this.packet.getValue());
               InventoryUtil.doSwap(old);
            }
         }
      }
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   public boolean crystal(BlockPos pos) {
      for(Entity crystal : mc.world.loadedEntityList) {
         if (crystal instanceof EntityEnderCrystal
            && !(crystal.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5) > 4.0)) {
            CombatUtil.attackCrystal(crystal, true, false);
            return true;
         }
      }

      return false;
   }

   private boolean pistonActive(BlockPos pos, EnumFacing facing, BlockPos oPos) {
      return this.pistonActive(pos, facing, oPos, false) ? true : this.pistonActive(pos, facing, oPos, true);
   }

   private IBlockState getBlockState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   private boolean pistonActive(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
      if (up) {
         pos = pos.up();
      }

      if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
         return false;
      } else if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() != facing) {
         return false;
      } else if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD) {
         return false;
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) {
               if (!BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
                  int old = mc.player.inventory.currentItem;
                  crystalPos = oPos.offset(facing, -1);
                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
                  BlockUtil.placeCrystal(oPos.offset(facing, -1), true);
                  InventoryUtil.doSwap(old);
               }

               this.doFire(oPos, facing);
               powerPos = pos.offset(i);
               this.mineBlock(pos.offset(i));
               return true;
            }
         }

         return false;
      }
   }

   private boolean power(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.placePower(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.placePower(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }

            if (this.placePower(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.placePower(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean piston(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.placePiston(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.placePiston(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }

            if (this.placePiston(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.placePiston(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkCrystal(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityEnderCrystal) {
            float damage = DamageUtil.calculateDamage(entity, this.target);
            if (damage > 6.0F) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean placePower(BlockPos pos, EnumFacing facing, BlockPos oPos) {
      return this.placePower(pos, facing, oPos, false) ? true : this.placePower(pos, facing, oPos, true);
   }

   private boolean placePower(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
      if (up) {
         pos = pos.up();
      }

      if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
         return false;
      } else if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() != facing) {
         return false;
      } else if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_HEAD
         || this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (!mc.world.isAirBlock(pos.offset(facing, -1))
         && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD
         && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_EXTENSION
         && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE) {
         return false;
      } else {
         int old = mc.player.inventory.currentItem;
         return this.placeRedStone(pos, facing, old, oPos);
      }
   }

   private boolean placePiston(BlockPos pos, EnumFacing facing, BlockPos oPos) {
      return this.placePiston(pos, facing, oPos, false) ? true : this.placePiston(pos, facing, oPos, true);
   }

   private boolean placePiston(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
      if (up) {
         pos = pos.up();
      }

      if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
         return false;
      } else if (!BlockUtil.canPlace(pos) && !(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (this.getBlock(pos) instanceof BlockPistonBase
         && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() != facing) {
         return false;
      } else if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_HEAD
         || this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
         return true;
      } else if (!mc.world.isAirBlock(pos.offset(facing, -1))
         && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD
         && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_EXTENSION) {
         return false;
      } else if ((
            mc.player.posY - (double)pos.down().getY() <= -1.0
               || mc.player.posY - (double)pos.down().getY() >= 2.0
         )
         && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6) {
         return false;
      } else {
         int old = mc.player.inventory.currentItem;
         if (BlockUtil.canPlace(pos)) {
            EntityUtil.facePlacePos(pos);
            AutoPush.pistonFacing(facing);
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(BlockPistonBase.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
            InventoryUtil.doSwap(old);
            EntityUtil.facePlacePos(pos);
            return this.multiPlace.getValue() && this.placeRedStone(pos, facing, old, oPos) ? true : true;
         } else {
            return this.placeRedStone(pos, facing, old, oPos);
         }
      }
   }

   private boolean placeRedStone(BlockPos pos, EnumFacing facing, int old, BlockPos oPos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (this.getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) {
            powerPos = pos.offset(i);
            return true;
         }
      }

      EnumFacing bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
      if (bestNeighboring != null
         && !pos.offset(bestNeighboring).equals(oPos.offset(facing, -1))
         && !pos.offset(bestNeighboring).equals(oPos.offset(facing, -1).up())) {
         powerPos = pos.offset(bestNeighboring);
         if (BlockUtil.canPlace(powerPos)) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(bestNeighboring), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
         }
      }

      for(EnumFacing i : EnumFacing.VALUES) {
         if (!pos.offset(i).equals(pos.offset(facing, -1))
            && !pos.offset(i).equals(oPos.offset(facing, -1))
            && !pos.offset(i).equals(oPos.offset(facing, -1).up())
            && BlockUtil.canPlace(pos.offset(i))) {
            powerPos = pos.offset(i);
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
         }
      }

      return false;
   }

   private boolean pull(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.pistonActive(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.pistonActive(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }

            if (this.pistonActive(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
               return true;
            }

            if (this.pistonActive(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) {
               return true;
            }
         }
      }

      return false;
   }

   private void mineBlock(BlockPos pos) {
      CombatUtil.mineBlock(pos);
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }
}
