/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.collect.Comparators;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.lang.invoke.LambdaMetafactory;
import java.util.Comparator;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkSentS2CPacket;
import net.minecraft.network.packet.s2c.play.StartChunkSendS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

public class ChunkDataSender {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final float field_45000 = 0.01f;
    public static final float field_45001 = 64.0f;
    private static final float field_45003 = 9.0f;
    private static final int field_45004 = 10;
    private final LongSet chunks = new LongOpenHashSet();
    private final boolean local;
    private float desiredBatchSize = 9.0f;
    private float pending;
    private int unacknowledgedBatches;
    private int maxUnacknowledgedBatches = 1;

    public ChunkDataSender(boolean local) {
        this.local = local;
    }

    public void add(WorldChunk chunk) {
        this.chunks.add(chunk.getPos().toLong());
    }

    public void unload(ServerPlayerEntity player, ChunkPos pos) {
        if (!this.chunks.remove(pos.toLong()) && player.isAlive()) {
            player.networkHandler.sendPacket(new UnloadChunkS2CPacket(pos));
        }
    }

    public void sendChunkBatches(ServerPlayerEntity player) {
        if (this.unacknowledgedBatches >= this.maxUnacknowledgedBatches) {
            return;
        }
        float f = Math.max(1.0f, this.desiredBatchSize);
        this.pending = Math.min(this.pending + this.desiredBatchSize, f);
        if (this.pending < 1.0f) {
            return;
        }
        if (this.chunks.isEmpty()) {
            return;
        }
        ServerWorld serverWorld = player.getServerWorld();
        ServerChunkLoadingManager serverChunkLoadingManager = serverWorld.getChunkManager().chunkLoadingManager;
        List<WorldChunk> list = this.makeBatch(serverChunkLoadingManager, player.getChunkPos());
        if (list.isEmpty()) {
            return;
        }
        ServerPlayNetworkHandler serverPlayNetworkHandler = player.networkHandler;
        ++this.unacknowledgedBatches;
        serverPlayNetworkHandler.sendPacket(StartChunkSendS2CPacket.INSTANCE);
        for (WorldChunk worldChunk : list) {
            ChunkDataSender.sendChunkData(serverPlayNetworkHandler, serverWorld, worldChunk);
        }
        serverPlayNetworkHandler.sendPacket(new ChunkSentS2CPacket(list.size()));
        this.pending -= (float)list.size();
    }

    private static void sendChunkData(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk) {
        handler.sendPacket(new ChunkDataS2CPacket(chunk, world.getLightingProvider(), null, null));
        ChunkPos chunkPos = chunk.getPos();
        DebugInfoSender.sendChunkWatchingChange(world, chunkPos);
    }

    /*
     * Unable to fully structure code
     */
    private List<WorldChunk> makeBatch(ServerChunkLoadingManager chunkLoadingManager, ChunkPos playerPos) {
        i = MathHelper.floor(this.pending);
        if (this.local) ** GOTO lbl7
        if (this.chunks.size() <= i) {
lbl7:
            // 2 sources

            list = this.chunks.longStream().mapToObj((LongFunction<WorldChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getPostProcessedChunk(long ), (J)Lnet/minecraft/world/chunk/WorldChunk;)((ServerChunkLoadingManager)chunkLoadingManager)).filter((Predicate<WorldChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/chunk/WorldChunk;)Z)()).sorted(Comparator.comparingInt((ToIntFunction<WorldChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, method_52389(net.minecraft.util.math.ChunkPos net.minecraft.world.chunk.WorldChunk ), (Lnet/minecraft/world/chunk/WorldChunk;)I)((ChunkPos)playerPos))).toList();
        } else {
            list = this.chunks.stream().collect(Comparators.least(i, Comparator.comparingInt((ToIntFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)I, getSquaredDistance(long ), (Ljava/lang/Long;)I)((ChunkPos)playerPos)))).stream().mapToLong((ToLongFunction<Long>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)J, longValue(), (Ljava/lang/Long;)J)()).mapToObj((LongFunction<WorldChunk>)LambdaMetafactory.metafactory(null, null, null, (J)Ljava/lang/Object;, getPostProcessedChunk(long ), (J)Lnet/minecraft/world/chunk/WorldChunk;)((ServerChunkLoadingManager)chunkLoadingManager)).filter((Predicate<WorldChunk>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Z, nonNull(java.lang.Object ), (Lnet/minecraft/world/chunk/WorldChunk;)Z)()).toList();
        }
        for (WorldChunk worldChunk : list) {
            this.chunks.remove(worldChunk.getPos().toLong());
        }
        return list;
    }

    public void onAcknowledgeChunks(float desiredBatchSize) {
        --this.unacknowledgedBatches;
        float f = this.desiredBatchSize = Double.isNaN(desiredBatchSize) ? 0.01f : MathHelper.clamp(desiredBatchSize, 0.01f, 64.0f);
        if (this.unacknowledgedBatches == 0) {
            this.pending = 1.0f;
        }
        this.maxUnacknowledgedBatches = 10;
    }

    public boolean isInNextBatch(long chunkPos) {
        return this.chunks.contains(chunkPos);
    }

    private static /* synthetic */ int method_52389(ChunkPos chunkPos, WorldChunk chunk) {
        return chunkPos.getSquaredDistance(chunk.getPos());
    }
}

