/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.blockpredicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.gen.blockpredicate.BlockPredicateType;
import net.minecraft.world.gen.blockpredicate.OffsetPredicate;

class ReplaceableBlockPredicate
extends OffsetPredicate {
    public static final MapCodec<ReplaceableBlockPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> ReplaceableBlockPredicate.registerOffsetField(instance).apply(instance, ReplaceableBlockPredicate::new));

    public ReplaceableBlockPredicate(Vec3i vec3i) {
        super(vec3i);
    }

    @Override
    protected boolean test(BlockState state) {
        return state.isReplaceable();
    }

    @Override
    public BlockPredicateType<?> getType() {
        return BlockPredicateType.REPLACEABLE;
    }
}

