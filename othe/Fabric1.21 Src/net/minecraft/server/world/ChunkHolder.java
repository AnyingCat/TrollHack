/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.OptionalChunk;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

public class ChunkHolder
extends AbstractChunkHolder {
    public static final OptionalChunk<WorldChunk> UNLOADED_WORLD_CHUNK = OptionalChunk.of("Unloaded level chunk");
    private static final CompletableFuture<OptionalChunk<WorldChunk>> UNLOADED_WORLD_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_WORLD_CHUNK);
    private final HeightLimitView world;
    private volatile CompletableFuture<OptionalChunk<WorldChunk>> accessibleFuture = UNLOADED_WORLD_CHUNK_FUTURE;
    private volatile CompletableFuture<OptionalChunk<WorldChunk>> tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
    private volatile CompletableFuture<OptionalChunk<WorldChunk>> entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
    private int lastTickLevel;
    private int level;
    private int completedLevel;
    /**
     * Indicates that {@link #blockUpdatesBySection} contains at least one entry.
     */
    private boolean pendingBlockUpdates;
    /**
     * Contains the packed chunk-local positions that have been marked for update
     * by {@link #markForBlockUpdate}, grouped by their vertical chunk section.
     * <p>
     * Entries for a section are null if the section has no positions marked for update.
     */
    private final ShortSet[] blockUpdatesBySection;
    private final BitSet blockLightUpdateBits = new BitSet();
    private final BitSet skyLightUpdateBits = new BitSet();
    private final LightingProvider lightingProvider;
    private final LevelUpdateListener levelUpdateListener;
    private final PlayersWatchingChunkProvider playersWatchingChunkProvider;
    private boolean accessible;
    private CompletableFuture<?> levelIncreaseFuture = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> postProcessingFuture = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> savingFuture = CompletableFuture.completedFuture(null);

    public ChunkHolder(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, LevelUpdateListener levelUpdateListener, PlayersWatchingChunkProvider playersWatchingChunkProvider) {
        super(pos);
        this.world = world;
        this.lightingProvider = lightingProvider;
        this.levelUpdateListener = levelUpdateListener;
        this.playersWatchingChunkProvider = playersWatchingChunkProvider;
        this.level = this.lastTickLevel = ChunkLevels.INACCESSIBLE + 1;
        this.completedLevel = this.lastTickLevel;
        this.setLevel(level);
        this.blockUpdatesBySection = new ShortSet[world.countVerticalSections()];
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> getTickingFuture() {
        return this.tickingFuture;
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> getEntityTickingFuture() {
        return this.entityTickingFuture;
    }

    public CompletableFuture<OptionalChunk<WorldChunk>> getAccessibleFuture() {
        return this.accessibleFuture;
    }

    @Nullable
    public WorldChunk getWorldChunk() {
        return this.getTickingFuture().getNow(UNLOADED_WORLD_CHUNK).orElse(null);
    }

    @Nullable
    public WorldChunk getPostProcessedChunk() {
        if (!this.postProcessingFuture.isDone()) {
            return null;
        }
        return this.getWorldChunk();
    }

    public CompletableFuture<?> getPostProcessingFuture() {
        return this.postProcessingFuture;
    }

    public void combinePostProcessingFuture(CompletableFuture<?> postProcessingFuture) {
        this.postProcessingFuture = this.postProcessingFuture.isDone() ? postProcessingFuture : this.postProcessingFuture.thenCombine(postProcessingFuture, (object, object2) -> null);
    }

    public CompletableFuture<?> getSavingFuture() {
        return this.savingFuture;
    }

    public boolean isSavable() {
        return this.getRefCount() == 0 && this.savingFuture.isDone();
    }

    private void combineSavingFuture(CompletableFuture<?> savingFuture) {
        this.savingFuture = this.savingFuture.isDone() ? savingFuture : this.savingFuture.thenCombine(savingFuture, (object, thenResult) -> null);
    }

    public void markForBlockUpdate(BlockPos pos) {
        WorldChunk worldChunk = this.getWorldChunk();
        if (worldChunk == null) {
            return;
        }
        int i = this.world.getSectionIndex(pos.getY());
        if (this.blockUpdatesBySection[i] == null) {
            this.pendingBlockUpdates = true;
            this.blockUpdatesBySection[i] = new ShortOpenHashSet();
        }
        this.blockUpdatesBySection[i].add(ChunkSectionPos.packLocal(pos));
    }

    /**
     * @param y chunk section y coordinate
     */
    public void markForLightUpdate(LightType lightType, int y) {
        Chunk chunk = this.getOrNull(ChunkStatus.INITIALIZE_LIGHT);
        if (chunk == null) {
            return;
        }
        chunk.setNeedsSaving(true);
        WorldChunk worldChunk = this.getWorldChunk();
        if (worldChunk == null) {
            return;
        }
        int i = this.lightingProvider.getBottomY();
        int j = this.lightingProvider.getTopY();
        if (y < i || y > j) {
            return;
        }
        int k = y - i;
        if (lightType == LightType.SKY) {
            this.skyLightUpdateBits.set(k);
        } else {
            this.blockLightUpdateBits.set(k);
        }
    }

    public void flushUpdates(WorldChunk chunk) {
        List<ServerPlayerEntity> list;
        if (!this.pendingBlockUpdates && this.skyLightUpdateBits.isEmpty() && this.blockLightUpdateBits.isEmpty()) {
            return;
        }
        World world = chunk.getWorld();
        if (!this.skyLightUpdateBits.isEmpty() || !this.blockLightUpdateBits.isEmpty()) {
            list = this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, true);
            if (!list.isEmpty()) {
                LightUpdateS2CPacket lightUpdateS2CPacket = new LightUpdateS2CPacket(chunk.getPos(), this.lightingProvider, this.skyLightUpdateBits, this.blockLightUpdateBits);
                this.sendPacketToPlayers(list, lightUpdateS2CPacket);
            }
            this.skyLightUpdateBits.clear();
            this.blockLightUpdateBits.clear();
        }
        if (!this.pendingBlockUpdates) {
            return;
        }
        list = this.playersWatchingChunkProvider.getPlayersWatchingChunk(this.pos, false);
        for (int i = 0; i < this.blockUpdatesBySection.length; ++i) {
            ShortSet shortSet = this.blockUpdatesBySection[i];
            if (shortSet == null) continue;
            this.blockUpdatesBySection[i] = null;
            if (list.isEmpty()) continue;
            int j = this.world.sectionIndexToCoord(i);
            ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(chunk.getPos(), j);
            if (shortSet.size() == 1) {
                BlockPos blockPos = chunkSectionPos.unpackBlockPos(shortSet.iterator().nextShort());
                BlockState blockState = world.getBlockState(blockPos);
                this.sendPacketToPlayers(list, new BlockUpdateS2CPacket(blockPos, blockState));
                this.tryUpdateBlockEntityAt(list, world, blockPos, blockState);
                continue;
            }
            ChunkSection chunkSection = chunk.getSection(i);
            ChunkDeltaUpdateS2CPacket chunkDeltaUpdateS2CPacket = new ChunkDeltaUpdateS2CPacket(chunkSectionPos, shortSet, chunkSection);
            this.sendPacketToPlayers(list, chunkDeltaUpdateS2CPacket);
            chunkDeltaUpdateS2CPacket.visitUpdates((pos, state) -> this.tryUpdateBlockEntityAt(list, world, (BlockPos)pos, (BlockState)state));
        }
        this.pendingBlockUpdates = false;
    }

    private void tryUpdateBlockEntityAt(List<ServerPlayerEntity> players, World world, BlockPos pos, BlockState state) {
        if (state.hasBlockEntity()) {
            this.sendBlockEntityUpdatePacket(players, world, pos);
        }
    }

    private void sendBlockEntityUpdatePacket(List<ServerPlayerEntity> players, World world, BlockPos pos) {
        Packet<ClientPlayPacketListener> packet;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null && (packet = blockEntity.toUpdatePacket()) != null) {
            this.sendPacketToPlayers(players, packet);
        }
    }

    private void sendPacketToPlayers(List<ServerPlayerEntity> players, Packet<?> packet) {
        players.forEach(player -> player.networkHandler.sendPacket(packet));
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getCompletedLevel() {
        return this.completedLevel;
    }

    private void setCompletedLevel(int level) {
        this.completedLevel = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private void increaseLevel(ServerChunkLoadingManager chunkLoadingManager, CompletableFuture<OptionalChunk<WorldChunk>> chunkFuture, Executor executor, ChunkLevelType target) {
        this.levelIncreaseFuture.cancel(false);
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.thenRunAsync(() -> chunkLoadingManager.onChunkStatusChange(this.pos, target), executor);
        this.levelIncreaseFuture = completableFuture;
        chunkFuture.thenAccept(optionalChunk -> optionalChunk.ifPresent(chunk -> completableFuture.complete(null)));
    }

    private void decreaseLevel(ServerChunkLoadingManager chunkLoadingManager, ChunkLevelType target) {
        this.levelIncreaseFuture.cancel(false);
        chunkLoadingManager.onChunkStatusChange(this.pos, target);
    }

    /**
     * Updates {@code Futures} indicating the expected loading state of the underlying chunk of this {@code ChunkHolder}.
     * Note that the method merely makes actual loading process possible, but do not perform these actions.
     */
    protected void updateFutures(ServerChunkLoadingManager chunkLoadingManager, Executor executor) {
        ChunkLevelType chunkLevelType = ChunkLevels.getType(this.lastTickLevel);
        ChunkLevelType chunkLevelType2 = ChunkLevels.getType(this.level);
        boolean bl = chunkLevelType.isAfter(ChunkLevelType.FULL);
        boolean bl2 = chunkLevelType2.isAfter(ChunkLevelType.FULL);
        this.accessible |= bl2;
        if (!bl && bl2) {
            this.accessibleFuture = chunkLoadingManager.makeChunkAccessible(this);
            this.increaseLevel(chunkLoadingManager, this.accessibleFuture, executor, ChunkLevelType.FULL);
            this.combineSavingFuture(this.accessibleFuture);
        }
        if (bl && !bl2) {
            this.accessibleFuture.complete(UNLOADED_WORLD_CHUNK);
            this.accessibleFuture = UNLOADED_WORLD_CHUNK_FUTURE;
        }
        boolean bl3 = chunkLevelType.isAfter(ChunkLevelType.BLOCK_TICKING);
        boolean bl4 = chunkLevelType2.isAfter(ChunkLevelType.BLOCK_TICKING);
        if (!bl3 && bl4) {
            this.tickingFuture = chunkLoadingManager.makeChunkTickable(this);
            this.increaseLevel(chunkLoadingManager, this.tickingFuture, executor, ChunkLevelType.BLOCK_TICKING);
            this.combineSavingFuture(this.tickingFuture);
        }
        if (bl3 && !bl4) {
            this.tickingFuture.complete(UNLOADED_WORLD_CHUNK);
            this.tickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
        }
        boolean bl5 = chunkLevelType.isAfter(ChunkLevelType.ENTITY_TICKING);
        boolean bl6 = chunkLevelType2.isAfter(ChunkLevelType.ENTITY_TICKING);
        if (!bl5 && bl6) {
            if (this.entityTickingFuture != UNLOADED_WORLD_CHUNK_FUTURE) {
                throw Util.throwOrPause(new IllegalStateException());
            }
            this.entityTickingFuture = chunkLoadingManager.makeChunkEntitiesTickable(this);
            this.increaseLevel(chunkLoadingManager, this.entityTickingFuture, executor, ChunkLevelType.ENTITY_TICKING);
            this.combineSavingFuture(this.entityTickingFuture);
        }
        if (bl5 && !bl6) {
            this.entityTickingFuture.complete(UNLOADED_WORLD_CHUNK);
            this.entityTickingFuture = UNLOADED_WORLD_CHUNK_FUTURE;
        }
        if (!chunkLevelType2.isAfter(chunkLevelType)) {
            this.decreaseLevel(chunkLoadingManager, chunkLevelType2);
        }
        this.levelUpdateListener.updateLevel(this.pos, this::getCompletedLevel, this.level, this::setCompletedLevel);
        this.lastTickLevel = this.level;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public void updateAccessibleStatus() {
        this.accessible = ChunkLevels.getType(this.level).isAfter(ChunkLevelType.FULL);
    }

    @FunctionalInterface
    public static interface LevelUpdateListener {
        public void updateLevel(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public static interface PlayersWatchingChunkProvider {
        public List<ServerPlayerEntity> getPlayersWatchingChunk(ChunkPos var1, boolean var2);
    }
}

