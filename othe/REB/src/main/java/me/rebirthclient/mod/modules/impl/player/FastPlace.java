//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.item.ItemExpBottle;

public class FastPlace extends Module {
   public final Setting<Boolean> enderChest = this.add(new Setting<>("OnlyEnderChest", true));
   public final Setting<Boolean> exp = this.add(new Setting<>("OnlyEXP", true));
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 20, 0, 100));
   private final Timer delayTimer = new Timer();

   public FastPlace() {
      super("FastPlace", "Fast projectile", Category.PLAYER);
   }

   @Override
   public void onUpdate() {
      if (!fullNullCheck()) {
         if (!this.enderChest.getValue() || InventoryUtil.holdingItem(BlockEnderChest.class) || this.exp.getValue()) {
            if (!this.exp.getValue() || InventoryUtil.holdingItem(ItemExpBottle.class)) {
               if (this.delayTimer.passedMs((long)this.delay.getValue().intValue())) {
                  mc.rightClickDelayTimer = 1;
                  this.delayTimer.reset();
               }
            }
         }
      }
   }
}
