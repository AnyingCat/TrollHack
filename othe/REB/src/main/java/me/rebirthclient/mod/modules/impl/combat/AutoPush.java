//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
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
import net.minecraft.util.math.Vec3d;

public class AutoPush extends Module {
   public static final List<Block> canPushBlock = Arrays.asList(
      Blocks.AIR, Blocks.ENDER_CHEST, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE
   );
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> pistonPacket = this.add(new Setting<>("PistonPacket", false));
   private final Setting<Boolean> redStonePacket = this.add(new Setting<>("RedStonePacket", true));
   private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
   private final Setting<Boolean> onlyCrystal = this.add(new Setting<>("OnlyCrystal", false));
   private final Setting<Boolean> crystalCheck = this.add(new Setting<>("CrystalCheck", true));
   private final Setting<Boolean> attackCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.attackCrystal.isOpen()));
   private final Setting<Boolean> mine = this.add(new Setting<>("Mine", true));
   private final Setting<Boolean> allowWeb = this.add(new Setting<>("AllowWeb", true));
   private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 100, 0, 500));
   private final Setting<Boolean> selfGround = this.add(new Setting<>("SelfGround", true));
   private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
   private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 15.0));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", false));
   private final Setting<Boolean> checkPiston = this.add(new Setting<>("CheckPiston", false));
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   private final Setting<Boolean> pullBack = this.add(new Setting<>("PullBack", true).setParent());
   private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", true, v -> this.pullBack.isOpen()));
   private final Setting<Double> range = this.add(new Setting<>("Range", 5.0, 0.0, 6.0));
   private final Setting<Double> placeRange = this.add(new Setting<>("PlaceRange", 5.0, 0.0, 6.0));
   private final Setting<Integer> surroundCheck = this.add(new Setting<>("SurroundCheck", 2, 0, 4));
   private final Timer timer = new Timer();
   private EntityPlayer DisplayTarget = null;

   public AutoPush() {
      super("AutoPush", "use piston push hole fag", Category.COMBAT);
   }

   public static void pistonFacing(EnumFacing i) {
      if (i == EnumFacing.EAST) {
         EntityUtil.faceYawAndPitch(-90.0F, 5.0F);
      } else if (i == EnumFacing.WEST) {
         EntityUtil.faceYawAndPitch(90.0F, 5.0F);
      } else if (i == EnumFacing.NORTH) {
         EntityUtil.faceYawAndPitch(180.0F, 5.0F);
      } else if (i == EnumFacing.SOUTH) {
         EntityUtil.faceYawAndPitch(0.0F, 5.0F);
      }
   }

   static boolean checkTarget(BlockPos pos, Entity target) {
      Vec3d[] vec3dList = EntityUtil.getVarOffsets(0, 0, 0);

      for(Vec3d vec3d : vec3dList) {
         BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);

         for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
            if (entity == target) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isWeb(EntityPlayer entityPlayer) {
      if (isWeb(new BlockPos(entityPlayer.posX + 0.2, entityPlayer.posY + 0.5, entityPlayer.posZ + 0.2))) {
         return true;
      } else if (isWeb(new BlockPos(entityPlayer.posX - 0.2, entityPlayer.posY + 0.5, entityPlayer.posZ + 0.2))) {
         return true;
      } else {
         return isWeb(new BlockPos(entityPlayer.posX - 0.2, entityPlayer.posY + 0.5, entityPlayer.posZ - 0.2))
            ? true
            : isWeb(new BlockPos(entityPlayer.posX + 0.2, entityPlayer.posY + 0.5, entityPlayer.posZ - 0.2));
      }
   }

   private static boolean isWeb(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() == Blocks.WEB && checkEntity(pos);
   }

   private static boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer && entity != mc.player) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void onUpdate() {
      if (this.timer.passedMs((long)this.updateDelay.getValue().intValue())) {
         if (this.selfGround.getValue() && !mc.player.onGround) {
            if (this.autoDisable.getValue()) {
               this.disable();
            }
         } else if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) != -1 && InventoryUtil.findHotbarClass(BlockPistonBase.class) != -1) {
            if (Managers.SPEED.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
               if (this.autoDisable.getValue()) {
                  this.disable();
               }
            } else if (!this.noEating.getValue() || !EntityUtil.isEating()) {
               if (!this.onlyCrystal.getValue()
                  || mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL)
                  || mc.player.getHeldItemMainhand().getItem().equals(Items.END_CRYSTAL)) {
                  this.timer.reset();

                  for(EntityPlayer target : mc.world.playerEntities) {
                     if (!EntityUtil.invalid(target, this.range.getValue())
                        && this.canPush(target)
                        && (target.onGround || !this.onlyGround.getValue())
                        && !(Managers.SPEED.getPlayerSpeed(target) > this.maxTargetSpeed.getValue())
                        && (!isWeb(target) || this.allowWeb.getValue())) {
                        this.DisplayTarget = target;
                        if (this.doPush(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ + 0.1), target)) {
                           return;
                        }

                        if (this.doPush(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ + 0.1), target)) {
                           return;
                        }

                        if (this.doPush(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ - 0.1), target)) {
                           return;
                        }

                        if (this.doPush(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ - 0.1), target)) {
                           return;
                        }
                     }
                  }

                  if (this.autoDisable.getValue()) {
                     this.disable();
                  }

                  this.DisplayTarget = null;
               }
            }
         } else {
            if (this.autoDisable.getValue()) {
               this.disable();
            }
         }
      }
   }

   private boolean checkPiston(BlockPos targetPos) {
      for(EnumFacing i : EnumFacing.VALUES) {
         if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
            BlockPos pos = targetPos.up();
            if (this.getBlock(pos.offset(i)) instanceof BlockPistonBase
               && ((EnumFacing)this.getBlockState(pos.offset(i)).getValue(BlockDirectional.FACING)).getOpposite() == i) {
               for(EnumFacing i2 : EnumFacing.VALUES) {
                  if (this.getBlock(pos.offset(i).offset(i2)) == Blocks.REDSTONE_BLOCK && this.mine.getValue()) {
                     this.mine(pos.offset(i).offset(i2));
                     if (this.autoDisable.getValue()) {
                        this.disable();
                     }

                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean doPush(BlockPos targetPos, EntityPlayer target) {
      if (this.checkPiston.getValue() && this.checkPiston(targetPos)) {
         return true;
      } else {
         if (mc.world.isAirBlock(targetPos.up(2))) {
            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof BlockPistonBase
                     && canPushBlock.contains(this.getBlock(pos.offset(i, -2)))
                     && (
                        this.getBlock(pos.offset(i, -2).up()) == Blocks.AIR
                           || this.getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK
                     )
                     && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i) {
                     for(EnumFacing i2 : EnumFacing.VALUES) {
                        if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                           if (this.mine.getValue()) {
                              this.mine(pos.offset(i2));
                           }

                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }
            }

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof BlockPistonBase
                     && canPushBlock.contains(this.getBlock(pos.offset(i, -2)))
                     && (
                        this.getBlock(pos.offset(i, -2).up()) == Blocks.AIR
                           || this.getBlock(pos.offset(i, -2).up()) == Blocks.REDSTONE_BLOCK
                     )
                     && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i
                     && this.doPower(pos)) {
                     return true;
                  }
               }
            }

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if ((
                        !(mc.player.posY - target.posY <= -1.0) && !(mc.player.posY - target.posY >= 2.0)
                           || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)
                     )
                     && (!this.attackCrystal(pos) || !this.crystalCheck.getValue())
                     && BlockUtil.canPlace2(pos)
                     && canPushBlock.contains(this.getBlock(pos.offset(i, -2)))
                     && canPushBlock.contains(this.getBlock(pos.offset(i, -2).up()))) {
                     if (BlockUtil.canBlockFacing(pos) || !this.downPower(pos)) {
                        this.doPiston(i, pos);
                        return true;
                     }
                     break;
                  }
               }
            }

            if ((this.getBlock(targetPos) != Blocks.AIR || !this.onlyBurrow.getValue()) && this.pullBack.getValue()) {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                     BlockPos pos = targetPos.offset(i).up();

                     for(EnumFacing i2 : EnumFacing.VALUES) {
                        if (this.getBlock(pos) instanceof BlockPistonBase
                           && this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK
                           && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i) {
                           this.mine(pos.offset(i2));
                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }

               for(EnumFacing i : EnumFacing.VALUES) {
                  if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                     BlockPos pos = targetPos.offset(i).up();

                     for(EnumFacing i2 : EnumFacing.VALUES) {
                        if (this.getBlock(pos) instanceof BlockPistonBase
                           && this.getBlock(pos.offset(i2)) == Blocks.AIR
                           && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i
                           && (!this.attackCrystal(pos.offset(i2)) || !this.crystalCheck.getValue())
                           && !this.doPower(pos, i2)) {
                           this.mine(pos.offset(i2));
                           return true;
                        }
                     }
                  }
               }

               for(EnumFacing i : EnumFacing.VALUES) {
                  if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                     BlockPos pos = targetPos.offset(i).up();
                     if ((
                           !(mc.player.posY - target.posY <= -1.0) && !(mc.player.posY - target.posY >= 2.0)
                              || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)
                        )
                        && (!this.attackCrystal(pos) || !this.crystalCheck.getValue())
                        && BlockUtil.canPlace2(pos)
                        && !this.downPower(pos)) {
                        this.doPiston(i, pos);
                        return true;
                     }
                  }
               }

               return false;
            }

            if (this.autoDisable.getValue()) {
               this.disable();
            }

            return true;
         } else {
            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof BlockPistonBase
                     && (
                        mc.world.isAirBlock(pos.offset(i, -2)) && mc.world.isAirBlock(pos.offset(i, -2).down())
                           || checkTarget(pos.offset(i, 2), target)
                     )
                     && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i) {
                     for(EnumFacing i2 : EnumFacing.VALUES) {
                        if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
                           if (this.mine.getValue()) {
                              this.mine(pos.offset(i2));
                           }

                           if (this.autoDisable.getValue()) {
                              this.disable();
                           }

                           return true;
                        }
                     }
                  }
               }
            }

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if (this.getBlock(pos) instanceof BlockPistonBase
                     && (
                        mc.world.isAirBlock(pos.offset(i, -2)) && mc.world.isAirBlock(pos.offset(i, -2).down())
                           || checkTarget(pos.offset(i, 2), target)
                     )
                     && ((EnumFacing)this.getBlockState(pos).getValue(BlockDirectional.FACING)).getOpposite() == i
                     && this.doPower(pos)) {
                     return true;
                  }
               }
            }

            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos pos = targetPos.offset(i).up();
                  if ((
                        !(mc.player.posY - target.posY <= -1.0) && !(mc.player.posY - target.posY >= 2.0)
                           || !(BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6)
                     )
                     && (!this.attackCrystal(pos) || !this.crystalCheck.getValue())
                     && BlockUtil.canPlace2(pos)
                     && (
                        mc.world.isAirBlock(pos.offset(i, -2)) && mc.world.isAirBlock(pos.offset(i, -2).down())
                           || checkTarget(pos.offset(i, 2), target)
                     )
                     && canPushBlock.contains(this.getBlock(pos.offset(i, -2).up()))) {
                     if (BlockUtil.canBlockFacing(pos) || !this.downPower(pos)) {
                        this.doPiston(i, pos);
                        return true;
                     }
                     break;
                  }
               }
            }
         }

         return false;
      }
   }

   private boolean doPower(BlockPos pos, EnumFacing i2) {
      if (!BlockUtil.canPlace(pos.offset(i2), this.placeRange.getValue())) {
         return true;
      } else {
         int old = mc.player.inventory.currentItem;
         InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
         BlockUtil.placeBlock(pos.offset(i2), EnumHand.MAIN_HAND, this.rotate.getValue(), this.redStonePacket.getValue());
         InventoryUtil.doSwap(old);
         return false;
      }
   }

   private boolean doPower(BlockPos pos) {
      EnumFacing facing = BlockUtil.getBestNeighboring(pos, null);
      if (facing != null) {
         if (this.attackCrystal(pos.offset(facing)) && this.crystalCheck.getValue()) {
            return true;
         }

         if (!this.doPower(pos, facing)) {
            return true;
         }
      }

      for(EnumFacing i2 : EnumFacing.VALUES) {
         if (this.attackCrystal(pos.offset(i2)) && this.crystalCheck.getValue()) {
            return true;
         }

         if (!this.doPower(pos, i2)) {
            return true;
         }
      }

      return false;
   }

   private boolean downPower(BlockPos pos) {
      if (!BlockUtil.canBlockFacing(pos)) {
         boolean noPower = true;

         for(EnumFacing i2 : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
               noPower = false;
               break;
            }
         }

         if (noPower) {
            if (!BlockUtil.canPlace(pos.add(0, -1, 0), this.placeRange.getValue())) {
               return true;
            }

            int old = mc.player.inventory.currentItem;
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.add(0, -1, 0), EnumHand.MAIN_HAND, this.rotate.getValue(), this.redStonePacket.getValue());
            InventoryUtil.doSwap(old);
         }
      }

      return false;
   }

   private void doPiston(EnumFacing i, BlockPos pos) {
      if (BlockUtil.canPlace(pos, this.placeRange.getValue())) {
         if (this.rotate.getValue()) {
            EntityUtil.facePlacePos(pos);
         }

         pistonFacing(i);
         int old = mc.player.inventory.currentItem;
         InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
         BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
         InventoryUtil.doSwap(old);
         if (this.rotate.getValue()) {
            EntityUtil.facePlacePos(pos);
         }

         for(EnumFacing i2 : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i2)) == Blocks.REDSTONE_BLOCK) {
               if (this.mine.getValue()) {
                  this.mine(pos.offset(i2));
               }

               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               return;
            }
         }

         this.doPower(pos);
      }
   }

   @Override
   public String getInfo() {
      return this.DisplayTarget != null ? this.DisplayTarget.getName() : null;
   }

   private boolean attackCrystal(BlockPos pos) {
      for(Entity crystal : mc.world.loadedEntityList) {
         if (crystal instanceof EntityEnderCrystal
            && (
               !(crystal.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5) > 4.0)
                  || !this.crystalCheck.getValue()
            )
            && (
               !(crystal.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5) > 2.0)
                  || this.crystalCheck.getValue()
            )) {
            CombatUtil.attackCrystal(crystal, this.rotate.getValue(), this.eatingPause.getValue());
            return true;
         }
      }

      return false;
   }

   private void mine(BlockPos pos) {
      CombatUtil.mineBlock(pos);
   }

   private Block getBlock(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock();
   }

   private IBlockState getBlockState(BlockPos pos) {
      return mc.world.getBlockState(pos);
   }

   private Boolean canPush(EntityPlayer player) {
      int progress = 0;
      if (!mc.world.isAirBlock(new BlockPos(player.posX + 1.0, player.posY + 0.5, player.posZ))) {
         ++progress;
      }

      if (!mc.world.isAirBlock(new BlockPos(player.posX - 1.0, player.posY + 0.5, player.posZ))) {
         ++progress;
      }

      if (!mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ + 1.0))) {
         ++progress;
      }

      if (!mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ - 1.0))) {
         ++progress;
      }

      if (mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 2.5, player.posZ))) {
         return !mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ))
            ? true
            : progress > this.surroundCheck.getValue() - 1;
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (i != EnumFacing.UP && i != EnumFacing.DOWN) {
               BlockPos pos = EntityUtil.getEntityPos(player).offset(i);
               if (mc.world.isAirBlock(pos) && mc.world.isAirBlock(pos.up()) || checkTarget(pos, this.DisplayTarget)) {
                  return !mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ))
                     ? true
                     : progress > this.surroundCheck.getValue() - 1;
               }
            }
         }

         return false;
      }
   }
}
