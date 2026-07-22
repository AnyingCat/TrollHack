/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.storage;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record StorageKey(String level, RegistryKey<World> dimension, String type) {
    public StorageKey withSuffix(String suffix) {
        return new StorageKey(this.level, this.dimension, this.type + suffix);
    }
}

