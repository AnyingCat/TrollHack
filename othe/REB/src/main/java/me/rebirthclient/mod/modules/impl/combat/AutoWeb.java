//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AutoWeb extends Module {
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 2000));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Boolean> raytrace = this.add(new Setting<>("Raytrace", false));
   private final Setting<Boolean> noInWeb = this.add(new Setting<>("NoInWeb", true));
   private final Setting<Boolean> checkSelf = this.add(new Setting<>("CheckSelf", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("SelfGround", true));
   private final Setting<Boolean> down = this.add(new Setting<>("Down", false));
   private final Setting<Boolean> face = this.add(new Setting<>("Face", false));
   private final Setting<Boolean> feet = this.add(new Setting<>("Feet", true).setParent());
   private final Setting<Boolean> onlyAir = this.add(new Setting<>("OnlyAir", true));
   private final Setting<Integer> surroundCheck = this.add(new Setting<>("SurroundCheck", 3, 0, 4, v -> this.feet.isOpen()));
   private final Setting<Boolean> air = this.add(new Setting<>("Air", true));
   private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
   private final Setting<Double> minTargetSpeed = this.add(new Setting<>("MinTargetSpeed", 0.0, 0.0, 20.0));
   private final Setting<Float> range = this.add(new Setting<>("Range", 5.0F, 1.0F, 6.0F));
   private final Timer timer = new Timer();
   private EntityPlayer target;
   private int progress = 0;

   public AutoWeb() {
      super("AutoWeb", "", Category.COMBAT);
   }

   @Override
   public String getInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   @Override
   public void onTick() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         if (this.onlyGround.getValue() && !mc.player.onGround) {
            this.target = null;
         } else if (Managers.SPEED.getPlayerSpeed(mc.player) > this.maxSelfSpeed.getValue()) {
            this.target = null;
         } else if (PullCrystal.INSTANCE.isOn() && PullCrystal.INSTANCE.pauseWeb.getValue()) {
            this.target = null;
         } else {
            this.progress = 0;
            boolean found = false;

            for(EntityPlayer player : mc.world.playerEntities) {
               if (!EntityUtil.invalid(player, (double)this.range.getValue().floatValue()) && (!isInWeb(player) || !this.noInWeb.getValue())) {
                  found = true;
                  this.target = player;
                  if (!this.onlyAir.getValue() || !player.onGround) {
                     if (this.down.getValue()) {
                        this.placeWeb(new BlockPos(this.target.posX, this.target.posY - 0.3, this.target.posZ));
                        this.placeWeb(new BlockPos(this.target.posX + 0.1, this.target.posY - 0.3, this.target.posZ + 0.1));
                        this.placeWeb(new BlockPos(this.target.posX - 0.1, this.target.posY - 0.3, this.target.posZ + 0.1));
                        this.placeWeb(new BlockPos(this.target.posX - 0.1, this.target.posY - 0.3, this.target.posZ - 0.1));
                        this.placeWeb(new BlockPos(this.target.posX + 0.1, this.target.posY - 0.3, this.target.posZ - 0.1));
                     }

                     if (this.face.getValue()) {
                        this.placeWeb(new BlockPos(this.target.posX + 0.2, this.target.posY + 1.5, this.target.posZ + 0.2));
                        this.placeWeb(new BlockPos(this.target.posX - 0.2, this.target.posY + 1.5, this.target.posZ + 0.2));
                        this.placeWeb(new BlockPos(this.target.posX - 0.2, this.target.posY + 1.5, this.target.posZ - 0.2));
                        this.placeWeb(new BlockPos(this.target.posX + 0.2, this.target.posY + 1.5, this.target.posZ - 0.2));
                     }

                     if ((!(Managers.SPEED.getPlayerSpeed(player) < this.minTargetSpeed.getValue()) || this.air.getValue() && !player.onGround)
                        && this.feet.getValue()
                        && !CombatUtil.isHole(EntityUtil.getEntityPos(this.target), true, this.surroundCheck.getValue(), false)) {
                        this.placeWeb(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
                        this.placeWeb(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ + 0.2));
                        this.placeWeb(new BlockPos(this.target.posX - 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
                        this.placeWeb(new BlockPos(this.target.posX + 0.2, this.target.posY + 0.5, this.target.posZ - 0.2));
                     }
                  }
               }
            }

            if (!found) {
               this.target = null;
            }
         }
      }
   }

   public static boolean isInWeb(EntityPlayer player) {
      if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 1.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 1.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 1.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 1.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY - 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY - 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY - 0.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY - 0.5, player.posZ - 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX + 0.3, player.posY + 0.5, player.posZ + 0.3))) {
         return true;
      } else if (isWeb(new BlockPos(player.posX - 0.3, player.posY + 0.5, player.posZ + 0.3))) {
         return true;
      } else {
         return isWeb(new BlockPos(player.posX - 0.3, player.posY + 0.5, player.posZ - 0.3))
            ? true
            : isWeb(new BlockPos(player.posX + 0.3, player.posY + 0.5, player.posZ - 0.3));
      }
   }

   private static boolean isWeb(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() == Blocks.WEB && checkEntity(pos);
   }

   private boolean isSelf(BlockPos pos) {
      if (!this.checkSelf.getValue()) {
         return false;
      } else {
         for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity == mc.player) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean checkEntity(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityPlayer && entity != mc.player) {
            return true;
         }
      }

      return false;
   }

   private void placeWeb(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         if (mc.world.isAirBlock(pos.up())) {
            if (this.canPlace(pos)) {
               if (!this.isSelf(pos)) {
                  if (InventoryUtil.findHotbarClass(BlockWeb.class) != -1) {
                     int old = mc.player.inventory.currentItem;
                     InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockWeb.class));
                     BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
                     ++this.progress;
                     InventoryUtil.doSwap(old);
                     this.timer.reset();
                  }
               }
            }
         }
      }
   }

   private boolean canPlace(BlockPos pos) {
      if (!BlockUtil.canBlockFacing(pos)) {
         return false;
      } else {
         return !BlockUtil.canReplace(pos) ? false : this.strictPlaceCheck(pos);
      }
   }

   private boolean strictPlaceCheck(BlockPos pos) {
      if (!CombatSetting.INSTANCE.strictPlace.getValue() && this.raytrace.getValue()) {
         return true;
      } else {
         for(EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue() || !this.raytrace.getValue())) {
            if (BlockUtil.canClick(pos.offset(side))) {
               return true;
            }
         }

         return false;
      }
   }
}
