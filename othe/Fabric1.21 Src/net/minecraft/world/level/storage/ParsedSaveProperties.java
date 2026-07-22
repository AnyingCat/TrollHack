/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.level.storage;

import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;

public record ParsedSaveProperties(SaveProperties properties, DimensionOptionsRegistryHolder.DimensionsConfig dimensions) {
}

