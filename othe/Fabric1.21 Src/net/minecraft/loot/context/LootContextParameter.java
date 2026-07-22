/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.loot.context;

import net.minecraft.util.Identifier;

public class LootContextParameter<T> {
    private final Identifier id;

    public LootContextParameter(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return this.id;
    }

    public String toString() {
        return "<parameter " + String.valueOf(this.id) + ">";
    }
}

