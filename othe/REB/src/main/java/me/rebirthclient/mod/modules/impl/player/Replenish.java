//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.player;

import java.util.ArrayList;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Replenish extends Module {
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 2, 0, 10));
   private final Setting<Integer> stack = this.add(new Setting<>("Stack", 50, 8, 64));
   private final Timer timer = new Timer();
   private final ArrayList<Item> Hotbar = new ArrayList();

   public Replenish() {
      super("Replenish", "Replenishes your hotbar", Category.PLAYER);
   }

   @Override
   public void onEnable() {
      if (!fullNullCheck()) {
         this.Hotbar.clear();

         for(int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && !this.Hotbar.contains(stack.getItem())) {
               this.Hotbar.add(stack.getItem());
            } else {
               this.Hotbar.add(Items.AIR);
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      if (mc.currentScreen == null) {
         if (this.timer.passedMs((long)(this.delay.getValue() * 1000))) {
            for(int i = 0; i < 9; ++i) {
               if (this.RefillSlotIfNeed(i)) {
                  this.timer.reset();
                  return;
               }
            }
         }
      }
   }

   private boolean RefillSlotIfNeed(int slot) {
      ItemStack stack = mc.player.inventory.getStackInSlot(slot);
      if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
         if (!stack.isStackable()) {
            return false;
         } else if (stack.getCount() >= stack.getMaxStackSize()) {
            return false;
         } else if (stack.getCount() >= this.stack.getValue()) {
            return false;
         } else {
            for(int i = 9; i < 36; ++i) {
               ItemStack item = mc.player.inventory.getStackInSlot(i);
               if (!item.isEmpty() && this.CanItemBeMergedWith(stack, item)) {
                  mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                  mc.playerController.updateController();
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean CanItemBeMergedWith(ItemStack source, ItemStack stack) {
      return source.getItem() == stack.getItem() && source.getDisplayName().equals(stack.getDisplayName());
   }
}
