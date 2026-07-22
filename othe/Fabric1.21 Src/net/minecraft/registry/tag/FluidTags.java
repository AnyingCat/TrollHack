/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry.tag;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class FluidTags {
    public static final TagKey<Fluid> WATER = FluidTags.of("water");
    public static final TagKey<Fluid> LAVA = FluidTags.of("lava");

    private FluidTags() {
    }

    private static TagKey<Fluid> of(String id) {
        return TagKey.of(RegistryKeys.FLUID, Identifier.ofVanilla(id));
    }
}

