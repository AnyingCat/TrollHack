/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen;

import net.minecraft.world.HeightLimitView;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class HeightContext {
    private final int minY;
    private final int height;

    public HeightContext(ChunkGenerator generator, HeightLimitView world) {
        this.minY = Math.max(world.getBottomY(), generator.getMinimumY());
        this.height = Math.min(world.getHeight(), generator.getWorldHeight());
    }

    public int getMinY() {
        return this.minY;
    }

    public int getHeight() {
        return this.height;
    }
}

