/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import net.minecraft.util.math.ChunkPos;

public interface ChunkFilter {
    public static final ChunkFilter IGNORE_ALL = new ChunkFilter(){

        @Override
        public boolean isWithinDistance(int x, int z, boolean includeEdge) {
            return false;
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
        }
    };

    public static ChunkFilter cylindrical(ChunkPos center, int viewDistance) {
        return new Cylindrical(center, viewDistance);
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void forEachChangedChunk(ChunkFilter oldFilter, ChunkFilter newFilter, Consumer<ChunkPos> newlyIncluded, Consumer<ChunkPos> justRemoved) {
        Cylindrical cylindrical2;
        Cylindrical cylindrical;
        block8: {
            block7: {
                if (oldFilter.equals(newFilter)) {
                    return;
                }
                if (!(oldFilter instanceof Cylindrical)) break block7;
                cylindrical = (Cylindrical)oldFilter;
                if (newFilter instanceof Cylindrical && cylindrical.overlaps(cylindrical2 = (Cylindrical)newFilter)) break block8;
            }
            oldFilter.forEach(justRemoved);
            newFilter.forEach(newlyIncluded);
            return;
        }
        int i = Math.min(cylindrical.getLeft(), cylindrical2.getLeft());
        int j = Math.min(cylindrical.getBottom(), cylindrical2.getBottom());
        int k = Math.max(cylindrical.getRight(), cylindrical2.getRight());
        int l = Math.max(cylindrical.getTop(), cylindrical2.getTop());
        int m = i;
        while (m <= k) {
            for (int n = j; n <= l; ++n) {
                boolean bl2;
                boolean bl = cylindrical.isWithinDistance(m, n);
                if (bl == (bl2 = cylindrical2.isWithinDistance(m, n))) continue;
                if (bl2) {
                    newlyIncluded.accept(new ChunkPos(m, n));
                    continue;
                }
                justRemoved.accept(new ChunkPos(m, n));
            }
            ++m;
        }
        return;
    }

    default public boolean isWithinDistance(ChunkPos pos) {
        return this.isWithinDistance(pos.x, pos.z);
    }

    default public boolean isWithinDistance(int x, int z) {
        return this.isWithinDistance(x, z, true);
    }

    public boolean isWithinDistance(int var1, int var2, boolean var3);

    public void forEach(Consumer<ChunkPos> var1);

    default public boolean isWithinDistanceExcludingEdge(int x, int z) {
        return this.isWithinDistance(x, z, false);
    }

    public static boolean isWithinDistanceExcludingEdge(int centerX, int centerZ, int viewDistance, int x, int z) {
        return ChunkFilter.isWithinDistance(centerX, centerZ, viewDistance, x, z, false);
    }

    public static boolean isWithinDistance(int centerX, int centerZ, int viewDistance, int x, int z, boolean includeEdge) {
        int i = Math.max(0, Math.abs(x - centerX) - 1);
        int j = Math.max(0, Math.abs(z - centerZ) - 1);
        long l = Math.max(0, Math.max(i, j) - (includeEdge ? 1 : 0));
        long m = Math.min(i, j);
        long n = m * m + l * l;
        int k = viewDistance * viewDistance;
        return n < (long)k;
    }

    public record Cylindrical(ChunkPos center, int viewDistance) implements ChunkFilter
    {
        int getLeft() {
            return this.center.x - this.viewDistance - 1;
        }

        int getBottom() {
            return this.center.z - this.viewDistance - 1;
        }

        int getRight() {
            return this.center.x + this.viewDistance + 1;
        }

        int getTop() {
            return this.center.z + this.viewDistance + 1;
        }

        @VisibleForTesting
        protected boolean overlaps(Cylindrical o) {
            return this.getLeft() <= o.getRight() && this.getRight() >= o.getLeft() && this.getBottom() <= o.getTop() && this.getTop() >= o.getBottom();
        }

        @Override
        public boolean isWithinDistance(int x, int z, boolean includeEdge) {
            return ChunkFilter.isWithinDistance(this.center.x, this.center.z, this.viewDistance, x, z, includeEdge);
        }

        @Override
        public void forEach(Consumer<ChunkPos> consumer) {
            for (int i = this.getLeft(); i <= this.getRight(); ++i) {
                for (int j = this.getBottom(); j <= this.getTop(); ++j) {
                    if (!this.isWithinDistance(i, j)) continue;
                    consumer.accept(new ChunkPos(i, j));
                }
            }
        }
    }
}

