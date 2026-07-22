/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;

public interface InventoryOwner {
    public static final String INVENTORY_KEY = "Inventory";

    public SimpleInventory getInventory();

    public static void pickUpItem(MobEntity entity, InventoryOwner inventoryOwner, ItemEntity item) {
        ItemStack itemStack = item.getStack();
        if (entity.canGather(itemStack)) {
            SimpleInventory simpleInventory = inventoryOwner.getInventory();
            boolean bl = simpleInventory.canInsert(itemStack);
            if (!bl) {
                return;
            }
            entity.triggerItemPickedUpByEntityCriteria(item);
            int i = itemStack.getCount();
            ItemStack itemStack2 = simpleInventory.addStack(itemStack);
            entity.sendPickup(item, i - itemStack2.getCount());
            if (itemStack2.isEmpty()) {
                item.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    default public void readInventory(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        if (nbt.contains(INVENTORY_KEY, NbtElement.LIST_TYPE)) {
            this.getInventory().readNbtList(nbt.getList(INVENTORY_KEY, NbtElement.COMPOUND_TYPE), wrapperLookup);
        }
    }

    default public void writeInventory(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbt.put(INVENTORY_KEY, this.getInventory().toNbtList(wrapperLookup));
    }
}

