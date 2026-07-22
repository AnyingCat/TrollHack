/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

public interface LightingView {
    public void checkBlock(BlockPos var1);

    public boolean hasUpdates();

    public int doLightUpdates();

    default public void setSectionStatus(BlockPos pos, boolean notReady) {
        this.setSectionStatus(ChunkSectionPos.from(pos), notReady);
    }

    public void setSectionStatus(ChunkSectionPos var1, boolean var2);

    public void setColumnEnabled(ChunkPos var1, boolean var2);

    public void propagateLight(ChunkPos var1);
}

