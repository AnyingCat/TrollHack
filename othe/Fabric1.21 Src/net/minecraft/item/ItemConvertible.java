/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.item.Item;

/**
 * Represents an object that has an item form.
 */
public interface ItemConvertible {
    /**
     * Gets this object in its item form.
     */
    public Item asItem();
}

