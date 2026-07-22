/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum BlockFace implements StringIdentifiable
{
    FLOOR("floor"),
    WALL("wall"),
    CEILING("ceiling");

    private final String name;

    private BlockFace(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}

