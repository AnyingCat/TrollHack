/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

public class ForcedChunkState
extends PersistentState {
    public static final String CHUNKS_KEY = "chunks";
    private static final String FORCED_KEY = "Forced";
    private final LongSet chunks;

    public static PersistentState.Type<ForcedChunkState> getPersistentStateType() {
        return new PersistentState.Type<ForcedChunkState>(ForcedChunkState::new, ForcedChunkState::fromNbt, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    }

    private ForcedChunkState(LongSet chunks) {
        this.chunks = chunks;
    }

    public ForcedChunkState() {
        this(new LongOpenHashSet());
    }

    public static ForcedChunkState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return new ForcedChunkState(new LongOpenHashSet(nbt.getLongArray(FORCED_KEY)));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLongArray(FORCED_KEY, this.chunks.toLongArray());
        return nbt;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}

