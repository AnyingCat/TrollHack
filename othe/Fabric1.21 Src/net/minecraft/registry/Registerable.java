/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public interface Registerable<T> {
    public RegistryEntry.Reference<T> register(RegistryKey<T> var1, T var2, Lifecycle var3);

    default public RegistryEntry.Reference<T> register(RegistryKey<T> key, T value) {
        return this.register(key, value, Lifecycle.stable());
    }

    public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> var1);
}

