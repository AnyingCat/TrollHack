//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AnvilAura extends Module {
   public final Setting<Float> targetRange = this.add(new Setting<>("TargetRange", 5.0F, 0.0F, 10.0F));
   public final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 5.0F, 0.0F, 10.0F));
   public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
   public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 2000));
   private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
   private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 10.0, 0.0, 30.0));
   private final Timer delayTimer = new Timer();
   private int progress = 0;

   public AnvilAura() {
      super("AnvilAura", "Useless", Category.COMBAT);
   }

   @Override
   public void onTick() {
      if (this.delayTimer.passedMs((long)this.delay.getValue().intValue())) {
         this.progress = 0;

         for(EntityPlayer player : mc.world.playerEntities) {
            if (!(Managers.SPEED.getPlayerSpeed(player) > this.maxTargetSpeed.getValue())
               && !EntityUtil.invalid(player, (double)this.targetRange.getValue().floatValue())) {
               BlockPos pos = EntityUtil.getEntityPos(player);
               if (mc.world.isAirBlock(pos.up())) {
                  for(int i = 10; i > 1; --i) {
                     if (this.checkAnvil(pos.up(i), pos.up())) {
                        this.placeAnvil(pos.up(i));
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   private boolean checkAnvil(BlockPos anvilPos, BlockPos targetPos) {
      if (!this.canPlace(anvilPos)) {
         return false;
      } else {
         for(int i = 0; i < anvilPos.getY() - targetPos.getY(); ++i) {
            if (!mc.world.isAirBlock(anvilPos.down(i))) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean canPlace(BlockPos pos) {
      if (!BlockUtil.canPlace(pos)) {
         return false;
      } else {
         return !(
            mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5)
               > (double)this.placeRange.getValue().floatValue()
         );
      }
   }

   private void placeAnvil(BlockPos pos) {
      if (this.progress < this.multiPlace.getValue()) {
         if (InventoryUtil.findHotbarBlock(Blocks.ANVIL) != -1) {
            if (this.canPlace(pos)) {
               int old = mc.player.inventory.currentItem;
               this.delayTimer.reset();
               ++this.progress;
               InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.ANVIL));
               BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
               InventoryUtil.doSwap(old);
            }
         }
      }
   }
}
