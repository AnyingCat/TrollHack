/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.util.math.random;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class RandomSequence {
    public static final Codec<RandomSequence> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Xoroshiro128PlusPlusRandom.CODEC.fieldOf("source")).forGetter(sequence -> sequence.source)).apply((Applicative<RandomSequence, ?>)instance, RandomSequence::new));
    private final Xoroshiro128PlusPlusRandom source;

    public RandomSequence(Xoroshiro128PlusPlusRandom source) {
        this.source = source;
    }

    public RandomSequence(long seed, Identifier id) {
        this(RandomSequence.createSource(seed, Optional.of(id)));
    }

    public RandomSequence(long seed, Optional<Identifier> id) {
        this(RandomSequence.createSource(seed, id));
    }

    private static Xoroshiro128PlusPlusRandom createSource(long seed, Optional<Identifier> id) {
        RandomSeed.XoroshiroSeed xoroshiroSeed = RandomSeed.createUnmixedXoroshiroSeed(seed);
        if (id.isPresent()) {
            xoroshiroSeed = xoroshiroSeed.split(RandomSequence.createSeed(id.get()));
        }
        return new Xoroshiro128PlusPlusRandom(xoroshiroSeed.mix());
    }

    public static RandomSeed.XoroshiroSeed createSeed(Identifier id) {
        return RandomSeed.createXoroshiroSeed(id.toString());
    }

    public Random getSource() {
        return this.source;
    }
}

