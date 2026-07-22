/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.world.gen.structure;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.structure.DesertTempleGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.Util;
import net.minecraft.util.collection.SortedArraySet;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.BasicTempleStructure;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

public class DesertPyramidStructure
extends BasicTempleStructure {
    public static final MapCodec<DesertPyramidStructure> CODEC = DesertPyramidStructure.createCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.Config config) {
        super(DesertTempleGenerator::new, 21, 21, config);
    }

    @Override
    public void postPlace(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox box, ChunkPos chunkPos, StructurePiecesList pieces) {
        SortedArraySet set = SortedArraySet.create(Vec3i::compareTo);
        for (StructurePiece structurePiece : pieces.pieces()) {
            if (!(structurePiece instanceof DesertTempleGenerator)) continue;
            DesertTempleGenerator desertTempleGenerator = (DesertTempleGenerator)structurePiece;
            set.addAll(desertTempleGenerator.getPotentialSuspiciousSandPositions());
            DesertPyramidStructure.placeSuspiciousSand(box, world, desertTempleGenerator.getBasementMarkerPos());
        }
        ObjectArrayList objectArrayList = new ObjectArrayList(set.stream().toList());
        Random random2 = Random.create(world.getSeed()).nextSplitter().split(pieces.getBoundingBox().getCenter());
        Util.shuffle(objectArrayList, random2);
        int i = Math.min(set.size(), random2.nextBetweenExclusive(5, 8));
        for (BlockPos blockPos : objectArrayList) {
            if (i > 0) {
                --i;
                DesertPyramidStructure.placeSuspiciousSand(box, world, blockPos);
                continue;
            }
            if (!box.contains(blockPos)) continue;
            world.setBlockState(blockPos, Blocks.SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
    }

    private static void placeSuspiciousSand(BlockBox box, StructureWorldAccess world, BlockPos pos) {
        if (box.contains(pos)) {
            world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState(), Block.NOTIFY_LISTENERS);
            world.getBlockEntity(pos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent(blockEntity -> blockEntity.setLootTable(LootTables.DESERT_PYRAMID_ARCHAEOLOGY, pos.asLong()));
        }
    }

    @Override
    public StructureType<?> getType() {
        return StructureType.DESERT_PYRAMID;
    }
}

