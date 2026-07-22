//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.autotrap.AutoTrap;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PistonCrystal extends Module {
   public static PistonCrystal INSTANCE;
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", false));
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", false));
   private final Setting<Boolean> eatingBreak = this.add(new Setting<>("EatingBreak", false));
   private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 5.0F, 1.0F, 8.0F));
   private final Setting<Float> range = this.add(new Setting<>("Range", 4.0F, 1.0F, 8.0F));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
   private final Setting<Boolean> mine = this.add(new Setting<>("Mine", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("PauseAir", true));
   private final Setting<Boolean> onlyStatic = this.add(new Setting<>("PauseMoving", true));
   private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 100, 0, 500));
   private EntityPlayer target = null;
   private final Timer timer = new Timer();
   public static BlockPos lastMine = null;
   public static boolean shouldMine = false;

   public PistonCrystal() {
      super("PistonCrystal", "in strict", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      if (this.autoDisable.getValue() && AutoTrap.INSTANCE.isOff()) {
         this.disable();
      } else {
         this.target = this.getTarget((double)this.range.getValue().floatValue());
         if (this.target != null) {
            if (!this.eatingPause.getValue() || !EntityUtil.isEating()) {
               if (this.timer.passedMs((long)this.updateDelay.getValue().intValue())) {
                  if (!PullCrystal.check(this.onlyStatic.getValue(), !mc.player.onGround, this.onlyGround.getValue())) {
                     this.timer.reset();
                     BlockPos pos = EntityUtil.getEntityPos(this.target);
                     if (!EntityUtil.isEating() || this.eatingBreak.getValue()) {
                        if (this.checkCrystal(pos.up(0))) {
                           CombatUtil.attackCrystal(pos.up(0), true, true);
                        }

                        if (this.checkCrystal(pos.up(1))) {
                           CombatUtil.attackCrystal(pos.up(1), true, true);
                        }

                        if (this.checkCrystal(pos.up(2))) {
                           CombatUtil.attackCrystal(pos.up(2), true, true);
                        }
                     }

                     shouldMine = false;
                     if (!this.doPistonActive(pos.up(2))) {
                        if (!this.doPistonActive(pos.up())) {
                           if (!this.doPlaceCrystal(pos.up(2))) {
                              if (!this.doPlaceCrystal(pos.up())) {
                                 if (!this.doPlacePiston(pos.up(2))) {
                                    if (!this.doPlacePiston(pos.up())) {
                                       if (!this.mine.getValue()) {
                                          shouldMine = true;
                                       } else if (!this.doMineRedstone(pos.up(2))) {
                                          if (!this.doMineRedstone(pos.up())) {
                                             shouldMine = true;
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private EntityPlayer getTarget(double range) {
      EntityPlayer target = null;
      double distance = range;

      for(EntityPlayer player : mc.world.playerEntities) {
         if (!EntityUtil.invalid(player, range) && this.getBlock(EntityUtil.getEntityPos(player)) != Blocks.OBSIDIAN) {
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

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   private boolean doPlacePiston(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP && this.placePiston(pos, i)) {
            return true;
         }
      }

      return false;
   }

   private boolean doMineRedstone(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP && this.doMineRedstone(pos, i)) {
            return true;
         }
      }

      return false;
   }

   private boolean doPlaceCrystal(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP && this.placeCrystal(pos, i)) {
            return true;
         }
      }

      return false;
   }

   private boolean doMineRedstone(BlockPos pos, EnumFacing i) {
      if (this.tryMineRedstone(pos.offset(i, 3), i)) {
         return true;
      } else if (this.tryMineRedstone(pos.offset(i, 3).up(), i)) {
         return true;
      } else if (this.tryMineRedstone(pos.offset(i, 2), i)) {
         return true;
      } else if (this.tryMineRedstone(pos.offset(i, 2).up(), i)) {
         return true;
      } else {
         double offsetX = (double)(pos.offset(i).getX() - pos.getX());
         double offsetZ = (double)(pos.offset(i).getZ() - pos.getZ());
         if (this.tryMineRedstone(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.tryMineRedstone(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else if (this.tryMineRedstone(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
            return true;
         } else if (this.tryMineRedstone(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
            return true;
         } else if (this.tryMineRedstone(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.tryMineRedstone(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else {
            return this.tryMineRedstone(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)
               ? true
               : this.tryMineRedstone(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i);
         }
      }
   }

   private boolean tryMineRedstone(BlockPos pos, EnumFacing facing) {
      if (!BlockUtil.canPlace(pos, (double)this.placeRange.getValue().floatValue()) && !(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
         return false;
      } else if ((mc.player.posY - (double)pos.getY() <= -2.0 || mc.player.posY - (double)pos.getY() >= 3.0)
         && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6) {
         return false;
      } else if (!mc.world.isAirBlock(pos.offset(facing, -1))
         && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE
         && this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION) {
         return false;
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (mc.world.getBlockState(pos.offset(i)).getBlock() == Blocks.REDSTONE_BLOCK) {
               shouldMine = true;
               lastMine = pos.offset(i);
               CombatUtil.mineBlock(pos.offset(i));
               return true;
            }
         }

         return false;
      }
   }

   private boolean placePiston(BlockPos pos, EnumFacing i) {
      if (!BlockUtil.canPlaceCrystal(pos.offset(i), (double)this.placeRange.getValue().floatValue())) {
         return false;
      } else if (this.tryPlacePiston(pos.offset(i, 3), i)) {
         return true;
      } else if (this.tryPlacePiston(pos.offset(i, 3).up(), i)) {
         return true;
      } else if (this.tryPlacePiston(pos.offset(i, 2), i)) {
         return true;
      } else if (this.tryPlacePiston(pos.offset(i, 2).up(), i)) {
         return true;
      } else {
         double offsetX = (double)(pos.offset(i).getX() - pos.getX());
         double offsetZ = (double)(pos.offset(i).getZ() - pos.getZ());
         if (this.tryPlacePiston(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.tryPlacePiston(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else if (this.tryPlacePiston(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
            return true;
         } else if (this.tryPlacePiston(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
            return true;
         } else if (this.tryPlacePiston(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.tryPlacePiston(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else {
            return this.tryPlacePiston(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)
               ? true
               : this.tryPlacePiston(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i);
         }
      }
   }

   private boolean tryPlacePiston(BlockPos pos, EnumFacing facing) {
      if (!BlockUtil.canPlace(pos, (double)this.placeRange.getValue().floatValue()) && !(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
         return false;
      } else if ((mc.player.posY - (double)pos.getY() <= -2.0 || mc.player.posY - (double)pos.getY() >= 3.0)
         && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6) {
         return false;
      } else if (mc.world.isAirBlock(pos.offset(facing, -1))
         && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE
         && this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION) {
         if (!BlockUtil.canPlace(pos, (double)this.placeRange.getValue().floatValue())) {
            return this.isPiston(pos, facing);
         } else {
            EnumFacing side = BlockUtil.getFirstFacing(pos);
            if (side == null) {
               return false;
            } else {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (mc.world.getBlockState(pos.offset(i)).getBlock() == Blocks.REDSTONE_BLOCK) {
                     return false;
                  }
               }

               int old = mc.player.inventory.currentItem;
               AutoPush.pistonFacing(facing);
               InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
               BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.packet.getValue());
               InventoryUtil.doSwap(old);
               BlockPos neighbour = pos.offset(side);
               EnumFacing opposite = side.getOpposite();
               Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
               EntityUtil.faceVector(hitVec);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private boolean placeCrystal(BlockPos pos, EnumFacing facing) {
      if (!BlockUtil.canPlaceCrystal(pos.offset(facing), (double)this.placeRange.getValue().floatValue())) {
         return false;
      } else if (!this.hasPiston(pos, facing)) {
         return false;
      } else if (InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) == -1) {
         return false;
      } else {
         int old = mc.player.inventory.currentItem;
         InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
         BlockUtil.placeCrystal(pos.offset(facing), true);
         InventoryUtil.doSwap(old);
         return true;
      }
   }

   private boolean hasPiston(BlockPos pos, EnumFacing i) {
      if (this.isPiston(pos.offset(i, 3), i)) {
         return true;
      } else if (this.isPiston(pos.offset(i, 3).up(), i)) {
         return true;
      } else if (this.isPiston(pos.offset(i, 2), i)) {
         return true;
      } else if (this.isPiston(pos.offset(i, 2).up(), i)) {
         return true;
      } else {
         double offsetX = (double)(pos.offset(i).getX() - pos.getX());
         double offsetZ = (double)(pos.offset(i).getZ() - pos.getZ());
         if (this.isPiston(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.isPiston(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else if (this.isPiston(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
            return true;
         } else if (this.isPiston(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
            return true;
         } else if (this.isPiston(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
            return true;
         } else if (this.isPiston(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
         } else {
            return this.isPiston(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)
               ? true
               : this.isPiston(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i);
         }
      }
   }

   private boolean isPiston(BlockPos pos, EnumFacing facing) {
      if (!(mc.world.getBlockState(pos).getBlock() instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)mc.world.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() != facing) {
         return false;
      } else {
         return mc.world.isAirBlock(pos.offset(facing, -1))
            || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE
            || this.getBlock(pos.offset(facing.getOpposite())) == Blocks.PISTON_EXTENSION;
      }
   }

   private boolean doPistonActive(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP && BlockUtil.posHasCrystal(pos.offset(i))) {
            this.doFire(pos, i);
            if (this.doRedStone(pos.offset(i, 3), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 3).up(), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2).up(), i)) {
               return true;
            }

            double offsetX = (double)(pos.offset(i).getX() - pos.getX());
            double offsetZ = (double)(pos.offset(i).getZ() - pos.getZ());
            if (this.doRedStone(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)) {
               return true;
            }

            if (this.doRedStone(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i)) {
               return true;
            }
         }
      }

      return false;
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
                  && !pos.offset(i).equals(pos.offset(facing, -1))
                  && canFire(pos.offset(i))) {
                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                  placeFire(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
                  InventoryUtil.doSwap(old);
                  return;
               }
            }

            if (canFire(pos.offset(facing, -1))) {
               InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
               placeFire(pos.offset(facing, -1), EnumHand.MAIN_HAND, true, this.packet.getValue());
               InventoryUtil.doSwap(old);
            } else {
               if (canFire(pos.offset(facing, 1))) {
                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                  placeFire(pos.offset(facing, 1), EnumHand.MAIN_HAND, true, this.packet.getValue());
                  InventoryUtil.doSwap(old);
               }
            }
         }
      }
   }

   public static void placeFire(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
      EnumFacing side = EnumFacing.DOWN;
      BlockPos neighbour = pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
      if (rotate) {
         EntityUtil.faceVector(hitVec);
      }

      PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
      BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
   }

   private static boolean canFire(BlockPos pos) {
      if (BlockUtil.canReplace(pos.down())) {
         return false;
      } else if (!mc.world.isAirBlock(pos)) {
         return false;
      } else {
         for(EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (BlockUtil.canClick(pos.offset(side)) && side == EnumFacing.DOWN) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean doRedStone(BlockPos pos, EnumFacing facing) {
      if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
         return false;
      } else if (((EnumFacing)mc.world.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() != facing) {
         return false;
      } else if (!mc.world.isAirBlock(pos.offset(facing, -1))
         && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE
         && this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION) {
         return false;
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) {
               return true;
            }
         }

         if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
            return false;
         } else {
            int old = mc.player.inventory.currentItem;
            EnumFacing bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
            if (bestNeighboring != null && BlockUtil.canPlace(pos.offset(bestNeighboring), (double)this.placeRange.getValue().floatValue())) {
               InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
               BlockUtil.placeBlock(pos.offset(bestNeighboring), EnumHand.MAIN_HAND, true, this.packet.getValue());
               InventoryUtil.doSwap(old);
               return true;
            } else {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (BlockUtil.canPlace(pos.offset(i), (double)this.placeRange.getValue().floatValue())) {
                     InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
                     BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
                     InventoryUtil.doSwap(old);
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }
}
