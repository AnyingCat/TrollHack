/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.Box;

public interface Hopper
extends Inventory {
    public static final Box INPUT_AREA_SHAPE = Block.createCuboidShape(0.0, 11.0, 0.0, 16.0, 32.0, 16.0).getBoundingBoxes().get(0);

    default public Box getInputAreaShape() {
        return INPUT_AREA_SHAPE;
    }

    public double getHopperX();

    public double getHopperY();

    public double getHopperZ();

    public boolean canBlockFromAbove();
}

