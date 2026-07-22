//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CrystalDefense extends Module {
   public static CrystalDefense INSTANCE = new CrystalDefense();
   final Timer timer = new Timer();
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true));
   private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.breakCrystal.isOpen()));
   int progress = 0;
   private final List<BlockPos> crystalPos = new ArrayList();
   private BlockPos pos;

   public CrystalDefense() {
      super("CrystalDefense", "Surrounds you with Obsidian", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onTick() {
      if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.progress = 0;
         if (this.pos != null && !this.pos.equals(EntityUtil.getPlayerPos())) {
            this.crystalPos.clear();
         }

         this.pos = EntityUtil.getPlayerPos();
         if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) != -1) {
            if (!this.onlyGround.getValue() || mc.player.onGround) {
               for(EnumFacing i : EnumFacing.VALUES) {
                  if (i != EnumFacing.UP && !this.isGod(this.pos.offset(i).up())) {
                     BlockPos offsetPos = this.pos.offset(i).up(2);
                     if (this.checkCrystal(offsetPos) && !this.crystalPos.contains(offsetPos)) {
                        this.crystalPos.add(offsetPos);
                     }
                  }
               }

               boolean clear = true;

               for(BlockPos defensePos : this.crystalPos) {
                  if (mc.world.getBlockState(defensePos).getBlock() != Blocks.OBSIDIAN && BlockUtil.canPlace3(defensePos)) {
                     if (BlockUtil.canPlace(defensePos)) {
                        this.placeBlock(defensePos);
                        clear = false;
                     }

                     if (this.checkCrystal(defensePos)) {
                        if (this.breakCrystal.getValue()) {
                           CombatUtil.attackCrystal(defensePos, this.rotate.getValue(), this.eatingPause.getValue());
                        }

                        clear = false;
                     }
                  }
               }

               if (clear) {
                  this.crystalPos.clear();
               }
            }
         }
      }
   }

   private boolean checkCrystal(BlockPos pos) {
      for(Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
         if (entity instanceof EntityEnderCrystal && EntityUtil.getEntityPos(entity).equals(pos)) {
            return true;
         }
      }

      return false;
   }

   private boolean isGod(BlockPos pos) {
      return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
   }

   private void placeBlock(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         int old = mc.player.inventory.currentItem;
         if (InventoryUtil.findHotbarClass(BlockObsidian.class) != -1) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
            InventoryUtil.doSwap(old);
            ++this.progress;
            this.timer.reset();
         }
      }
   }
}
