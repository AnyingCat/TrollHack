//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat.autotrap;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.BreakManager;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.AutoPush;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoTrap extends Module {
   final Timer timer = new Timer();
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   private final Setting<Float> range = this.add(new Setting<>("Range", 5.0F, 1.0F, 8.0F));
   private final Setting<AutoTrap.TargetMode> targetMod = this.add(new Setting<>("TargetMode", AutoTrap.TargetMode.Single));
   private final Setting<AutoTrap.Mode> mode = this.add(new Setting<>("Mode", AutoTrap.Mode.Trap));
   private final Setting<Boolean> helper = this.add(new Setting<>("Helper", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> helperDown = this.add(new Setting<>("HelperDown", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> extend = this.add(new Setting<>("extend", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> antiStep = this.add(new Setting<>("AntiStep", false, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> head = this.add(new Setting<>("Head", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> headExtend = this.add(new Setting<>("HeadExtend", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> chest = this.add(new Setting<>("Chest", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston).setParent());
   private final Setting<Boolean> onlyGround = this.add(
      new Setting<>("OnlyGround", true, v -> this.mode.getValue() != AutoTrap.Mode.Piston && this.chest.isOpen())
   );
   private final Setting<Boolean> legs = this.add(new Setting<>("Legs", false, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> down = this.add(new Setting<>("Down", false, v -> this.mode.getValue() != AutoTrap.Mode.Piston));
   private final Setting<Boolean> facing = this.add(new Setting<>("Facing", false, v -> this.mode.getValue() != AutoTrap.Mode.Trap));
   private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0F, 0.0F, 36.0F, v -> this.breakCrystal.isOpen()));
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.breakCrystal.isOpen()));
   public final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 500));
   private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0F, 1.0F, 6.0F));
   private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 30.0));
   private final Setting<Boolean> selfGround = this.add(new Setting<>("SelfGround", true));
   private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
   public final Setting<Boolean> render = this.add(new Setting<>("Render", true));
   public final Setting<Boolean> depth = this.add(new Setting<>("Depth", true, v -> this.render.getValue()));
   public final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.getValue()));
   public final Setting<Boolean> outline = this.add(new Setting<>("Outline", false, v -> this.render.getValue()));
   public final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 3.0F, 0.1F, 3.0F, v -> this.render.getValue()));
   public final Setting<Integer> fadeTime = this.add(new Setting<>("FadeTime", 500, 0, 5000, v -> this.render.getValue()));
   public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100), v -> this.render.getValue()));
   public final Setting<Boolean> sync = this.add(new Setting<>("Sync", true, v -> this.render.getValue()));
   public EntityPlayer target;
   public static AutoTrap INSTANCE;
   int progress = 0;
   private final ArrayList<BlockPos> trapList = new ArrayList();
   private final ArrayList<BlockPos> placeList = new ArrayList();

   public AutoTrap() {
      super("AutoTrap", "Automatically trap the enemy", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      this.trapList.clear();
      this.placeList.clear();
      this.progress = 0;
      if (this.selfGround.getValue() && !mc.player.onGround) {
         this.target = null;
         if (this.autoDisable.getValue()) {
            this.disable();
         }
      } else if (Managers.SPEED.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
         this.target = null;
         if (this.autoDisable.getValue()) {
            this.disable();
         }
      } else if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         if (this.targetMod.getValue() == AutoTrap.TargetMode.Single) {
            this.target = CombatUtil.getTarget((double)this.range.getValue().floatValue(), this.maxTargetSpeed.getMaxValue());
            if (this.target == null) {
               return;
            }

            this.trapTarget(this.target);
         } else if (this.targetMod.getValue() == AutoTrap.TargetMode.Multi) {
            boolean found = false;

            for(EntityPlayer player : mc.world.playerEntities) {
               if (!(Managers.SPEED.getPlayerSpeed(player) > this.maxTargetSpeed.getValue())
                  && !EntityUtil.invalid(player, (double)this.range.getValue().floatValue())) {
                  found = true;
                  this.target = player;
                  this.trapTarget(this.target);
               }
            }

            if (!found) {
               if (this.autoDisable.getValue()) {
                  this.disable();
               }

               this.target = null;
            }
         }
      }
   }

   private void trapTarget(EntityPlayer target) {
      if (this.mode.getValue() == AutoTrap.Mode.Trap) {
         this.doTrap(EntityUtil.getEntityPos(target));
      } else if (this.mode.getValue() == AutoTrap.Mode.Piston) {
         this.doPiston(EntityUtil.getEntityPos(target).up());
      } else {
         this.doAuto(EntityUtil.getEntityPos(target));
      }
   }

   private void doAuto(BlockPos pos) {
      if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
         this.doTrap(pos);
      } else {
         for(EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.UP
               && facing != EnumFacing.DOWN
               && mc.world.getBlockState(pos.up().offset(facing)).getBlock() instanceof BlockPistonBase) {
               return;
            }
         }

         for(EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.UP && facing != EnumFacing.DOWN && BlockUtil.canPlace(pos.up().offset(facing))) {
               this.placePiston(pos.up().offset(facing), facing);
               return;
            }
         }

         this.doTrap(pos);
      }
   }

   private void doPiston(BlockPos pos) {
      if (!(mc.world.getBlockState(pos.south()).getBlock() instanceof BlockPistonBase)
         && !(mc.world.getBlockState(pos.south(-1)).getBlock() instanceof BlockPistonBase)
         && !this.placePiston(pos.south(), EnumFacing.SOUTH)) {
         this.placePiston(pos.south(-1), EnumFacing.SOUTH.getOpposite());
      }

      if (!(mc.world.getBlockState(pos.east()).getBlock() instanceof BlockPistonBase)
         && !(mc.world.getBlockState(pos.east(-1)).getBlock() instanceof BlockPistonBase)
         && !this.placePiston(pos.east(), EnumFacing.EAST)) {
         this.placePiston(pos.east(-1), EnumFacing.EAST.getOpposite());
      }
   }

   private void doTrap(BlockPos pos) {
      if (!this.trapList.contains(pos)) {
         this.trapList.add(pos);
         if (this.antiStep.getValue() && BreakManager.isMine(pos.add(0, 2, 0))) {
            this.placeBlock(pos.add(0, 3, 0));
         }

         if (this.down.getValue()) {
            BlockPos offsetPos = pos.down();
            this.placeBlock(offsetPos);
            if (!BlockUtil.canPlaceEnum(offsetPos) && BlockUtil.canReplace(offsetPos) && this.getHelper(offsetPos) != null) {
               this.placeBlock(this.getHelper(offsetPos));
            }
         }

         if (this.headExtend.getValue()) {
            BlockPos offsetPos = pos.add(1, 0, 0);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(0, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(0, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(-1, 0, 0);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(1, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(-1, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(1, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }

            offsetPos = pos.add(-1, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.placeBlock(offsetPos.up(2));
            }
         }

         boolean trapChest = false;
         if (this.head.getValue() && BlockUtil.canReplace(pos.add(0, 2, 0))) {
            if (!BlockUtil.canPlaceEnum(pos.up(2))) {
               trapChest = true;
               if (this.getHelper(pos.up(2)) != null) {
                  this.placeBlock(this.getHelper(pos.up(2)));
                  trapChest = false;
               }
            }

            this.placeBlock(pos.up(2));
         }

         if (this.chest.getValue() && (!this.onlyGround.getValue() || this.target.onGround) || trapChest) {
            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos offsetPos = pos.offset(i).up();
                  this.placeBlock(offsetPos);
                  if (!BlockUtil.canPlaceEnum(offsetPos) && BlockUtil.canReplace(offsetPos) && this.getHelper(offsetPos) != null) {
                     this.placeBlock(this.getHelper(offsetPos));
                     if (!BlockUtil.canPlaceEnum(offsetPos.down())
                        && BlockUtil.canReplace(offsetPos.down())
                        && this.getHelper(offsetPos.down()) != null) {
                        this.placeBlock(this.getHelper(offsetPos.down()));
                     }
                  }
               }
            }
         }

         if (this.legs.getValue()) {
            for(EnumFacing i : EnumFacing.VALUES) {
               if (i != EnumFacing.DOWN && i != EnumFacing.UP) {
                  BlockPos offsetPos = pos.offset(i);
                  this.placeBlock(offsetPos);
                  if (!BlockUtil.canPlaceEnum(offsetPos) && BlockUtil.canReplace(offsetPos) && this.getHelper(offsetPos) != null) {
                     this.placeBlock(this.getHelper(offsetPos));
                  }
               }
            }
         }

         if (this.extend.getValue()) {
            BlockPos offsetPos = pos.add(1, 0, 0);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(0, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(0, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(-1, 0, 0);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(1, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(-1, 0, 1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(1, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }

            offsetPos = pos.add(-1, 0, -1);
            if (this.checkEntity(new BlockPos(offsetPos)) != null) {
               this.doTrap(offsetPos);
            }
         }
      }
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() + ", " + this.mode.getValue().name() : this.mode.getValue().name();
   }

   public BlockPos getHelper(BlockPos pos) {
      if (!this.helper.getValue()) {
         return null;
      } else if (this.helperDown.getValue() && BlockUtil.canPlace(pos.down())) {
         return pos.down();
      } else {
         for(EnumFacing i : EnumFacing.VALUES) {
            if (BlockUtil.canPlace(pos.offset(i))) {
               return pos.offset(i);
            }
         }

         return null;
      }
   }

   private Entity checkEntity(BlockPos pos) {
      Entity test = null;

      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer && entity != mc.player) {
            test = entity;
         }
      }

      return test;
   }

   private boolean placePiston(BlockPos pos, EnumFacing facing) {
      if (this.progress >= this.multiPlace.getValue()) {
         return false;
      } else if (!BlockUtil.canPlace(pos)) {
         return false;
      } else if (mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
         > (double)this.placeRange.getValue().floatValue()) {
         return false;
      } else {
         int old = mc.player.inventory.currentItem;
         if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            return false;
         } else {
            EnumFacing side = BlockUtil.getFirstFacing(pos);
            if (side == null) {
               return false;
            } else {
               InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
               if (this.facing.getValue()) {
                  AutoPush.pistonFacing(facing);
                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.packet.getValue());
                  BlockPos neighbour = pos.offset(side);
                  EnumFacing opposite = side.getOpposite();
                  Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
                  if (this.rotate.getValue()) {
                     EntityUtil.faceVector(hitVec);
                  }
               } else {
                  BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               }

               InventoryUtil.doSwap(old);
               this.timer.reset();
               ++this.progress;
               return true;
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      AutoTrapRender.addBlock(pos);
      if (BlockUtil.canPlace3(pos)) {
         if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue() || BlockUtil.canPlace(pos)) {
            if (this.progress < this.multiPlace.getValue()) {
               if (!(
                  mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5)
                     > (double)this.placeRange.getValue().floatValue()
               )) {
                  int old = mc.player.inventory.currentItem;
                  if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
                     if (!this.placeList.contains(pos)) {
                        this.placeList.add(pos);
                        if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue()) {
                           CombatUtil.attackCrystal(pos, this.rotate.getValue(), this.eatingPause.getValue());
                        }

                        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
                        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), !this.render.getValue());
                        InventoryUtil.doSwap(old);
                        this.timer.reset();
                        ++this.progress;
                     }
                  }
               }
            }
         }
      }
   }

   public static enum Mode {
      Trap,
      Piston,
      Auto;
   }

   public static enum TargetMode {
      Single,
      Multi;
   }
}
