/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TallBlockItem
extends BlockItem {
    public TallBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        BlockPos blockPos;
        World world = context.getWorld();
        BlockState blockState = world.isWater(blockPos = context.getBlockPos().up()) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
        world.setBlockState(blockPos, blockState, Block.NOTIFY_ALL_AND_REDRAW | Block.FORCE_STATE);
        return super.place(context, state);
    }
}

