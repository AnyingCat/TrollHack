/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;

/**
 * A registry that allows adding or modifying values.
 * Note that in vanilla, all registries are instances of this.
 * 
 * @see Registry
 */
public interface MutableRegistry<T>
extends Registry<T> {
    public RegistryEntry.Reference<T> add(RegistryKey<T> var1, T var2, RegistryEntryInfo var3);

    /**
     * {@return whether the registry is empty}
     */
    public boolean isEmpty();

    public RegistryEntryLookup<T> createMutableEntryLookup();
}

