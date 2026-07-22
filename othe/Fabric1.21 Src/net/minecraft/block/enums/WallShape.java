/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public enum WallShape implements StringIdentifiable
{
    NONE("none"),
    LOW("low"),
    TALL("tall");

    private final String name;

    private WallShape(String name) {
        this.name = name;
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this.name;
    }
}

