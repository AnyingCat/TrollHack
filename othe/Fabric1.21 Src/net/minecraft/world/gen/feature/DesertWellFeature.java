/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class DesertWellFeature
extends Feature<DefaultFeatureConfig> {
    private static final BlockStatePredicate CAN_GENERATE = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState sand = Blocks.SAND.getDefaultState();
    private final BlockState slab = Blocks.SANDSTONE_SLAB.getDefaultState();
    private final BlockState wall = Blocks.SANDSTONE.getDefaultState();
    private final BlockState fluidInside = Blocks.WATER.getDefaultState();

    public DesertWellFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        int j;
        int j2;
        int i;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        blockPos = blockPos.up();
        while (structureWorldAccess.isAir(blockPos) && blockPos.getY() > structureWorldAccess.getBottomY() + 2) {
            blockPos = blockPos.down();
        }
        if (!CAN_GENERATE.test(structureWorldAccess.getBlockState(blockPos))) {
            return false;
        }
        for (i = -2; i <= 2; ++i) {
            for (j2 = -2; j2 <= 2; ++j2) {
                if (!structureWorldAccess.isAir(blockPos.add(i, -1, j2)) || !structureWorldAccess.isAir(blockPos.add(i, -2, j2))) continue;
                return false;
            }
        }
        for (i = -2; i <= 0; ++i) {
            for (j2 = -2; j2 <= 2; ++j2) {
                for (int k = -2; k <= 2; ++k) {
                    structureWorldAccess.setBlockState(blockPos.add(j2, i, k), this.wall, Block.NOTIFY_LISTENERS);
                }
            }
        }
        structureWorldAccess.setBlockState(blockPos, this.fluidInside, Block.NOTIFY_LISTENERS);
        for (Direction direction : Direction.Type.HORIZONTAL) {
            structureWorldAccess.setBlockState(blockPos.offset(direction), this.fluidInside, Block.NOTIFY_LISTENERS);
        }
        BlockPos blockPos2 = blockPos.down();
        structureWorldAccess.setBlockState(blockPos2, this.sand, Block.NOTIFY_LISTENERS);
        for (Direction direction2 : Direction.Type.HORIZONTAL) {
            structureWorldAccess.setBlockState(blockPos2.offset(direction2), this.sand, Block.NOTIFY_LISTENERS);
        }
        for (j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                if (j != -2 && j != 2 && k != -2 && k != 2) continue;
                structureWorldAccess.setBlockState(blockPos.add(j, 1, k), this.wall, Block.NOTIFY_LISTENERS);
            }
        }
        structureWorldAccess.setBlockState(blockPos.add(2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(-2, 1, 0), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(0, 1, 2), this.slab, Block.NOTIFY_LISTENERS);
        structureWorldAccess.setBlockState(blockPos.add(0, 1, -2), this.slab, Block.NOTIFY_LISTENERS);
        for (j = -1; j <= 1; ++j) {
            for (int k = -1; k <= 1; ++k) {
                if (j == 0 && k == 0) {
                    structureWorldAccess.setBlockState(blockPos.add(j, 4, k), this.wall, Block.NOTIFY_LISTENERS);
                    continue;
                }
                structureWorldAccess.setBlockState(blockPos.add(j, 4, k), this.slab, Block.NOTIFY_LISTENERS);
            }
        }
        for (j = 1; j <= 3; ++j) {
            structureWorldAccess.setBlockState(blockPos.add(-1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(-1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(1, j, -1), this.wall, Block.NOTIFY_LISTENERS);
            structureWorldAccess.setBlockState(blockPos.add(1, j, 1), this.wall, Block.NOTIFY_LISTENERS);
        }
        BlockPos blockPos3 = blockPos;
        List<BlockPos> list = List.of(blockPos3, blockPos3.east(), blockPos3.south(), blockPos3.west(), blockPos3.north());
        Random random = context.getRandom();
        DesertWellFeature.generateSuspiciousSand(structureWorldAccess, Util.getRandom(list, random).down(1));
        DesertWellFeature.generateSuspiciousSand(structureWorldAccess, Util.getRandom(list, random).down(2));
        return true;
    }

    private static void generateSuspiciousSand(StructureWorldAccess world, BlockPos pos) {
        world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState(), Block.NOTIFY_ALL);
        world.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent(blockEntity -> blockEntity.setLootTable(LootTables.DESERT_WELL_ARCHAEOLOGY, pos.asLong()));
    }
}

