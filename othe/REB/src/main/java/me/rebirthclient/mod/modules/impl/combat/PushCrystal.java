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

public class PushCrystal extends Module {
   public static PushCrystal INSTANCE;
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", false));
   private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
   private final Setting<Boolean> eatingBreak = this.add(new Setting<>("EatingBreak", false));
   private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 5.0F, 1.0F, 8.0F));
   private final Setting<Float> range = this.add(new Setting<>("Range", 4.0F, 1.0F, 8.0F));
   private final Setting<Boolean> powerPacket = this.add(new Setting<>("PowerPacket", true));
   private final Setting<Boolean> pistonPacket = this.add(new Setting<>("PistonPacket", false));
   private final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
   private final Setting<Boolean> switchPos = this.add(new Setting<>("Switch", false));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("SelfGround", true));
   private final Setting<Boolean> onlyStatic = this.add(new Setting<>("MovingPause", true));
   private final Setting<Integer> updateDelay = this.add(new Setting<>("PlaceDelay", 100, 0, 500));
   private final Setting<Integer> posUpdateDelay = this.add(new Setting<>("PosUpdateDelay", 500, 0, 1000));
   private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
   private EntityPlayer target = null;
   private final Timer timer = new Timer();
   private final Timer crystalTimer = new Timer();
   public BlockPos bestPos = null;
   public BlockPos bestOPos = null;
   public EnumFacing bestFacing = null;
   public double distance = 100.0;
   public boolean getPos = false;
   private boolean isPiston = false;

   public PushCrystal() {
      super("PushCrystal", "in strict", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      if (this.autoDisable.getValue() && AutoTrap.INSTANCE.isOff()) {
         this.disable();
      } else {
         this.target = this.getTarget((double)this.range.getValue().floatValue());
         if (this.target != null) {
            if (!this.noEating.getValue() || !EntityUtil.isEating()) {
               if (!PullCrystal.check(this.onlyStatic.getValue(), !mc.player.onGround, this.onlyGround.getValue())) {
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

                  if (this.bestPos != null && mc.world.getBlockState(this.bestPos).getBlock() instanceof BlockPistonBase) {
                     this.isPiston = true;
                  } else if (this.isPiston) {
                     this.isPiston = false;
                     this.crystalTimer.reset();
                     this.bestPos = null;
                  }

                  if (this.crystalTimer.passedMs((long)this.posUpdateDelay.getValue().intValue())) {
                     this.distance = 100.0;
                     this.getPos = false;
                     this.getBestPos(pos.up(2));
                     this.getBestPos(pos.up());
                  }

                  if (this.timer.passedMs((long)this.updateDelay.getValue().intValue())) {
                     if (this.getPos && this.bestPos != null) {
                        this.timer.reset();
                        if (this.debug.getValue()) {
                           this.sendMessage(
                              "[Debug] PistonPos:"
                                 + this.bestPos
                                 + " Facing:"
                                 + this.bestFacing
                                 + " CrystalPos:"
                                 + this.bestOPos.offset(this.bestFacing)
                           );
                        }

                        this.doPistonAura(this.bestPos, this.bestFacing, this.bestOPos);
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
            if (damage > 7.0F) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean checkCrystal2(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityEnderCrystal && EntityUtil.getEntityPos(entity).equals(pos)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   private void getBestPos(BlockPos pos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
            this.getPos(pos, i);
         }
      }
   }

   private void getPos(BlockPos pos, EnumFacing i) {
      if (BlockUtil.canPlaceCrystal(pos.offset(i), (double)this.placeRange.getValue().floatValue()) || this.checkCrystal2(pos.offset(i))) {
         this.getPos(pos.offset(i, 3), i, pos);
         this.getPos(pos.offset(i, 3).up(), i, pos);
         this.getPos(pos.offset(i, 2), i, pos);
         this.getPos(pos.offset(i, 2).up(), i, pos);
         double offsetX = (double)(pos.offset(i).getX() - pos.getX());
         double offsetZ = (double)(pos.offset(i).getZ() - pos.getZ());
         this.getPos(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i, pos);
         this.getPos(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i, pos);
         this.getPos(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i, pos);
      }
   }

   private void getPos(BlockPos pos, EnumFacing facing, BlockPos oPos) {
      if (!this.switchPos.getValue() || this.bestPos == null || !this.bestPos.equals(pos) || !mc.world.isAirBlock(this.bestPos)) {
         if (BlockUtil.canPlace(pos, (double)this.placeRange.getValue().floatValue()) || this.getBlock(pos) instanceof BlockPistonBase) {
            if (InventoryUtil.findHotbarClass(BlockPistonBase.class) != -1) {
               if (this.getBlock(pos) instanceof BlockPistonBase
                  || !(mc.player.posY - (double)pos.getY() <= -2.0)
                     && !(mc.player.posY - (double)pos.getY() >= 3.0)
                  || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)) {
                  if ((
                        mc.world.isAirBlock(pos.offset(facing, -1))
                           || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE && CombatSetting.INSTANCE.placeInFire.getValue()
                     )
                     && (
                        this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION
                           || this.checkCrystal2(pos.offset(facing.getOpposite()))
                     )) {
                     if (BlockUtil.canPlace(pos, (double)this.placeRange.getValue().floatValue()) || this.isPiston(pos, facing)) {
                        if (mc.player
                                 .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                              < this.distance
                           || this.bestPos == null) {
                           this.bestPos = pos;
                           this.bestOPos = oPos;
                           this.bestFacing = facing;
                           this.distance = mc.player
                              .getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                           this.getPos = true;
                           this.crystalTimer.reset();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void doPistonAura(BlockPos pos, EnumFacing facing, BlockPos oPos) {
      if (mc.world.isAirBlock(pos)) {
         EnumFacing side = BlockUtil.getFirstFacing(pos);
         if (side == null) {
            return;
         }

         int old = mc.player.inventory.currentItem;
         AutoPush.pistonFacing(facing);
         InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
         BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
         InventoryUtil.doSwap(old);
         BlockPos neighbour = pos.offset(side);
         EnumFacing opposite = side.getOpposite();
         Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
         EntityUtil.faceVector(hitVec);
      }

      this.doRedStone(pos, facing, oPos.offset(facing));
      this.placeCrystal(oPos, facing);
      this.doFire(oPos, facing);
   }

   private void placeCrystal(BlockPos pos, EnumFacing facing) {
      if (BlockUtil.canPlaceCrystal(pos.offset(facing), (double)this.placeRange.getValue().floatValue())) {
         if (InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) != -1) {
            int old = mc.player.inventory.currentItem;
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
            BlockUtil.placeCrystal(pos.offset(facing), true);
            InventoryUtil.doSwap(old);
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
                  placeFire(pos.offset(i), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
                  InventoryUtil.doSwap(old);
                  return;
               }
            }

            if (canFire(pos.offset(facing, -1)) && mc.world.isAirBlock(pos.offset(facing, -1).up())) {
               InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
               placeFire(pos.offset(facing, -1), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
               InventoryUtil.doSwap(old);
            } else {
               if (canFire(pos.offset(facing, 1))) {
                  InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
                  placeFire(pos.offset(facing, 1), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
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

   private void doRedStone(BlockPos pos, EnumFacing facing, BlockPos crystalPos) {
      if (mc.world.isAirBlock(pos.offset(facing, -1))
         || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE
         || this.getBlock(pos.offset(facing.getOpposite())) == Blocks.PISTON_EXTENSION) {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) == Blocks.REDSTONE_BLOCK) {
               return;
            }
         }

         if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) != -1) {
            int old = mc.player.inventory.currentItem;
            EnumFacing bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
            if (bestNeighboring != null
               && BlockUtil.canPlace(pos.offset(bestNeighboring), (double)this.placeRange.getValue().floatValue())
               && !pos.offset(bestNeighboring).equals(crystalPos)) {
               InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
               BlockUtil.placeBlock(pos.offset(bestNeighboring), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
               InventoryUtil.doSwap(old);
            } else {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (BlockUtil.canPlace(pos.offset(i), (double)this.placeRange.getValue().floatValue()) && !pos.offset(i).equals(crystalPos)) {
                     InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
                     BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
                     InventoryUtil.doSwap(old);
                     return;
                  }
               }
            }
         }
      }
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }
}
