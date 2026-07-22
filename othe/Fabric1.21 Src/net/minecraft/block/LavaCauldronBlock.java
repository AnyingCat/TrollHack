/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A cauldron filled with lava.
 */
public class LavaCauldronBlock
extends AbstractCauldronBlock {
    public static final MapCodec<LavaCauldronBlock> CODEC = LavaCauldronBlock.createCodec(LavaCauldronBlock::new);

    public MapCodec<LavaCauldronBlock> getCodec() {
        return CODEC;
    }

    public LavaCauldronBlock(AbstractBlock.Settings settings) {
        super(settings, CauldronBehavior.LAVA_CAULDRON_BEHAVIOR);
    }

    @Override
    protected double getFluidHeight(BlockState state) {
        return 0.9375;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (this.isEntityTouchingFluid(state, pos, entity)) {
            entity.setOnFireFromLava();
        }
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return 3;
    }
}

