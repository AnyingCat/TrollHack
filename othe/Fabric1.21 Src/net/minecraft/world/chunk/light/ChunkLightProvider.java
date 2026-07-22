/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.chunk.light;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightStorage;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkLightProvider<M extends ChunkToNibbleArrayMap<M>, S extends LightStorage<M>>
implements ChunkLightingView {
    public static final int field_44729 = 15;
    protected static final int field_44730 = 1;
    protected static final long field_44731 = class_8531.packWithAllDirectionsSet(1);
    private static final int field_44732 = 512;
    protected static final Direction[] DIRECTIONS = Direction.values();
    protected final ChunkProvider chunkProvider;
    protected final S lightStorage;
    private final LongOpenHashSet blockPositionsToCheck = new LongOpenHashSet(512, 0.5f);
    private final LongArrayFIFOQueue field_44734 = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue field_44735 = new LongArrayFIFOQueue();
    private final BlockPos.Mutable reusableBlockPos = new BlockPos.Mutable();
    private static final int field_31709 = 2;
    private final long[] cachedChunkPositions = new long[2];
    private final LightSourceView[] cachedChunks = new LightSourceView[2];

    protected ChunkLightProvider(ChunkProvider chunkProvider, S lightStorage) {
        this.chunkProvider = chunkProvider;
        this.lightStorage = lightStorage;
        this.clearChunkCache();
    }

    public static boolean needsLightUpdate(BlockView blockView, BlockPos pos, BlockState oldState, BlockState newState) {
        if (newState == oldState) {
            return false;
        }
        return newState.getOpacity(blockView, pos) != oldState.getOpacity(blockView, pos) || newState.getLuminance() != oldState.getLuminance() || newState.hasSidedTransparency() || oldState.hasSidedTransparency();
    }

    public static int getRealisticOpacity(BlockView world, BlockState state1, BlockPos pos1, BlockState state2, BlockPos pos2, Direction direction, int opacity2) {
        VoxelShape voxelShape2;
        boolean bl = ChunkLightProvider.isTrivialForLighting(state1);
        boolean bl2 = ChunkLightProvider.isTrivialForLighting(state2);
        if (bl && bl2) {
            return opacity2;
        }
        VoxelShape voxelShape = bl ? VoxelShapes.empty() : state1.getCullingShape(world, pos1);
        VoxelShape voxelShape3 = voxelShape2 = bl2 ? VoxelShapes.empty() : state2.getCullingShape(world, pos2);
        if (VoxelShapes.adjacentSidesCoverSquare(voxelShape, voxelShape2, direction)) {
            return 16;
        }
        return opacity2;
    }

    public static VoxelShape getOpaqueShape(BlockView blockView, BlockPos pos, BlockState blockState, Direction direction) {
        return ChunkLightProvider.isTrivialForLighting(blockState) ? VoxelShapes.empty() : blockState.getCullingFace(blockView, pos, direction);
    }

    protected static boolean isTrivialForLighting(BlockState blockState) {
        return !blockState.isOpaque() || !blockState.hasSidedTransparency();
    }

    protected BlockState getStateForLighting(BlockPos pos) {
        int j;
        int i = ChunkSectionPos.getSectionCoord(pos.getX());
        LightSourceView lightSourceView = this.getChunk(i, j = ChunkSectionPos.getSectionCoord(pos.getZ()));
        if (lightSourceView == null) {
            return Blocks.BEDROCK.getDefaultState();
        }
        return lightSourceView.getBlockState(pos);
    }

    protected int getOpacity(BlockState state, BlockPos pos) {
        return Math.max(1, state.getOpacity(this.chunkProvider.getWorld(), pos));
    }

    protected boolean shapesCoverFullCube(long sourceId, BlockState sourceState, long targetId, BlockState targetState, Direction direction) {
        VoxelShape voxelShape = this.getOpaqueShape(sourceState, sourceId, direction);
        VoxelShape voxelShape2 = this.getOpaqueShape(targetState, targetId, direction.getOpposite());
        return VoxelShapes.unionCoversFullCube(voxelShape, voxelShape2);
    }

    protected VoxelShape getOpaqueShape(BlockState blockState, long pos, Direction direction) {
        return ChunkLightProvider.getOpaqueShape(this.chunkProvider.getWorld(), this.reusableBlockPos.set(pos), blockState, direction);
    }

    @Nullable
    protected LightSourceView getChunk(int chunkX, int chunkZ) {
        long l = ChunkPos.toLong(chunkX, chunkZ);
        for (int i = 0; i < 2; ++i) {
            if (l != this.cachedChunkPositions[i]) continue;
            return this.cachedChunks[i];
        }
        LightSourceView lightSourceView = this.chunkProvider.getChunk(chunkX, chunkZ);
        for (int j = 1; j > 0; --j) {
            this.cachedChunkPositions[j] = this.cachedChunkPositions[j - 1];
            this.cachedChunks[j] = this.cachedChunks[j - 1];
        }
        this.cachedChunkPositions[0] = l;
        this.cachedChunks[0] = lightSourceView;
        return lightSourceView;
    }

    private void clearChunkCache() {
        Arrays.fill(this.cachedChunkPositions, ChunkPos.MARKER);
        Arrays.fill(this.cachedChunks, null);
    }

    @Override
    public void checkBlock(BlockPos pos) {
        this.blockPositionsToCheck.add(pos.asLong());
    }

    public void enqueueSectionData(long sectionPos, @Nullable ChunkNibbleArray lightArray) {
        ((LightStorage)this.lightStorage).enqueueSectionData(sectionPos, lightArray);
    }

    public void setRetainColumn(ChunkPos pos, boolean retainData) {
        ((LightStorage)this.lightStorage).setRetainColumn(ChunkSectionPos.withZeroY(pos.x, pos.z), retainData);
    }

    @Override
    public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
        ((LightStorage)this.lightStorage).setSectionStatus(pos.asLong(), notReady);
    }

    @Override
    public void setColumnEnabled(ChunkPos pos, boolean retainData) {
        ((LightStorage)this.lightStorage).setColumnEnabled(ChunkSectionPos.withZeroY(pos.x, pos.z), retainData);
    }

    @Override
    public int doLightUpdates() {
        LongIterator longIterator = this.blockPositionsToCheck.iterator();
        while (longIterator.hasNext()) {
            this.method_51529(longIterator.nextLong());
        }
        this.blockPositionsToCheck.clear();
        this.blockPositionsToCheck.trim(512);
        int i = 0;
        i += this.method_51570();
        this.clearChunkCache();
        ((LightStorage)this.lightStorage).updateLight(this);
        ((LightStorage)this.lightStorage).notifyChanges();
        return i += this.method_51567();
    }

    private int method_51567() {
        int i = 0;
        while (!this.field_44735.isEmpty()) {
            long l = this.field_44735.dequeueLong();
            long m = this.field_44735.dequeueLong();
            int j = ((LightStorage)this.lightStorage).get(l);
            int k = class_8531.getLightLevel(m);
            if (class_8531.method_51582(m) && j < k) {
                ((LightStorage)this.lightStorage).set(l, k);
                j = k;
            }
            if (j == k) {
                this.method_51531(l, m, j);
            }
            ++i;
        }
        return i;
    }

    private int method_51570() {
        int i = 0;
        while (!this.field_44734.isEmpty()) {
            long l = this.field_44734.dequeueLong();
            long m = this.field_44734.dequeueLong();
            this.method_51530(l, m);
            ++i;
        }
        return i;
    }

    protected void method_51565(long blockPos, long flags) {
        this.field_44734.enqueue(blockPos);
        this.field_44734.enqueue(flags);
    }

    protected void method_51566(long blockPos, long flags) {
        this.field_44735.enqueue(blockPos);
        this.field_44735.enqueue(flags);
    }

    @Override
    public boolean hasUpdates() {
        return ((LightStorage)this.lightStorage).hasLightUpdates() || !this.blockPositionsToCheck.isEmpty() || !this.field_44734.isEmpty() || !this.field_44735.isEmpty();
    }

    @Override
    @Nullable
    public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
        return ((LightStorage)this.lightStorage).getLightSection(pos.asLong());
    }

    @Override
    public int getLightLevel(BlockPos pos) {
        return ((LightStorage)this.lightStorage).getLight(pos.asLong());
    }

    public String displaySectionLevel(long sectionPos) {
        return this.getStatus(sectionPos).getSigil();
    }

    public LightStorage.Status getStatus(long sectionPos) {
        return ((LightStorage)this.lightStorage).getStatus(sectionPos);
    }

    protected abstract void method_51529(long var1);

    protected abstract void method_51531(long var1, long var3, int var5);

    protected abstract void method_51530(long var1, long var3);

    public static class class_8531 {
        private static final int DIRECTION_BIT_OFFSET = 4;
        private static final int field_44738 = 6;
        private static final long field_44739 = 15L;
        private static final long DIRECTION_BIT_MASK = 1008L;
        private static final long field_44741 = 1024L;
        private static final long field_44742 = 2048L;

        public static long packWithOneDirectionCleared(int lightLevel, Direction direction) {
            long l = class_8531.clearDirectionBit(1008L, direction);
            return class_8531.withLightLevel(l, lightLevel);
        }

        public static long packWithAllDirectionsSet(int lightLevel) {
            return class_8531.withLightLevel(1008L, lightLevel);
        }

        public static long method_51573(int lightLevel, boolean trivial) {
            long l = 1008L;
            l |= 0x800L;
            if (trivial) {
                l |= 0x400L;
            }
            return class_8531.withLightLevel(l, lightLevel);
        }

        public static long method_51574(int lightLevel, boolean trivial, Direction direction) {
            long l = class_8531.clearDirectionBit(1008L, direction);
            if (trivial) {
                l |= 0x400L;
            }
            return class_8531.withLightLevel(l, lightLevel);
        }

        public static long method_51579(int lightLevel, boolean trivial, Direction direction) {
            long l = 0L;
            if (trivial) {
                l |= 0x400L;
            }
            l = class_8531.setDirectionBit(l, direction);
            return class_8531.withLightLevel(l, lightLevel);
        }

        public static long method_51578(boolean down, boolean north, boolean south, boolean west, boolean east) {
            long l = class_8531.withLightLevel(0L, 15);
            if (down) {
                l = class_8531.setDirectionBit(l, Direction.DOWN);
            }
            if (north) {
                l = class_8531.setDirectionBit(l, Direction.NORTH);
            }
            if (south) {
                l = class_8531.setDirectionBit(l, Direction.SOUTH);
            }
            if (west) {
                l = class_8531.setDirectionBit(l, Direction.WEST);
            }
            if (east) {
                l = class_8531.setDirectionBit(l, Direction.EAST);
            }
            return l;
        }

        public static int getLightLevel(long packed) {
            return (int)(packed & 0xFL);
        }

        public static boolean isTrivial(long packed) {
            return (packed & 0x400L) != 0L;
        }

        public static boolean method_51582(long packed) {
            return (packed & 0x800L) != 0L;
        }

        public static boolean isDirectionBitSet(long packed, Direction direction) {
            return (packed & 1L << direction.ordinal() + 4) != 0L;
        }

        private static long withLightLevel(long packed, int lightLevel) {
            return packed & 0xFFFFFFFFFFFFFFF0L | (long)lightLevel & 0xFL;
        }

        private static long setDirectionBit(long packed, Direction direction) {
            return packed | 1L << direction.ordinal() + 4;
        }

        private static long clearDirectionBit(long packed, Direction direction) {
            return packed & (1L << direction.ordinal() + 4 ^ 0xFFFFFFFFFFFFFFFFL);
        }
    }
}

