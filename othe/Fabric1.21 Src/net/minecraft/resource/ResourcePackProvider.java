/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import java.util.function.Consumer;
import net.minecraft.resource.ResourcePackProfile;

/**
 * A resource pack provider provides {@link ResourcePackProfile}s, usually to
 * {@link ResourcePackManager}s.
 */
@FunctionalInterface
public interface ResourcePackProvider {
    /**
     * Register resource pack profiles created with the {@code factory} to the
     * {@code profileAdder}.
     * 
     * @see ResourcePackProfile
     * 
     * @param profileAdder the profile adder that accepts created resource pack profiles
     */
    public void register(Consumer<ResourcePackProfile> var1);
}

