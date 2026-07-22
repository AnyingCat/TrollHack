/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ChunkRenderingDataPreparer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Direction[] field_45618 = Direction.values();
    private static final int field_45619 = 60;
    private static final double field_45620 = Math.ceil(Math.sqrt(3.0) * 16.0);
    private boolean field_45621 = true;
    @Nullable
    private Future<?> field_45622;
    @Nullable
    private BuiltChunkStorage field_45623;
    private final AtomicReference<class_8681> field_45624 = new AtomicReference();
    private final AtomicReference<class_8680> field_45625 = new AtomicReference();
    private final AtomicBoolean field_45626 = new AtomicBoolean(false);

    public void method_52826(@Nullable BuiltChunkStorage builtChunkStorage) {
        if (this.field_45622 != null) {
            try {
                this.field_45622.get();
                this.field_45622 = null;
            } catch (Exception exception) {
                LOGGER.warn("Full update failed", exception);
            }
        }
        this.field_45623 = builtChunkStorage;
        if (builtChunkStorage != null) {
            this.field_45624.set(new class_8681(builtChunkStorage.chunks.length));
            this.method_52817();
        } else {
            this.field_45624.set(null);
        }
    }

    public void method_52817() {
        this.field_45621 = true;
    }

    public void method_52828(Frustum frustum, List<ChunkBuilder.BuiltChunk> list) {
        for (ChunkInfo chunkInfo : this.field_45624.get().storage().chunks) {
            if (!frustum.isVisible(chunkInfo.chunk.getBoundingBox())) continue;
            list.add(chunkInfo.chunk);
        }
    }

    public boolean method_52836() {
        return this.field_45626.compareAndSet(true, false);
    }

    public void method_52819(ChunkPos chunkPos) {
        class_8680 lv2;
        class_8680 lv = this.field_45625.get();
        if (lv != null) {
            this.method_52822(lv, chunkPos);
        }
        if ((lv2 = this.field_45624.get().events) != lv) {
            this.method_52822(lv2, chunkPos);
        }
    }

    public void method_52827(ChunkBuilder.BuiltChunk builtChunk) {
        class_8680 lv2;
        class_8680 lv = this.field_45625.get();
        if (lv != null) {
            lv.sectionsToPropagateFrom.add(builtChunk);
        }
        if ((lv2 = this.field_45624.get().events) != lv) {
            lv2.sectionsToPropagateFrom.add(builtChunk);
        }
    }

    public void method_52834(boolean bl, Camera camera, Frustum frustum, List<ChunkBuilder.BuiltChunk> list) {
        Vec3d vec3d = camera.getPos();
        if (this.field_45621 && (this.field_45622 == null || this.field_45622.isDone())) {
            this.method_52833(bl, camera, vec3d);
        }
        this.method_52835(bl, frustum, list, vec3d);
    }

    private void method_52833(boolean bl, Camera camera, Vec3d vec3d) {
        this.field_45621 = false;
        this.field_45622 = Util.getMainWorkerExecutor().submit(() -> {
            class_8681 lv = new class_8681(this.field_45623.chunks.length);
            this.field_45625.set(lv.events);
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            this.method_52821(camera, queue);
            queue.forEach(chunkInfo -> arg.storage.field_45627.setInfo(chunkInfo.chunk, (ChunkInfo)chunkInfo));
            this.method_52825(lv.storage, vec3d, queue, bl, builtChunk -> {});
            this.field_45624.set(lv);
            this.field_45625.set(null);
            this.field_45626.set(true);
        });
    }

    private void method_52835(boolean bl, Frustum frustum, List<ChunkBuilder.BuiltChunk> list, Vec3d vec3d) {
        class_8681 lv = this.field_45624.get();
        this.method_52823(lv);
        if (!lv.events.sectionsToPropagateFrom.isEmpty()) {
            ArrayDeque<ChunkInfo> queue = Queues.newArrayDeque();
            while (!lv.events.sectionsToPropagateFrom.isEmpty()) {
                ChunkBuilder.BuiltChunk builtChunk2 = (ChunkBuilder.BuiltChunk)lv.events.sectionsToPropagateFrom.poll();
                ChunkInfo chunkInfo = lv.storage.field_45627.getInfo(builtChunk2);
                if (chunkInfo == null || chunkInfo.chunk != builtChunk2) continue;
                queue.add(chunkInfo);
            }
            Frustum frustum2 = WorldRenderer.method_52816(frustum);
            Consumer<ChunkBuilder.BuiltChunk> consumer = builtChunk -> {
                if (frustum2.isVisible(builtChunk.getBoundingBox())) {
                    list.add((ChunkBuilder.BuiltChunk)builtChunk);
                }
            };
            this.method_52825(lv.storage, vec3d, queue, bl, consumer);
        }
    }

    private void method_52823(class_8681 arg) {
        LongIterator longIterator = arg.events.chunksWhichReceivedNeighbors.iterator();
        while (longIterator.hasNext()) {
            long l = longIterator.nextLong();
            List list = (List)arg.storage.field_45628.get(l);
            if (list == null || !((ChunkBuilder.BuiltChunk)list.get(0)).shouldBuild()) continue;
            arg.events.sectionsToPropagateFrom.addAll(list);
            arg.storage.field_45628.remove(l);
        }
        arg.events.chunksWhichReceivedNeighbors.clear();
    }

    private void method_52822(class_8680 arg, ChunkPos chunkPos) {
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x - 1, chunkPos.z));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x, chunkPos.z - 1));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x + 1, chunkPos.z));
        arg.chunksWhichReceivedNeighbors.add(ChunkPos.toLong(chunkPos.x, chunkPos.z + 1));
    }

    private void method_52821(Camera camera, Queue<ChunkInfo> queue) {
        int i = 16;
        Vec3d vec3d = camera.getPos();
        BlockPos blockPos = camera.getBlockPos();
        ChunkBuilder.BuiltChunk builtChunk = this.field_45623.getRenderedChunk(blockPos);
        if (builtChunk == null) {
            HeightLimitView heightLimitView = this.field_45623.getWorld();
            boolean bl = blockPos.getY() > heightLimitView.getBottomY();
            int j = bl ? heightLimitView.getTopY() - 8 : heightLimitView.getBottomY() + 8;
            int k = MathHelper.floor(vec3d.x / 16.0) * 16;
            int l = MathHelper.floor(vec3d.z / 16.0) * 16;
            int m = this.field_45623.getViewDistance();
            ArrayList<ChunkInfo> list = Lists.newArrayList();
            for (int n = -m; n <= m; ++n) {
                for (int o = -m; o <= m; ++o) {
                    ChunkBuilder.BuiltChunk builtChunk2 = this.field_45623.getRenderedChunk(new BlockPos(k + ChunkSectionPos.getOffsetPos(n, 8), j, l + ChunkSectionPos.getOffsetPos(o, 8)));
                    if (builtChunk2 == null || !this.method_52832(blockPos, builtChunk2.getOrigin())) continue;
                    Direction direction = bl ? Direction.DOWN : Direction.UP;
                    ChunkInfo chunkInfo2 = new ChunkInfo(builtChunk2, direction, 0);
                    chunkInfo2.updateCullingState(chunkInfo2.cullingState, direction);
                    if (n > 0) {
                        chunkInfo2.updateCullingState(chunkInfo2.cullingState, Direction.EAST);
                    } else if (n < 0) {
                        chunkInfo2.updateCullingState(chunkInfo2.cullingState, Direction.WEST);
                    }
                    if (o > 0) {
                        chunkInfo2.updateCullingState(chunkInfo2.cullingState, Direction.SOUTH);
                    } else if (o < 0) {
                        chunkInfo2.updateCullingState(chunkInfo2.cullingState, Direction.NORTH);
                    }
                    list.add(chunkInfo2);
                }
            }
            list.sort(Comparator.comparingDouble(chunkInfo -> blockPos.getSquaredDistance(chunkInfo.chunk.getOrigin().add(8, 8, 8))));
            queue.addAll(list);
        } else {
            queue.add(new ChunkInfo(builtChunk, null, 0));
        }
    }

    private void method_52825(RenderableChunks renderableChunks, Vec3d vec3d, Queue<ChunkInfo> queue, boolean bl, Consumer<ChunkBuilder.BuiltChunk> consumer) {
        int i = 16;
        BlockPos blockPos = new BlockPos(MathHelper.floor(vec3d.x / 16.0) * 16, MathHelper.floor(vec3d.y / 16.0) * 16, MathHelper.floor(vec3d.z / 16.0) * 16);
        BlockPos blockPos2 = blockPos.add(8, 8, 8);
        while (!queue.isEmpty()) {
            ChunkInfo chunkInfo = queue.poll();
            ChunkBuilder.BuiltChunk builtChunk = chunkInfo.chunk;
            if (renderableChunks.chunks.add(chunkInfo)) {
                consumer.accept(chunkInfo.chunk);
            }
            boolean bl2 = Math.abs(builtChunk.getOrigin().getX() - blockPos.getX()) > 60 || Math.abs(builtChunk.getOrigin().getY() - blockPos.getY()) > 60 || Math.abs(builtChunk.getOrigin().getZ() - blockPos.getZ()) > 60;
            for (Direction direction : field_45618) {
                ChunkInfo chunkInfo2;
                ChunkBuilder.BuiltChunk builtChunk2 = this.method_52831(blockPos, builtChunk, direction);
                if (builtChunk2 == null || bl && chunkInfo.canCull(direction.getOpposite())) continue;
                if (bl && chunkInfo.hasAnyDirection()) {
                    ChunkBuilder.ChunkData chunkData = builtChunk.getData();
                    boolean bl3 = false;
                    for (int j = 0; j < field_45618.length; ++j) {
                        if (!chunkInfo.hasDirection(j) || !chunkData.isVisibleThrough(field_45618[j].getOpposite(), direction)) continue;
                        bl3 = true;
                        break;
                    }
                    if (!bl3) continue;
                }
                if (bl && bl2) {
                    BlockPos blockPos3 = builtChunk2.getOrigin();
                    BlockPos blockPos4 = blockPos3.add((direction.getAxis() == Direction.Axis.X ? blockPos2.getX() > blockPos3.getX() : blockPos2.getX() < blockPos3.getX()) ? 16 : 0, (direction.getAxis() == Direction.Axis.Y ? blockPos2.getY() > blockPos3.getY() : blockPos2.getY() < blockPos3.getY()) ? 16 : 0, (direction.getAxis() == Direction.Axis.Z ? blockPos2.getZ() > blockPos3.getZ() : blockPos2.getZ() < blockPos3.getZ()) ? 16 : 0);
                    Vec3d vec3d2 = new Vec3d(blockPos4.getX(), blockPos4.getY(), blockPos4.getZ());
                    Vec3d vec3d3 = vec3d.subtract(vec3d2).normalize().multiply(field_45620);
                    boolean bl4 = true;
                    while (vec3d.subtract(vec3d2).lengthSquared() > 3600.0) {
                        vec3d2 = vec3d2.add(vec3d3);
                        HeightLimitView heightLimitView = this.field_45623.getWorld();
                        if (vec3d2.y > (double)heightLimitView.getTopY() || vec3d2.y < (double)heightLimitView.getBottomY()) break;
                        ChunkBuilder.BuiltChunk builtChunk3 = this.field_45623.getRenderedChunk(BlockPos.ofFloored(vec3d2.x, vec3d2.y, vec3d2.z));
                        if (builtChunk3 != null && renderableChunks.field_45627.getInfo(builtChunk3) != null) continue;
                        bl4 = false;
                        break;
                    }
                    if (!bl4) continue;
                }
                if ((chunkInfo2 = renderableChunks.field_45627.getInfo(builtChunk2)) != null) {
                    chunkInfo2.addDirection(direction);
                    continue;
                }
                ChunkInfo chunkInfo3 = new ChunkInfo(builtChunk2, direction, chunkInfo.propagationLevel + 1);
                chunkInfo3.updateCullingState(chunkInfo.cullingState, direction);
                if (builtChunk2.shouldBuild()) {
                    queue.add(chunkInfo3);
                    renderableChunks.field_45627.setInfo(builtChunk2, chunkInfo3);
                    continue;
                }
                if (!this.method_52832(blockPos, builtChunk2.getOrigin())) continue;
                renderableChunks.field_45627.setInfo(builtChunk2, chunkInfo3);
                renderableChunks.field_45628.computeIfAbsent(ChunkPos.toLong(builtChunk2.getOrigin()), l -> new ArrayList()).add(builtChunk2);
            }
        }
    }

    private boolean method_52832(BlockPos blockPos, BlockPos blockPos2) {
        int i = ChunkSectionPos.getSectionCoord(blockPos.getX());
        int j = ChunkSectionPos.getSectionCoord(blockPos.getZ());
        int k = ChunkSectionPos.getSectionCoord(blockPos2.getX());
        int l = ChunkSectionPos.getSectionCoord(blockPos2.getZ());
        return ChunkFilter.isWithinDistanceExcludingEdge(i, j, this.field_45623.getViewDistance(), k, l);
    }

    @Nullable
    private ChunkBuilder.BuiltChunk method_52831(BlockPos blockPos, ChunkBuilder.BuiltChunk builtChunk, Direction direction) {
        BlockPos blockPos2 = builtChunk.getNeighborPosition(direction);
        if (!this.method_52832(blockPos, blockPos2)) {
            return null;
        }
        if (MathHelper.abs(blockPos.getY() - blockPos2.getY()) > this.field_45623.getViewDistance() * 16) {
            return null;
        }
        return this.field_45623.getRenderedChunk(blockPos2);
    }

    @Nullable
    @Debug
    protected ChunkInfo method_52837(ChunkBuilder.BuiltChunk builtChunk) {
        return this.field_45624.get().storage.field_45627.getInfo(builtChunk);
    }

    @Environment(value=EnvType.CLIENT)
    record class_8681(RenderableChunks storage, class_8680 events) {
        public class_8681(int i) {
            this(new RenderableChunks(i), new class_8680());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class RenderableChunks {
        public final ChunkInfoList field_45627;
        public final LinkedHashSet<ChunkInfo> chunks;
        public final Long2ObjectMap<List<ChunkBuilder.BuiltChunk>> field_45628;

        public RenderableChunks(int chunkCount) {
            this.field_45627 = new ChunkInfoList(chunkCount);
            this.chunks = new LinkedHashSet(chunkCount);
            this.field_45628 = new Long2ObjectOpenHashMap<List<ChunkBuilder.BuiltChunk>>();
        }
    }

    @Environment(value=EnvType.CLIENT)
    @Debug
    protected static class ChunkInfo {
        @Debug
        protected final ChunkBuilder.BuiltChunk chunk;
        private byte direction;
        byte cullingState;
        @Debug
        protected final int propagationLevel;

        ChunkInfo(ChunkBuilder.BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
            this.chunk = chunk;
            if (direction != null) {
                this.addDirection(direction);
            }
            this.propagationLevel = propagationLevel;
        }

        void updateCullingState(byte parentCullingState, Direction from) {
            this.cullingState = (byte)(this.cullingState | (parentCullingState | 1 << from.ordinal()));
        }

        boolean canCull(Direction from) {
            return (this.cullingState & 1 << from.ordinal()) > 0;
        }

        void addDirection(Direction direction) {
            this.direction = (byte)(this.direction | (this.direction | 1 << direction.ordinal()));
        }

        @Debug
        protected boolean hasDirection(int ordinal) {
            return (this.direction & 1 << ordinal) > 0;
        }

        boolean hasAnyDirection() {
            return this.direction != 0;
        }

        public int hashCode() {
            return this.chunk.getOrigin().hashCode();
        }

        public boolean equals(Object o) {
            if (!(o instanceof ChunkInfo)) {
                return false;
            }
            ChunkInfo chunkInfo = (ChunkInfo)o;
            return this.chunk.getOrigin().equals(chunkInfo.chunk.getOrigin());
        }
    }

    @Environment(value=EnvType.CLIENT)
    record class_8680(LongSet chunksWhichReceivedNeighbors, BlockingQueue<ChunkBuilder.BuiltChunk> sectionsToPropagateFrom) {
        public class_8680() {
            this(new LongOpenHashSet(), new LinkedBlockingQueue<ChunkBuilder.BuiltChunk>());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChunkInfoList {
        private final ChunkInfo[] current;

        ChunkInfoList(int size) {
            this.current = new ChunkInfo[size];
        }

        public void setInfo(ChunkBuilder.BuiltChunk chunk, ChunkInfo info) {
            this.current[chunk.index] = info;
        }

        @Nullable
        public ChunkInfo getInfo(ChunkBuilder.BuiltChunk chunk) {
            int i = chunk.index;
            if (i < 0 || i >= this.current.length) {
                return null;
            }
            return this.current[i];
        }
    }
}

