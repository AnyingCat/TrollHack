/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.light.LevelPropagator;

public abstract class SectionDistanceLevelPropagator
extends LevelPropagator {
    protected SectionDistanceLevelPropagator(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected void propagateLevel(long id, int level, boolean decrease) {
        if (decrease && level >= this.levelCount - 2) {
            return;
        }
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    long l = ChunkSectionPos.offset(id, i, j, k);
                    if (l == id) continue;
                    this.propagateLevel(id, l, level, decrease);
                }
            }
        }
    }

    @Override
    protected int recalculateLevel(long id, long excludedId, int maxLevel) {
        int i = maxLevel;
        for (int j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                for (int l = -1; l <= 1; ++l) {
                    long m = ChunkSectionPos.offset(id, j, k, l);
                    if (m == id) {
                        m = Long.MAX_VALUE;
                    }
                    if (m == excludedId) continue;
                    int n = this.getPropagatedLevel(m, id, this.getLevel(m));
                    if (i > n) {
                        i = n;
                    }
                    if (i != 0) continue;
                    return i;
                }
            }
        }
        return i;
    }

    @Override
    protected int getPropagatedLevel(long sourceId, long targetId, int level) {
        if (this.isMarker(sourceId)) {
            return this.getInitialLevel(targetId);
        }
        return level + 1;
    }

    protected abstract int getInitialLevel(long var1);

    public void update(long id, int level, boolean decrease) {
        this.updateLevel(Long.MAX_VALUE, id, level, decrease);
    }
}

