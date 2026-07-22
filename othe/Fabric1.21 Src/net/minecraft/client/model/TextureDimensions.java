/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Internal class used by {@link TexturedModelData}.
 */
@Environment(value=EnvType.CLIENT)
public class TextureDimensions {
    final int width;
    final int height;

    public TextureDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
}

