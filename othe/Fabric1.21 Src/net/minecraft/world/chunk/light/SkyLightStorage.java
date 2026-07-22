/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.LightStorage;

public class SkyLightStorage
extends LightStorage<Data> {
    protected SkyLightStorage(ChunkProvider chunkProvider) {
        super(LightType.SKY, chunkProvider, new Data(new Long2ObjectOpenHashMap<ChunkNibbleArray>(), new Long2IntOpenHashMap(), Integer.MAX_VALUE));
    }

    @Override
    protected int getLight(long blockPos) {
        return this.getLight(blockPos, false);
    }

    protected int getLight(long blockPos, boolean cached) {
        long l = ChunkSectionPos.fromBlockPos(blockPos);
        int i = ChunkSectionPos.unpackY(l);
        Data data = cached ? (Data)this.storage : (Data)this.uncachedStorage;
        int j = data.columnToTopSection.get(ChunkSectionPos.withZeroY(l));
        if (j == data.minSectionY || i >= j) {
            if (cached && !this.isSectionInEnabledColumn(l)) {
                return 0;
            }
            return 15;
        }
        ChunkNibbleArray chunkNibbleArray = this.getLightSection(data, l);
        if (chunkNibbleArray == null) {
            blockPos = BlockPos.removeChunkSectionLocalY(blockPos);
            while (chunkNibbleArray == null) {
                if (++i >= j) {
                    return 15;
                }
                l = ChunkSectionPos.offset(l, Direction.UP);
                chunkNibbleArray = this.getLightSection(data, l);
            }
        }
        return chunkNibbleArray.get(ChunkSectionPos.getLocalCoord(BlockPos.unpackLongX(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongY(blockPos)), ChunkSectionPos.getLocalCoord(BlockPos.unpackLongZ(blockPos)));
    }

    @Override
    protected void onLoadSection(long sectionPos) {
        long l;
        int j;
        int i = ChunkSectionPos.unpackY(sectionPos);
        if (((Data)this.storage).minSectionY > i) {
            ((Data)this.storage).minSectionY = i;
            ((Data)this.storage).columnToTopSection.defaultReturnValue(((Data)this.storage).minSectionY);
        }
        if ((j = ((Data)this.storage).columnToTopSection.get(l = ChunkSectionPos.withZeroY(sectionPos))) < i + 1) {
            ((Data)this.storage).columnToTopSection.put(l, i + 1);
        }
    }

    @Override
    protected void onUnloadSection(long sectionPos) {
        long l = ChunkSectionPos.withZeroY(sectionPos);
        int i = ChunkSectionPos.unpackY(sectionPos);
        if (((Data)this.storage).columnToTopSection.get(l) == i + 1) {
            long m = sectionPos;
            while (!this.hasSection(m) && this.isAboveMinHeight(i)) {
                --i;
                m = ChunkSectionPos.offset(m, Direction.DOWN);
            }
            if (this.hasSection(m)) {
                ((Data)this.storage).columnToTopSection.put(l, i + 1);
            } else {
                ((Data)this.storage).columnToTopSection.remove(l);
            }
        }
    }

    @Override
    protected ChunkNibbleArray createSection(long sectionPos) {
        ChunkNibbleArray chunkNibbleArray2;
        ChunkNibbleArray chunkNibbleArray = (ChunkNibbleArray)this.queuedSections.get(sectionPos);
        if (chunkNibbleArray != null) {
            return chunkNibbleArray;
        }
        int i = ((Data)this.storage).columnToTopSection.get(ChunkSectionPos.withZeroY(sectionPos));
        if (i == ((Data)this.storage).minSectionY || ChunkSectionPos.unpackY(sectionPos) >= i) {
            if (this.isSectionInEnabledColumn(sectionPos)) {
                return new ChunkNibbleArray(15);
            }
            return new ChunkNibbleArray();
        }
        long l = ChunkSectionPos.offset(sectionPos, Direction.UP);
        while ((chunkNibbleArray2 = this.getLightSection(l, true)) == null) {
            l = ChunkSectionPos.offset(l, Direction.UP);
        }
        return SkyLightStorage.copy(chunkNibbleArray2);
    }

    private static ChunkNibbleArray copy(ChunkNibbleArray source) {
        if (source.isArrayUninitialized()) {
            return source.copy();
        }
        byte[] bs = source.asByteArray();
        byte[] cs = new byte[2048];
        for (int i = 0; i < 16; ++i) {
            System.arraycopy(bs, 0, cs, i * 128, 128);
        }
        return new ChunkNibbleArray(cs);
    }

    protected boolean isAboveMinHeight(int sectionY) {
        return sectionY >= ((Data)this.storage).minSectionY;
    }

    protected boolean isAtOrAboveTopmostSection(long sectionPos) {
        long l = ChunkSectionPos.withZeroY(sectionPos);
        int i = ((Data)this.storage).columnToTopSection.get(l);
        return i == ((Data)this.storage).minSectionY || ChunkSectionPos.unpackY(sectionPos) >= i;
    }

    protected int getTopSectionForColumn(long columnPos) {
        return ((Data)this.storage).columnToTopSection.get(columnPos);
    }

    protected int getMinSectionY() {
        return ((Data)this.storage).minSectionY;
    }

    protected static final class Data
    extends ChunkToNibbleArrayMap<Data> {
        int minSectionY;
        final Long2IntOpenHashMap columnToTopSection;

        public Data(Long2ObjectOpenHashMap<ChunkNibbleArray> arrays, Long2IntOpenHashMap columnToTopSection, int minSectionY) {
            super(arrays);
            this.columnToTopSection = columnToTopSection;
            columnToTopSection.defaultReturnValue(minSectionY);
            this.minSectionY = minSectionY;
        }

        @Override
        public Data copy() {
            return new Data((Long2ObjectOpenHashMap<ChunkNibbleArray>)this.arrays.clone(), this.columnToTopSection.clone(), this.minSectionY);
        }

        @Override
        public /* synthetic */ ChunkToNibbleArrayMap copy() {
            return this.copy();
        }
    }
}

