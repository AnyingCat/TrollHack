//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.AutoCenter;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockObsidian;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class SelfTrap extends Module {
   public static SelfTrap INSTANCE;
   final Timer timer = new Timer();
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
   public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0F, 0.0F, 36.0F, v -> this.breakCrystal.isOpen()));
   private final Setting<Boolean> eatingPause = this.add(new Setting<>("EatingPause", true, v -> this.breakCrystal.isOpen()));
   private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Boolean> center = this.add(new Setting<>("Center", true));
   private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
   private final Setting<Boolean> trapBody = this.add(new Setting<>("TrapBody", true));
   private final Setting<Boolean> trapStep = this.add(new Setting<>("TrapStep", true));
   private final Setting<Boolean> trapHead = this.add(new Setting<>("TrapHead", false).setParent());
   private final Setting<Boolean> headButton = this.add(new Setting<>("useButton", false, v -> this.trapHead.isOpen()));
   int progress = 0;
   private BlockPos startPos;

   public SelfTrap() {
      super("SelfTrap", "One Self Trap", Category.COMBAT);
      INSTANCE = this;
   }

   @Override
   public void onEnable() {
      this.startPos = EntityUtil.getPlayerPos();
      if (this.center.getValue()) {
         AutoCenter.INSTANCE.enable();
      }
   }

   @Override
   public void onTick() {
      if (InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN) == -1) {
         this.disable();
      } else if ((this.startPos == null || !this.startPos.equals(EntityUtil.getPlayerPos())) && this.autoDisable.getValue()) {
         this.disable();
      } else if (this.timer.passedMs((long)this.delay.getValue().intValue())) {
         this.progress = 0;
         BlockPos pos = EntityUtil.getPlayerPos();
         if (this.trapBody.getValue()) {
            this.placeBlock(pos.add(1, 0, 0));
            this.placeBlock(pos.add(1, 1, 0));
            this.placeBlock(pos.add(-1, 0, 0));
            this.placeBlock(pos.add(-1, 1, 0));
            this.placeBlock(pos.add(0, 0, 1));
            this.placeBlock(pos.add(0, 1, 1));
            this.placeBlock(pos.add(0, 0, -1));
            this.placeBlock(pos.add(0, 1, -1));
            if (!BlockUtil.canBlockFacing(pos.add(0, 0, -1))) {
               this.placeBlock(pos.add(0, -1, -1));
            }

            if (!BlockUtil.canBlockFacing(pos.add(0, 0, 1))) {
               this.placeBlock(pos.add(0, -1, 1));
            }

            if (!BlockUtil.canBlockFacing(pos.add(1, 0, 0))) {
               this.placeBlock(pos.add(1, -1, 0));
            }

            if (!BlockUtil.canBlockFacing(pos.add(-1, 0, 0))) {
               this.placeBlock(pos.add(-1, -1, 0));
            }
         }

         if (this.trapStep.getValue()) {
            this.placeBlock(pos.add(1, 2, 0));
            this.placeBlock(pos.add(-1, 2, 0));
            this.placeBlock(pos.add(0, 2, 1));
            this.placeBlock(pos.add(0, 2, -1));
         }

         if (this.trapHead.getValue()) {
            if (!BlockUtil.canPlaceEnum(pos.add(0, 2, 0))
               && BlockUtil.canPlace2(pos.up(2))
               && !this.placeBlock(pos.add(0, 2, -1))
               && !this.placeBlock(pos.add(0, 2, 1))
               && !this.placeBlock(pos.add(-1, 2, 0))) {
               this.placeBlock(pos.add(1, 2, 0));
            }

            this.placeBlock(pos.add(0, 2, 0));
         }
      }
   }

   private boolean placeBlock(BlockPos pos) {
      if (this.progress >= this.multiPlace.getValue()) {
         return false;
      } else if (!BlockUtil.canPlace3(pos)) {
         return false;
      } else if ((!this.breakCrystal.getValue() || !(EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue())) && !BlockUtil.canPlace(pos)) {
         return false;
      } else {
         if (this.breakCrystal.getValue() && EntityUtil.getHealth(mc.player) >= this.safeHealth.getValue()) {
            CombatUtil.attackCrystal(pos, this.rotate.getValue(), false);
         }

         int old = mc.player.inventory.currentItem;
         int block;
         if (pos.equals(EntityUtil.getPlayerPos().up(2)) && this.headButton.getValue()) {
            if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1 && InventoryUtil.findHotbarClass(BlockButton.class) == -1) {
               return false;
            }

            if (InventoryUtil.findHotbarClass(BlockButton.class) != -1) {
               block = InventoryUtil.findHotbarClass(BlockButton.class);
            } else {
               block = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);
            }
         } else {
            if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1) {
               return false;
            }

            block = InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN);
         }

         InventoryUtil.doSwap(block);
         BlockUtil.placeBlock(
            pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.breakCrystal.getValue(), this.eatingPause.getValue()
         );
         InventoryUtil.doSwap(old);
         ++this.progress;
         this.timer.reset();
         return true;
      }
   }
}
