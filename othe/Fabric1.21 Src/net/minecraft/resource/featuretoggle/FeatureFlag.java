/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource.featuretoggle;

import net.minecraft.resource.featuretoggle.FeatureUniverse;

public class FeatureFlag {
    final FeatureUniverse universe;
    final long mask;

    FeatureFlag(FeatureUniverse universe, int id) {
        this.universe = universe;
        this.mask = 1L << id;
    }
}

