/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

class NotBlockPredicate
implements BlockPredicate {
    public static final MapCodec<NotBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)BlockPredicate.BASE_CODEC.fieldOf("predicate")).forGetter(predicate -> predicate.predicate)).apply((Applicative<NotBlockPredicate, ?>)instance, NotBlockPredicate::new));
    private final BlockPredicate predicate;

    public NotBlockPredicate(BlockPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return !this.predicate.test(structureWorldAccess, blockPos);
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.NOT;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

