/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndPlatformFeature
extends Feature<DefaultFeatureConfig> {
    public EndPlatformFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        EndPlatformFeature.generate(context.getWorld(), context.getOrigin(), false);
        return true;
    }

    public static void generate(ServerWorldAccess world, BlockPos pos, boolean breakBlocks) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -1; k < 3; ++k) {
                    Block block;
                    BlockPos.Mutable blockPos = mutable.set(pos).move(j, k, i);
                    Block block2 = block = k == -1 ? Blocks.OBSIDIAN : Blocks.AIR;
                    if (world.getBlockState(blockPos).isOf(block)) continue;
                    if (breakBlocks) {
                        world.breakBlock(blockPos, true, null);
                    }
                    world.setBlockState(blockPos, block.getDefaultState(), Block.NOTIFY_ALL);
                }
            }
        }
    }
}

