/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.inventory;

import net.minecraft.inventory.Inventory;

/**
 * A functional interface used in {@link SimpleInventory#addListener}.
 * 
 * <p>Other inventories can listen for inventory changes by overriding
 * {@link Inventory#markDirty}.
 */
public interface InventoryChangedListener {
    public void onInventoryChanged(Inventory var1);
}

