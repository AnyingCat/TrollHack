/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChorusPlantBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChorusFlowerBlock
extends Block {
    public static final MapCodec<ChorusFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)Registries.BLOCK.getCodec().fieldOf("plant")).forGetter(block -> block.plantBlock), ChorusFlowerBlock.createSettingsCodec()).apply((Applicative<ChorusFlowerBlock, ?>)instance, ChorusFlowerBlock::new));
    public static final int MAX_AGE = 5;
    public static final IntProperty AGE = Properties.AGE_5;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
    private final Block plantBlock;

    public MapCodec<ChorusFlowerBlock> getCodec() {
        return CODEC;
    }

    public ChorusFlowerBlock(Block plantBlock, AbstractBlock.Settings settings) {
        super(settings);
        this.plantBlock = plantBlock;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(AGE) < 5;
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int j;
        BlockPos blockPos = pos.up();
        if (!world.isAir(blockPos) || blockPos.getY() >= world.getTopY()) {
            return;
        }
        int i = state.get(AGE);
        if (i >= 5) {
            return;
        }
        boolean bl = false;
        boolean bl2 = false;
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(Blocks.END_STONE)) {
            bl = true;
        } else if (blockState.isOf(this.plantBlock)) {
            j = 1;
            for (int k = 0; k < 4; ++k) {
                BlockState blockState2 = world.getBlockState(pos.down(j + 1));
                if (blockState2.isOf(this.plantBlock)) {
                    ++j;
                    continue;
                }
                if (!blockState2.isOf(Blocks.END_STONE)) break;
                bl2 = true;
                break;
            }
            if (j < 2 || j <= random.nextInt(bl2 ? 5 : 4)) {
                bl = true;
            }
        } else if (blockState.isAir()) {
            bl = true;
        }
        if (bl && ChorusFlowerBlock.isSurroundedByAir(world, blockPos, null) && world.isAir(pos.up(2))) {
            world.setBlockState(pos, ChorusPlantBlock.withConnectionProperties(world, pos, this.plantBlock.getDefaultState()), Block.NOTIFY_LISTENERS);
            this.grow(world, blockPos, i);
        } else if (i < 4) {
            j = random.nextInt(4);
            if (bl2) {
                ++j;
            }
            boolean bl3 = false;
            for (int l = 0; l < j; ++l) {
                Direction direction = Direction.Type.HORIZONTAL.random(random);
                BlockPos blockPos2 = pos.offset(direction);
                if (!world.isAir(blockPos2) || !world.isAir(blockPos2.down()) || !ChorusFlowerBlock.isSurroundedByAir(world, blockPos2, direction.getOpposite())) continue;
                this.grow(world, blockPos2, i + 1);
                bl3 = true;
            }
            if (bl3) {
                world.setBlockState(pos, ChorusPlantBlock.withConnectionProperties(world, pos, this.plantBlock.getDefaultState()), Block.NOTIFY_LISTENERS);
            } else {
                this.die(world, pos);
            }
        } else {
            this.die(world, pos);
        }
    }

    private void grow(World world, BlockPos pos, int age) {
        world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, age), Block.NOTIFY_LISTENERS);
        world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_GROWS, pos, 0);
    }

    private void die(World world, BlockPos pos) {
        world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
        world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_DIES, pos, 0);
    }

    private static boolean isSurroundedByAir(WorldView world, BlockPos pos, @Nullable Direction exceptDirection) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (direction == exceptDirection || world.isAir(pos.offset(direction))) continue;
            return false;
        }
        return true;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction != Direction.UP && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.down());
        if (blockState.isOf(this.plantBlock) || blockState.isOf(Blocks.END_STONE)) {
            return true;
        }
        if (!blockState.isAir()) {
            return false;
        }
        boolean bl = false;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockState blockState2 = world.getBlockState(pos.offset(direction));
            if (blockState2.isOf(this.plantBlock)) {
                if (bl) {
                    return false;
                }
                bl = true;
                continue;
            }
            if (blockState2.isAir()) continue;
            return false;
        }
        return bl;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    public static void generate(WorldAccess world, BlockPos pos, Random random, int size) {
        world.setBlockState(pos, ChorusPlantBlock.withConnectionProperties(world, pos, Blocks.CHORUS_PLANT.getDefaultState()), Block.NOTIFY_LISTENERS);
        ChorusFlowerBlock.generate(world, pos, random, pos, size, 0);
    }

    private static void generate(WorldAccess world, BlockPos pos, Random random, BlockPos rootPos, int size, int layer) {
        Block block = Blocks.CHORUS_PLANT;
        int i = random.nextInt(4) + 1;
        if (layer == 0) {
            ++i;
        }
        for (int j = 0; j < i; ++j) {
            BlockPos blockPos = pos.up(j + 1);
            if (!ChorusFlowerBlock.isSurroundedByAir(world, blockPos, null)) {
                return;
            }
            world.setBlockState(blockPos, ChorusPlantBlock.withConnectionProperties(world, blockPos, block.getDefaultState()), Block.NOTIFY_LISTENERS);
            world.setBlockState(blockPos.down(), ChorusPlantBlock.withConnectionProperties(world, blockPos.down(), block.getDefaultState()), Block.NOTIFY_LISTENERS);
        }
        boolean bl = false;
        if (layer < 4) {
            int k = random.nextInt(4);
            if (layer == 0) {
                ++k;
            }
            for (int l = 0; l < k; ++l) {
                Direction direction = Direction.Type.HORIZONTAL.random(random);
                BlockPos blockPos2 = pos.up(i).offset(direction);
                if (Math.abs(blockPos2.getX() - rootPos.getX()) >= size || Math.abs(blockPos2.getZ() - rootPos.getZ()) >= size || !world.isAir(blockPos2) || !world.isAir(blockPos2.down()) || !ChorusFlowerBlock.isSurroundedByAir(world, blockPos2, direction.getOpposite())) continue;
                bl = true;
                world.setBlockState(blockPos2, ChorusPlantBlock.withConnectionProperties(world, blockPos2, block.getDefaultState()), Block.NOTIFY_LISTENERS);
                world.setBlockState(blockPos2.offset(direction.getOpposite()), ChorusPlantBlock.withConnectionProperties(world, blockPos2.offset(direction.getOpposite()), block.getDefaultState()), Block.NOTIFY_LISTENERS);
                ChorusFlowerBlock.generate(world, blockPos2, random, rootPos, size, layer + 1);
            }
        }
        if (!bl) {
            world.setBlockState(pos.up(i), (BlockState)Blocks.CHORUS_FLOWER.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!world.isClient && projectile.canModifyAt(world, blockPos) && projectile.canBreakBlocks(world)) {
            world.breakBlock(blockPos, true, projectile);
        }
    }
}

