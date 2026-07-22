/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;

record UnobstructedBlockPredicate(Vec3i offset) implements BlockPredicate
{
    public static MapCodec<UnobstructedBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(UnobstructedBlockPredicate::offset)).apply((Applicative<UnobstructedBlockPredicate, ?>)instance, UnobstructedBlockPredicate::new));

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.UNOBSTRUCTED;
    }

    @Override
    public boolean test(StructureWorldAccess structureWorldAccess, BlockPos blockPos) {
        return structureWorldAccess.doesNotIntersectEntities(null, VoxelShapes.fullCube().offset(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
    }

    @Override
    public /* synthetic */ boolean test(Object world, Object pos) {
        return this.test((StructureWorldAccess)world, (BlockPos)pos);
    }
}

