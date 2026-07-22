package github.trollhack.utils.world;

import github.trollhack.utils.interfaces.Mc;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InventoryUtil implements Mc {

    public static ItemStack getStackInSlot(int i) {
        if (mc.player == null) return ItemStack.EMPTY;
        return mc.player.getInventory().getStack(i);
    }

    public static int getHotbarSlot(ItemStack stack) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (ItemStack.areItemsEqual(mc.player.getInventory().getStack(i), stack)) return i;
        }
        return -1;
    }

    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    public static int findItemInHotbar(Item item) {
        return findItem(item);
    }

    public static int findItemInventorySlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 45; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static int findClass(Class<?> clazz) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (isInstanceOf(stack, clazz)) return i;
        }
        return -1;
    }

    public static int findClassInventorySlot(Class<?> clazz) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (isInstanceOf(stack, clazz)) return i < 9 ? i + 36 : i;
        }
        return -1;
    }

    public static int findBlock(Block blockIn) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;
            if (((BlockItem) stack.getItem()).getBlock() == blockIn) return i;
        }
        return -1;
    }

    public static int findBlock() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem
                    && ((BlockItem) stack.getItem()).getBlock() != Blocks.COBWEB) {
                return i;
            }
        }
        return -1;
    }

    public static int findUnBlock() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof BlockItem)) return i;
        }
        return -1;
    }

    public static int findBlockInventorySlot(Block block) {
        return findItemInventorySlot(block.asItem());
    }

    public static boolean isInstanceOf(ItemStack stack, Class<?> clazz) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (clazz.isInstance(item)) return true;
        if (item instanceof BlockItem) {
            Block block = Block.getBlockFromItem(item);
            return clazz.isInstance(block);
        }
        return false;
    }

    public static boolean holdingItem(Class<?> clazz) {
        if (mc.player == null) return false;
        return isInstanceOf(mc.player.getMainHandStack(), clazz)
                || isInstanceOf(mc.player.getOffHandStack(), clazz);
    }

    public static Map<Integer, ItemStack> getInventoryAndHotbarSlots() {
        Map<Integer, ItemStack> slots = new HashMap<>();
        if (mc.player == null) return slots;
        for (int i = 0; i <= 44; i++) {
            slots.put(i, mc.player.getInventory().getStack(i));
        }
        return slots;
    }

    public static int getItemCount(Item item) {
        if (mc.player == null) return 0;
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (entry.getValue().getItem() == item) count += entry.getValue().getCount();
        }
        return count;
    }

    public static int getItemCount(Class<?> clazz) {
        if (mc.player == null) return 0;
        int count = 0;
        for (Map.Entry<Integer, ItemStack> entry : getInventoryAndHotbarSlots().entrySet()) {
            if (isInstanceOf(entry.getValue(), clazz)) count += entry.getValue().getCount();
        }
        return count;
    }

    public static int getPotionCount(StatusEffect targetEffect) {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() != Items.SPLASH_POTION) continue;
            for (StatusEffectInstance effect : stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).getEffects()) {
                if (effect.getEffectType() == targetEffect) {
                    count += stack.getCount();
                    break;
                }
            }
        }
        return count;
    }

    public static int getCrystalSlot() {
        return findItem(Items.END_CRYSTAL);
    }

    public static int getCrystalCount() {
        return getItemCount(Items.END_CRYSTAL);
    }

    public static boolean hasCrystal() {
        return getCrystalSlot() != -1;
    }

    public static boolean hasItem(Item item) {
        return findItem(item) != -1;
    }

    public static boolean hasItemInInventory(Item item) {
        return findItemInventorySlot(item) != -1;
    }

    public static boolean isHoldingCrystal() {
        if (mc.player == null) return false;
        return mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL
                || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
    }

    public static int getHoldingCrystalHand() {
        if (mc.player == null) return -1;
        if (mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL) return 0;
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) return 1;
        return -1;
    }

    public static void switchToSlot(int slot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (slot < 0 || slot > 8) return;
        if (mc.player.getInventory().selectedSlot == slot) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    public static void switchSilent(int slot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (slot < 0 || slot > 8) return;
        if (mc.player.getInventory().selectedSlot == slot) return;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }

    public static void switchBack(int originalSlot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (originalSlot < 0 || originalSlot > 8) return;
        if (mc.player.getInventory().selectedSlot == originalSlot) return;
        mc.player.getInventory().selectedSlot = originalSlot;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
    }

    public static void switchBackSilent(int originalSlot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (originalSlot < 0 || originalSlot > 8) return;
        if (mc.player.getInventory().selectedSlot == originalSlot) return;
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
    }

    public static void pickSlot(int slot) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (slot < 0 || slot > 8) return;
        mc.getNetworkHandler().sendPacket(new PickFromInventoryC2SPacket(slot));
    }

    public static void inventorySwap(int slot, int selectedSlot) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.currentScreenHandler == null) return;
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot,
                selectedSlot,
                SlotActionType.SWAP,
                mc.player
        );
    }

    public static void syncSelectedSlot() {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.syncSelectedSlot();
    }

    public static int findEmptySlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    public static int findItemInHotbar(Predicate<ItemStack> predicate) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (predicate.test(stack)) return i;
        }
        return -1;
    }

    public static int findItemInMainInventory(Predicate<ItemStack> predicate) {
        if (mc.player == null) return -1;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (predicate.test(stack)) return i;
        }
        return -1;
    }

    public static int inventoryToScreenSlot(int inventoryIndex) {
        if (inventoryIndex < 9) return inventoryIndex + 36;
        if (inventoryIndex < 36) return inventoryIndex;
        if (inventoryIndex < 40) return inventoryIndex - 31;
        if (inventoryIndex == 40) return 45;
        return -1;
    }

    public static void moveToOffhand(int sourceScreenSlot) {
        if (mc.player == null || mc.interactionManager == null || mc.player.currentScreenHandler == null) return;
        if (sourceScreenSlot < 0 || sourceScreenSlot > 45) return;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, sourceScreenSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, sourceScreenSlot, 0, SlotActionType.PICKUP, mc.player);
    }
}
