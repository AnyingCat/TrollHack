/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;

abstract class CombinedBlockPredicate
implements BlockPredicate {
    protected final List<BlockPredicate> predicates;

    protected CombinedBlockPredicate(List<BlockPredicate> predicates) {
        this.predicates = predicates;
    }

    public static <T extends CombinedBlockPredicate> MapCodec<T> buildCodec(Function<List<BlockPredicate>, T> combiner) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockPredicate.BASE_CODEC.listOf().fieldOf("predicates")).forGetter(predicate -> predicate.predicates)).apply((Applicative<CombinedBlockPredicate, ?>)instance, combiner));
    }
}

