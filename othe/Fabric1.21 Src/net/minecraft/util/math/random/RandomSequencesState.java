/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSequence;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.PersistentState;
import org.slf4j.Logger;

public class RandomSequencesState
extends PersistentState {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long seed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap<Identifier, RandomSequence>();

    public static PersistentState.Type<RandomSequencesState> getPersistentStateType(long seed) {
        return new PersistentState.Type<RandomSequencesState>(() -> new RandomSequencesState(seed), (nbt, registryLookup) -> RandomSequencesState.fromNbt(seed, nbt), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public RandomSequencesState(long seed) {
        this.seed = seed;
    }

    public Random getOrCreate(Identifier id) {
        Random random = this.sequences.computeIfAbsent(id, this::createSequence).getSource();
        return new WrappedRandom(random);
    }

    private RandomSequence createSequence(Identifier id) {
        return this.createSequence(id, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        long l = (includeWorldSeed ? this.seed : 0L) ^ (long)salt;
        return new RandomSequence(l, includeSequenceId ? Optional.of(id) : Optional.empty());
    }

    public void forEachSequence(BiConsumer<Identifier, RandomSequence> consumer) {
        this.sequences.forEach(consumer);
    }

    public void setDefaultParameters(int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.salt = salt;
        this.includeWorldSeed = includeWorldSeed;
        this.includeSequenceId = includeSequenceId;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("salt", this.salt);
        nbt.putBoolean("include_world_seed", this.includeWorldSeed);
        nbt.putBoolean("include_sequence_id", this.includeSequenceId);
        NbtCompound nbtCompound = new NbtCompound();
        this.sequences.forEach((id, sequence) -> nbtCompound.put(id.toString(), RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, (RandomSequence)sequence).result().orElseThrow()));
        nbt.put("sequences", nbtCompound);
        return nbt;
    }

    private static boolean getBooleanFromNbtOrFallback(NbtCompound nbt, String key, boolean fallback) {
        if (nbt.contains(key, NbtElement.BYTE_TYPE)) {
            return nbt.getBoolean(key);
        }
        return fallback;
    }

    public static RandomSequencesState fromNbt(long seed, NbtCompound nbt) {
        RandomSequencesState randomSequencesState = new RandomSequencesState(seed);
        randomSequencesState.setDefaultParameters(nbt.getInt("salt"), RandomSequencesState.getBooleanFromNbtOrFallback(nbt, "include_world_seed", true), RandomSequencesState.getBooleanFromNbtOrFallback(nbt, "include_sequence_id", true));
        NbtCompound nbtCompound = nbt.getCompound("sequences");
        Set<String> set = nbtCompound.getKeys();
        for (String string : set) {
            try {
                RandomSequence randomSequence = (RandomSequence)RandomSequence.CODEC.decode(NbtOps.INSTANCE, nbtCompound.get(string)).result().get().getFirst();
                randomSequencesState.sequences.put(Identifier.of(string), randomSequence);
            } catch (Exception exception) {
                LOGGER.error("Failed to load random sequence {}", (Object)string, (Object)exception);
            }
        }
        return randomSequencesState;
    }

    public int resetAll() {
        int i = this.sequences.size();
        this.sequences.clear();
        return i;
    }

    public void reset(Identifier id) {
        this.sequences.put(id, this.createSequence(id));
    }

    public void reset(Identifier id, int salt, boolean includeWorldSeed, boolean includeSequenceId) {
        this.sequences.put(id, this.createSequence(id, salt, includeWorldSeed, includeSequenceId));
    }

    class WrappedRandom
    implements Random {
        private final Random random;

        WrappedRandom(Random random) {
            this.random = random;
        }

        @Override
        public Random split() {
            RandomSequencesState.this.markDirty();
            return this.random.split();
        }

        @Override
        public RandomSplitter nextSplitter() {
            RandomSequencesState.this.markDirty();
            return this.random.nextSplitter();
        }

        @Override
        public void setSeed(long seed) {
            RandomSequencesState.this.markDirty();
            this.random.setSeed(seed);
        }

        @Override
        public int nextInt() {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int bound) {
            RandomSequencesState.this.markDirty();
            return this.random.nextInt(bound);
        }

        @Override
        public long nextLong() {
            RandomSequencesState.this.markDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequencesState.this.markDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequencesState.this.markDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequencesState.this.markDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequencesState.this.markDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof WrappedRandom) {
                WrappedRandom wrappedRandom = (WrappedRandom)o;
                return this.random.equals(wrappedRandom.random);
            }
            return false;
        }
    }
}

