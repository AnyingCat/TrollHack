//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryUtil implements Wrapper {
   public static int getItemDurability(ItemStack stack) {
      return stack == null ? 0 : stack.getMaxDamage() - stack.itemDamage;
   }

   public static boolean isNull(ItemStack stack) {
      return stack == null || stack.getItem() instanceof ItemAir;
   }

   public static void doSwap(int slot) {
      mc.player.inventory.currentItem = slot;
      mc.playerController.updateController();
   }

   public static void switchToHotbarSlot(Class clazz, boolean silent) {
      int slot = findHotbarClass(clazz);
      if (slot > -1) {
         switchToHotbarSlot(slot, silent);
      }
   }

   public static void switchToHotbarSlot(int slot, boolean silent) {
      if (mc.player.inventory.currentItem != slot && slot >= 0) {
         if (silent) {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.playerController.updateController();
         } else {
            mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();
         }
      }
   }

   public static int getItemHotbar(Item input) {
      for(int i = 0; i < 9; ++i) {
         Item item = mc.player.inventory.getStackInSlot(i).getItem();
         if (Item.getIdFromItem(item) == Item.getIdFromItem(input)) {
            return i;
         }
      }

      return -1;
   }

   public static int findHotbarClass(Class clazz) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof ItemBlock && clazz.isInstance(((ItemBlock)stack.getItem()).getBlock())) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int findHotbarBlock(Class clazz) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            if (clazz.isInstance(stack.getItem())) {
               return i;
            }

            if (stack.getItem() instanceof ItemBlock && clazz.isInstance(((ItemBlock)stack.getItem()).getBlock())) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int findHotbarBlock(Block blockIn) {
      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock && ((ItemBlock)stack.getItem()).getBlock() == blockIn) {
            return i;
         }
      }

      return -1;
   }

   public static int findItemInHotbar(Item itemToFind) {
      int slot = -1;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.player.inventory.getStackInSlot(i);
         if (stack != ItemStack.EMPTY) {
            stack.getItem();
            Item item = stack.getItem();
            if (item.equals(itemToFind)) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   public static int findClassInventorySlot(Class clazz, boolean offHand) {
      AtomicInteger slot = new AtomicInteger();
      slot.set(-1);

      for(Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
         if (clazz.isInstance(((ItemStack)entry.getValue()).getItem()) && (entry.getKey() != 45 || offHand)) {
            slot.set(entry.getKey());
            return slot.get();
         }
      }

      return slot.get();
   }

   public static int findItemInventorySlot(Item item, boolean offHand) {
      AtomicInteger slot = new AtomicInteger();
      slot.set(-1);

      for(Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
         if (((ItemStack)entry.getValue()).getItem() == item && (entry.getKey() != 45 || offHand)) {
            slot.set(entry.getKey());
            return slot.get();
         }
      }

      return slot.get();
   }

   public static List<Integer> findEmptySlots(boolean withXCarry) {
      ArrayList<Integer> outPut = new ArrayList<>();

      for(Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
         if (((ItemStack)entry.getValue()).isEmpty || ((ItemStack)entry.getValue()).getItem() == Items.AIR) {
            outPut.add(entry.getKey());
         }
      }

      if (withXCarry) {
         for(int i = 1; i < 5; ++i) {
            Slot craftingSlot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
            ItemStack craftingStack = craftingSlot.getStack();
            if (craftingStack.isEmpty() || craftingStack.getItem() == Items.AIR) {
               outPut.add(i);
            }
         }
      }

      return outPut;
   }

   public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
      HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<>();

      for(int current = 9; current <= 44; ++current) {
         fullInventorySlots.put(current, mc.player.inventoryContainer.getInventory().get(current));
      }

      return fullInventorySlots;
   }

   public static boolean holdingItem(Class clazz) {
      ItemStack stack = mc.player.getHeldItemMainhand();
      boolean result = isInstanceOf(stack, clazz);
      if (!result) {
         mc.player.getHeldItemOffhand();
         result = isInstanceOf(stack, clazz);
      }

      return result;
   }

   public static boolean isInstanceOf(ItemStack stack, Class clazz) {
      if (stack == null) {
         return false;
      } else {
         Item item = stack.getItem();
         if (clazz.isInstance(item)) {
            return true;
         } else if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            return clazz.isInstance(block);
         } else {
            return false;
         }
      }
   }

   public static int getEmptyXCarry() {
      for(int i = 1; i < 5; ++i) {
         Slot craftingSlot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
         ItemStack craftingStack = craftingSlot.getStack();
         if (craftingStack.isEmpty() || craftingStack.getItem() == Items.AIR) {
            return i;
         }
      }

      return -1;
   }

   public static boolean isSlotEmpty(int i) {
      Slot slot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
      ItemStack stack = slot.getStack();
      return stack.isEmpty();
   }

   public static int findArmorSlot(EntityEquipmentSlot type, boolean binding) {
      int slot = -1;
      float damage = 0.0F;

      for(int i = 9; i < 45; ++i) {
         ItemStack s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).getStack();
         ItemArmor armor;
         if (s.getItem() != Items.AIR
            && s.getItem() instanceof ItemArmor
            && (armor = (ItemArmor)s.getItem()).getEquipmentSlot() == type) {
            float currentDamage = (float)(armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));
            boolean cursed = binding && EnchantmentHelper.hasBindingCurse(s);
            if (currentDamage > damage && !cursed) {
               damage = currentDamage;
               slot = i;
            }
         }
      }

      return slot;
   }

   public static int findArmorSlot(EntityEquipmentSlot type, boolean binding, boolean withXCarry) {
      int slot = findArmorSlot(type, binding);
      if (slot == -1 && withXCarry) {
         float damage = 0.0F;

         for(int i = 1; i < 5; ++i) {
            Slot craftingSlot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
            ItemStack craftingStack = craftingSlot.getStack();
            ItemArmor armor;
            if (craftingStack.getItem() != Items.AIR
               && craftingStack.getItem() instanceof ItemArmor
               && (armor = (ItemArmor)craftingStack.getItem()).getEquipmentSlot() == type) {
               float currentDamage = (float)(armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, craftingStack));
               boolean cursed = binding && EnchantmentHelper.hasBindingCurse(craftingStack);
               if (currentDamage > damage && !cursed) {
                  damage = currentDamage;
                  slot = i;
               }
            }
         }
      }

      return slot;
   }

   public static int findItemInventorySlot(Class clazz, boolean offHand, boolean withXCarry) {
      int slot = findClassInventorySlot(clazz, offHand);
      if (slot == -1 && withXCarry) {
         for(int i = 1; i < 5; ++i) {
            Slot craftingSlot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
            ItemStack craftingStack = craftingSlot.getStack();
            if (craftingStack.getItem() != Items.AIR && clazz.isInstance(craftingStack.getItem())) {
               slot = i;
            }
         }
      }

      return slot;
   }

   public static int findItemInventorySlot(Item item, boolean offHand, boolean withXCarry) {
      int slot = findItemInventorySlot(item, offHand);
      if (slot == -1 && withXCarry) {
         for(int i = 1; i < 5; ++i) {
            Slot craftingSlot = (Slot)mc.player.inventoryContainer.inventorySlots.get(i);
            ItemStack craftingStack = craftingSlot.getStack();
            if (craftingStack.getItem() != Items.AIR && craftingStack.getItem() == item) {
               slot = i;
            }
         }
      }

      return slot;
   }

   public static int getItemCount(Item item) {
      int count = 0;
      if (mc.player.getHeldItemOffhand().getItem() == item) {
         count += mc.player.getHeldItemOffhand().getCount();
      }

      for(int i = 1; i < 5; ++i) {
         ItemStack itemStack = ((Slot)mc.player.inventoryContainer.inventorySlots.get(i)).getStack();
         if (itemStack.getItem() == item) {
            count += itemStack.getCount();
         }
      }

      for(Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
         if (((ItemStack)entry.getValue()).getItem() == item && entry.getKey() != 45) {
            count += ((ItemStack)entry.getValue()).getCount();
         }
      }

      return count;
   }

   public static class QueuedTask {
      private final int slot;
      private final boolean update;
      private final boolean quickClick;

      public QueuedTask() {
         this.update = true;
         this.slot = -1;
         this.quickClick = false;
      }

      public QueuedTask(int slot) {
         this.slot = slot;
         this.quickClick = false;
         this.update = false;
      }

      public QueuedTask(int slot, boolean quickClick) {
         this.slot = slot;
         this.quickClick = quickClick;
         this.update = false;
      }

      public void run() {
         if (this.update) {
            Wrapper.mc.playerController.updateController();
         }

         if (this.slot != -1) {
            Wrapper.mc.playerController.windowClick(0, this.slot, 0, this.quickClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, Wrapper.mc.player);
         }
      }
   }
}
