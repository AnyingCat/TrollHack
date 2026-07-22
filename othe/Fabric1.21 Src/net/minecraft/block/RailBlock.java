/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RailBlock
extends AbstractRailBlock {
    public static final MapCodec<RailBlock> CODEC = RailBlock.createCodec(RailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = Properties.RAIL_SHAPE;

    public MapCodec<RailBlock> getCodec() {
        return CODEC;
    }

    public RailBlock(AbstractBlock.Settings settings) {
        super(false, settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SHAPE, RailShape.NORTH_SOUTH)).with(WATERLOGGED, false));
    }

    @Override
    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
        if (neighbor.getDefaultState().emitsRedstonePower() && new RailPlacementHelper(world, pos, state).getNeighborCount() == 3) {
            this.updateBlockState(world, pos, state, false);
        }
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        RailShape railShape = state.get(SHAPE);
        return (BlockState)state.with(SHAPE, switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case EAST_WEST: {
                        yield RailShape.EAST_WEST;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_WEST;
            }
            case BlockRotation.COUNTERCLOCKWISE_90 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.NORTH_WEST;
            }
            case BlockRotation.CLOCKWISE_90 -> {
                switch (railShape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_EAST;
            }
            default -> railShape;
        });
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        RailShape railShape = state.get(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT: {
                switch (railShape) {
                    case ASCENDING_NORTH: {
                        return (BlockState)state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_SOUTH: {
                        return (BlockState)state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                }
                break;
            }
            case FRONT_BACK: {
                switch (railShape) {
                    case ASCENDING_EAST: {
                        return (BlockState)state.with(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_WEST: {
                        return (BlockState)state.with(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case SOUTH_EAST: {
                        return (BlockState)state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case SOUTH_WEST: {
                        return (BlockState)state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_WEST: {
                        return (BlockState)state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_EAST: {
                        return (BlockState)state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                }
                break;
            }
        }
        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }
}

