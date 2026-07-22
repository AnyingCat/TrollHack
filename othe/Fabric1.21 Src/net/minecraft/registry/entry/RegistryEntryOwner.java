/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.entry;

/**
 * An owner of a {@link RegistryEntry} or {@link RegistryEntryList}. This is usually
 * a registry, but it is possible that an object owns multiple entries from
 * different registries.
 */
public interface RegistryEntryOwner<T> {
    default public boolean ownerEquals(RegistryEntryOwner<T> other) {
        return other == this;
    }
}

