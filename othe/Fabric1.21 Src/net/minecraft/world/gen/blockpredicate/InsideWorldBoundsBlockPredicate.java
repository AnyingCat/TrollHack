/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

public class InsideWorldBoundsBlockPredicate
implements BlockPredicate {
    public static final MapCodec<InsideWorldBoundsBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3i.createOffsetCodec(16).optionalFieldOf("offset", BlockPos.ORIGIN).forGetter(predicate -> predicate.offset)).apply((Applicative<InsideWorldBoundsBlockPredicate, ?>)instance, InsideWorldBoundsBlockPredicate::new));
    private final Vec3i offset;

    public InsideWorldBoundsBlockPredicate(Vec3i offset) {
        this.offset = offset;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return !structureWorldAccess.isOutOfHeightLimit(blockPos.add(this.offset));
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.INSIDE_WORLD_BOUNDS;
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

