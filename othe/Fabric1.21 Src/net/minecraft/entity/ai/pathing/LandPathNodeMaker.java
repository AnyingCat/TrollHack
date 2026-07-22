/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.pathing;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeMaker;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.TargetPathNode;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.chunk.ChunkCache;
import org.jetbrains.annotations.Nullable;

public class LandPathNodeMaker
extends PathNodeMaker {
    public static final double Y_OFFSET = 0.5;
    private static final double MIN_STEP_HEIGHT = 1.125;
    private final Long2ObjectMap<PathNodeType> nodeTypes = new Long2ObjectOpenHashMap<PathNodeType>();
    private final Object2BooleanMap<Box> collidedBoxes = new Object2BooleanOpenHashMap<Box>();
    private final PathNode[] successors = new PathNode[Direction.Type.HORIZONTAL.getFacingCount()];

    @Override
    public void init(ChunkCache cachedWorld, MobEntity entity) {
        super.init(cachedWorld, entity);
        entity.onStartPathfinding();
    }

    @Override
    public void clear() {
        this.entity.onFinishPathfinding();
        this.nodeTypes.clear();
        this.collidedBoxes.clear();
        super.clear();
    }

    @Override
    public PathNode getStart() {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i = this.entity.getBlockY();
        BlockState blockState = this.context.getBlockState(mutable.set(this.entity.getX(), (double)i, this.entity.getZ()));
        if (this.entity.canWalkOnFluid(blockState.getFluidState())) {
            while (this.entity.canWalkOnFluid(blockState.getFluidState())) {
                blockState = this.context.getBlockState(mutable.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.canSwim() && this.entity.isTouchingWater()) {
            while (blockState.isOf(Blocks.WATER) || blockState.getFluidState() == Fluids.WATER.getStill(false)) {
                blockState = this.context.getBlockState(mutable.set(this.entity.getX(), (double)(++i), this.entity.getZ()));
            }
            --i;
        } else if (this.entity.isOnGround()) {
            i = MathHelper.floor(this.entity.getY() + 0.5);
        } else {
            mutable.set(this.entity.getX(), this.entity.getY() + 1.0, this.entity.getZ());
            while (mutable.getY() > this.context.getWorld().getBottomY()) {
                i = mutable.getY();
                mutable.setY(mutable.getY() - 1);
                BlockState blockState2 = this.context.getBlockState(mutable);
                if (blockState2.isAir() || blockState2.canPathfindThrough(NavigationType.LAND)) continue;
                break;
            }
        }
        BlockPos blockPos = this.entity.getBlockPos();
        if (!this.canPathThrough(mutable.set(blockPos.getX(), i, blockPos.getZ()))) {
            Box box = this.entity.getBoundingBox();
            if (this.canPathThrough(mutable.set(box.minX, (double)i, box.minZ)) || this.canPathThrough(mutable.set(box.minX, (double)i, box.maxZ)) || this.canPathThrough(mutable.set(box.maxX, (double)i, box.minZ)) || this.canPathThrough(mutable.set(box.maxX, (double)i, box.maxZ))) {
                return this.getStart(mutable);
            }
        }
        return this.getStart(new BlockPos(blockPos.getX(), i, blockPos.getZ()));
    }

    protected PathNode getStart(BlockPos pos) {
        PathNode pathNode = this.getNode(pos);
        pathNode.type = this.getNodeType(pathNode.x, pathNode.y, pathNode.z);
        pathNode.penalty = this.entity.getPathfindingPenalty(pathNode.type);
        return pathNode;
    }

    protected boolean canPathThrough(BlockPos pos) {
        PathNodeType pathNodeType = this.getNodeType(pos.getX(), pos.getY(), pos.getZ());
        return pathNodeType != PathNodeType.OPEN && this.entity.getPathfindingPenalty(pathNodeType) >= 0.0f;
    }

    @Override
    public TargetPathNode getNode(double x, double y, double z) {
        return this.createNode(x, y, z);
    }

    @Override
    public int getSuccessors(PathNode[] successors, PathNode node) {
        int i = 0;
        int j = 0;
        PathNodeType pathNodeType = this.getNodeType(node.x, node.y + 1, node.z);
        PathNodeType pathNodeType2 = this.getNodeType(node.x, node.y, node.z);
        if (this.entity.getPathfindingPenalty(pathNodeType) >= 0.0f && pathNodeType2 != PathNodeType.STICKY_HONEY) {
            j = MathHelper.floor(Math.max(1.0f, this.entity.getStepHeight()));
        }
        double d = this.getFeetY(new BlockPos(node.x, node.y, node.z));
        for (Direction direction : Direction.Type.HORIZONTAL) {
            PathNode pathNode;
            this.successors[direction.getHorizontal()] = pathNode = this.getPathNode(node.x + direction.getOffsetX(), node.y, node.z + direction.getOffsetZ(), j, d, direction, pathNodeType2);
            if (!this.isValidAdjacentSuccessor(pathNode, node)) continue;
            successors[i++] = pathNode;
        }
        for (Direction direction : Direction.Type.HORIZONTAL) {
            PathNode pathNode2;
            Direction direction2 = direction.rotateYClockwise();
            if (!this.isValidDiagonalSuccessor(node, this.successors[direction.getHorizontal()], this.successors[direction2.getHorizontal()]) || !this.isValidDiagonalSuccessor(pathNode2 = this.getPathNode(node.x + direction.getOffsetX() + direction2.getOffsetX(), node.y, node.z + direction.getOffsetZ() + direction2.getOffsetZ(), j, d, direction, pathNodeType2))) continue;
            successors[i++] = pathNode2;
        }
        return i;
    }

    protected boolean isValidAdjacentSuccessor(@Nullable PathNode node, PathNode successor) {
        return node != null && !node.visited && (node.penalty >= 0.0f || successor.penalty < 0.0f);
    }

    protected boolean isValidDiagonalSuccessor(PathNode xNode, @Nullable PathNode zNode, @Nullable PathNode xDiagNode) {
        if (xDiagNode == null || zNode == null || xDiagNode.y > xNode.y || zNode.y > xNode.y) {
            return false;
        }
        if (zNode.type == PathNodeType.WALKABLE_DOOR || xDiagNode.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        boolean bl = xDiagNode.type == PathNodeType.FENCE && zNode.type == PathNodeType.FENCE && (double)this.entity.getWidth() < 0.5;
        return (xDiagNode.y < xNode.y || xDiagNode.penalty >= 0.0f || bl) && (zNode.y < xNode.y || zNode.penalty >= 0.0f || bl);
    }

    protected boolean isValidDiagonalSuccessor(@Nullable PathNode node) {
        if (node == null || node.visited) {
            return false;
        }
        if (node.type == PathNodeType.WALKABLE_DOOR) {
            return false;
        }
        return node.penalty >= 0.0f;
    }

    private static boolean isBlocked(PathNodeType nodeType) {
        return nodeType == PathNodeType.FENCE || nodeType == PathNodeType.DOOR_WOOD_CLOSED || nodeType == PathNodeType.DOOR_IRON_CLOSED;
    }

    private boolean isBlocked(PathNode node) {
        Box box = this.entity.getBoundingBox();
        Vec3d vec3d = new Vec3d((double)node.x - this.entity.getX() + box.getLengthX() / 2.0, (double)node.y - this.entity.getY() + box.getLengthY() / 2.0, (double)node.z - this.entity.getZ() + box.getLengthZ() / 2.0);
        int i = MathHelper.ceil(vec3d.length() / box.getAverageSideLength());
        vec3d = vec3d.multiply(1.0f / (float)i);
        for (int j = 1; j <= i; ++j) {
            if (!this.checkBoxCollision(box = box.offset(vec3d))) continue;
            return false;
        }
        return true;
    }

    protected double getFeetY(BlockPos pos) {
        CollisionView blockView = this.context.getWorld();
        if ((this.canSwim() || this.isAmphibious()) && blockView.getFluidState(pos).isIn(FluidTags.WATER)) {
            return (double)pos.getY() + 0.5;
        }
        return LandPathNodeMaker.getFeetY(blockView, pos);
    }

    public static double getFeetY(BlockView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        VoxelShape voxelShape = world.getBlockState(blockPos).getCollisionShape(world, blockPos);
        return (double)blockPos.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.getMax(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected PathNode getPathNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType) {
        PathNode pathNode = null;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        double d = this.getFeetY(mutable.set(x, y, z));
        if (d - prevFeetY > this.getStepHeight()) {
            return null;
        }
        PathNodeType pathNodeType = this.getNodeType(x, y, z);
        float f = this.entity.getPathfindingPenalty(pathNodeType);
        if (f >= 0.0f) {
            pathNode = this.getNodeWith(x, y, z, pathNodeType, f);
        }
        if (LandPathNodeMaker.isBlocked(nodeType) && pathNode != null && pathNode.penalty >= 0.0f && !this.isBlocked(pathNode)) {
            pathNode = null;
        }
        if (pathNodeType == PathNodeType.WALKABLE || this.isAmphibious() && pathNodeType == PathNodeType.WATER) {
            return pathNode;
        }
        if ((pathNode == null || pathNode.penalty < 0.0f) && maxYStep > 0 && (pathNodeType != PathNodeType.FENCE || this.canWalkOverFences()) && pathNodeType != PathNodeType.UNPASSABLE_RAIL && pathNodeType != PathNodeType.TRAPDOOR && pathNodeType != PathNodeType.POWDER_SNOW) {
            pathNode = this.getJumpOnTopNode(x, y, z, maxYStep, prevFeetY, direction, nodeType, mutable);
        } else if (!this.isAmphibious() && pathNodeType == PathNodeType.WATER && !this.canSwim()) {
            pathNode = this.getNonWaterNodeBelow(x, y, z, pathNode);
        } else if (pathNodeType == PathNodeType.OPEN) {
            pathNode = this.getOpenNode(x, y, z);
        } else if (LandPathNodeMaker.isBlocked(pathNodeType) && pathNode == null) {
            pathNode = this.getNodeWith(x, y, z, pathNodeType);
        }
        return pathNode;
    }

    private double getStepHeight() {
        return Math.max(1.125, (double)this.entity.getStepHeight());
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type, float penalty) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = type;
        pathNode.penalty = Math.max(pathNode.penalty, penalty);
        return pathNode;
    }

    private PathNode getBlockedNode(int x, int y, int z) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.type = PathNodeType.BLOCKED;
        pathNode.penalty = -1.0f;
        return pathNode;
    }

    private PathNode getNodeWith(int x, int y, int z, PathNodeType type) {
        PathNode pathNode = this.getNode(x, y, z);
        pathNode.visited = true;
        pathNode.type = type;
        pathNode.penalty = type.getDefaultPenalty();
        return pathNode;
    }

    @Nullable
    private PathNode getJumpOnTopNode(int x, int y, int z, int maxYStep, double prevFeetY, Direction direction, PathNodeType nodeType, BlockPos.Mutable mutablePos) {
        PathNode pathNode = this.getPathNode(x, y + 1, z, maxYStep - 1, prevFeetY, direction, nodeType);
        if (pathNode == null) {
            return null;
        }
        if (this.entity.getWidth() >= 1.0f) {
            return pathNode;
        }
        if (pathNode.type != PathNodeType.OPEN && pathNode.type != PathNodeType.WALKABLE) {
            return pathNode;
        }
        double d = (double)(x - direction.getOffsetX()) + 0.5;
        double e = (double)(z - direction.getOffsetZ()) + 0.5;
        double f = (double)this.entity.getWidth() / 2.0;
        Box box = new Box(d - f, this.getFeetY(mutablePos.set(d, (double)(y + 1), e)) + 0.001, e - f, d + f, (double)this.entity.getHeight() + this.getFeetY(mutablePos.set((double)pathNode.x, (double)pathNode.y, (double)pathNode.z)) - 0.002, e + f);
        return this.checkBoxCollision(box) ? null : pathNode;
    }

    @Nullable
    private PathNode getNonWaterNodeBelow(int x, int y, int z, @Nullable PathNode node) {
        --y;
        while (y > this.entity.getWorld().getBottomY()) {
            PathNodeType pathNodeType = this.getNodeType(x, y, z);
            if (pathNodeType != PathNodeType.WATER) {
                return node;
            }
            node = this.getNodeWith(x, y, z, pathNodeType, this.entity.getPathfindingPenalty(pathNodeType));
            --y;
        }
        return node;
    }

    private PathNode getOpenNode(int x, int y, int z) {
        for (int i = y - 1; i >= this.entity.getWorld().getBottomY(); --i) {
            if (y - i > this.entity.getSafeFallDistance()) {
                return this.getBlockedNode(x, i, z);
            }
            PathNodeType pathNodeType = this.getNodeType(x, i, z);
            float f = this.entity.getPathfindingPenalty(pathNodeType);
            if (pathNodeType == PathNodeType.OPEN) continue;
            if (f >= 0.0f) {
                return this.getNodeWith(x, i, z, pathNodeType, f);
            }
            return this.getBlockedNode(x, i, z);
        }
        return this.getBlockedNode(x, y, z);
    }

    private boolean checkBoxCollision(Box box) {
        return this.collidedBoxes.computeIfAbsent(box, box2 -> !this.context.getWorld().isSpaceEmpty(this.entity, box));
    }

    protected PathNodeType getNodeType(int x, int y, int z) {
        return this.nodeTypes.computeIfAbsent(BlockPos.asLong(x, y, z), l -> this.getNodeType(this.context, x, y, z, this.entity));
    }

    @Override
    public PathNodeType getNodeType(PathContext context, int x, int y, int z, MobEntity mob) {
        Set<PathNodeType> set = this.getCollidingNodeTypes(context, x, y, z);
        if (set.contains((Object)PathNodeType.FENCE)) {
            return PathNodeType.FENCE;
        }
        if (set.contains((Object)PathNodeType.UNPASSABLE_RAIL)) {
            return PathNodeType.UNPASSABLE_RAIL;
        }
        PathNodeType pathNodeType = PathNodeType.BLOCKED;
        for (PathNodeType pathNodeType2 : set) {
            if (mob.getPathfindingPenalty(pathNodeType2) < 0.0f) {
                return pathNodeType2;
            }
            if (!(mob.getPathfindingPenalty(pathNodeType2) >= mob.getPathfindingPenalty(pathNodeType))) continue;
            pathNodeType = pathNodeType2;
        }
        if (this.entityBlockXSize <= 1 && pathNodeType != PathNodeType.OPEN && mob.getPathfindingPenalty(pathNodeType) == 0.0f && this.getDefaultNodeType(context, x, y, z) == PathNodeType.OPEN) {
            return PathNodeType.OPEN;
        }
        return pathNodeType;
    }

    public Set<PathNodeType> getCollidingNodeTypes(PathContext context, int x, int y, int z) {
        EnumSet<PathNodeType> enumSet = EnumSet.noneOf(PathNodeType.class);
        for (int i = 0; i < this.entityBlockXSize; ++i) {
            for (int j = 0; j < this.entityBlockYSize; ++j) {
                for (int k = 0; k < this.entityBlockZSize; ++k) {
                    int l = i + x;
                    int m = j + y;
                    int n = k + z;
                    PathNodeType pathNodeType = this.getDefaultNodeType(context, l, m, n);
                    BlockPos blockPos = this.entity.getBlockPos();
                    boolean bl = this.canEnterOpenDoors();
                    if (pathNodeType == PathNodeType.DOOR_WOOD_CLOSED && this.canOpenDoors() && bl) {
                        pathNodeType = PathNodeType.WALKABLE_DOOR;
                    }
                    if (pathNodeType == PathNodeType.DOOR_OPEN && !bl) {
                        pathNodeType = PathNodeType.BLOCKED;
                    }
                    if (pathNodeType == PathNodeType.RAIL && this.getDefaultNodeType(context, blockPos.getX(), blockPos.getY(), blockPos.getZ()) != PathNodeType.RAIL && this.getDefaultNodeType(context, blockPos.getX(), blockPos.getY() - 1, blockPos.getZ()) != PathNodeType.RAIL) {
                        pathNodeType = PathNodeType.UNPASSABLE_RAIL;
                    }
                    enumSet.add(pathNodeType);
                }
            }
        }
        return enumSet;
    }

    @Override
    public PathNodeType getDefaultNodeType(PathContext context, int x, int y, int z) {
        return LandPathNodeMaker.getLandNodeType(context, new BlockPos.Mutable(x, y, z));
    }

    public static PathNodeType getLandNodeType(MobEntity entity, BlockPos pos) {
        return LandPathNodeMaker.getLandNodeType(new PathContext(entity.getWorld(), entity), pos.mutableCopy());
    }

    public static PathNodeType getLandNodeType(PathContext context, BlockPos.Mutable pos) {
        int k;
        int j;
        int i = pos.getX();
        PathNodeType pathNodeType = context.getNodeType(i, j = pos.getY(), k = pos.getZ());
        if (pathNodeType != PathNodeType.OPEN || j < context.getWorld().getBottomY() + 1) {
            return pathNodeType;
        }
        return switch (context.getNodeType(i, j - 1, k)) {
            case PathNodeType.OPEN, PathNodeType.WATER, PathNodeType.LAVA, PathNodeType.WALKABLE -> PathNodeType.OPEN;
            case PathNodeType.DAMAGE_FIRE -> PathNodeType.DAMAGE_FIRE;
            case PathNodeType.DAMAGE_OTHER -> PathNodeType.DAMAGE_OTHER;
            case PathNodeType.STICKY_HONEY -> PathNodeType.STICKY_HONEY;
            case PathNodeType.POWDER_SNOW -> PathNodeType.DANGER_POWDER_SNOW;
            case PathNodeType.DAMAGE_CAUTIOUS -> PathNodeType.DAMAGE_CAUTIOUS;
            case PathNodeType.TRAPDOOR -> PathNodeType.DANGER_TRAPDOOR;
            default -> LandPathNodeMaker.getNodeTypeFromNeighbors(context, i, j, k, PathNodeType.WALKABLE);
        };
    }

    public static PathNodeType getNodeTypeFromNeighbors(PathContext context, int x, int y, int z, PathNodeType fallback) {
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (i == 0 && k == 0) continue;
                    PathNodeType pathNodeType = context.getNodeType(x + i, y + j, z + k);
                    if (pathNodeType == PathNodeType.DAMAGE_OTHER) {
                        return PathNodeType.DANGER_OTHER;
                    }
                    if (pathNodeType == PathNodeType.DAMAGE_FIRE || pathNodeType == PathNodeType.LAVA) {
                        return PathNodeType.DANGER_FIRE;
                    }
                    if (pathNodeType == PathNodeType.WATER) {
                        return PathNodeType.WATER_BORDER;
                    }
                    if (pathNodeType != PathNodeType.DAMAGE_CAUTIOUS) continue;
                    return PathNodeType.DAMAGE_CAUTIOUS;
                }
            }
        }
        return fallback;
    }

    protected static PathNodeType getCommonNodeType(BlockView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (blockState.isAir()) {
            return PathNodeType.OPEN;
        }
        if (blockState.isIn(BlockTags.TRAPDOORS) || blockState.isOf(Blocks.LILY_PAD) || blockState.isOf(Blocks.BIG_DRIPLEAF)) {
            return PathNodeType.TRAPDOOR;
        }
        if (blockState.isOf(Blocks.POWDER_SNOW)) {
            return PathNodeType.POWDER_SNOW;
        }
        if (blockState.isOf(Blocks.CACTUS) || blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
        }
        if (blockState.isOf(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
        }
        if (blockState.isOf(Blocks.COCOA)) {
            return PathNodeType.COCOA;
        }
        if (blockState.isOf(Blocks.WITHER_ROSE) || blockState.isOf(Blocks.POINTED_DRIPSTONE)) {
            return PathNodeType.DAMAGE_CAUTIOUS;
        }
        FluidState fluidState = blockState.getFluidState();
        if (fluidState.isIn(FluidTags.LAVA)) {
            return PathNodeType.LAVA;
        }
        if (LandPathNodeMaker.isFireDamaging(blockState)) {
            return PathNodeType.DAMAGE_FIRE;
        }
        if (block instanceof DoorBlock) {
            DoorBlock doorBlock = (DoorBlock)block;
            if (blockState.get(DoorBlock.OPEN).booleanValue()) {
                return PathNodeType.DOOR_OPEN;
            }
            return doorBlock.getBlockSetType().canOpenByHand() ? PathNodeType.DOOR_WOOD_CLOSED : PathNodeType.DOOR_IRON_CLOSED;
        }
        if (block instanceof AbstractRailBlock) {
            return PathNodeType.RAIL;
        }
        if (block instanceof LeavesBlock) {
            return PathNodeType.LEAVES;
        }
        if (blockState.isIn(BlockTags.FENCES) || blockState.isIn(BlockTags.WALLS) || block instanceof FenceGateBlock && !blockState.get(FenceGateBlock.OPEN).booleanValue()) {
            return PathNodeType.FENCE;
        }
        if (!blockState.canPathfindThrough(NavigationType.LAND)) {
            return PathNodeType.BLOCKED;
        }
        if (fluidState.isIn(FluidTags.WATER)) {
            return PathNodeType.WATER;
        }
        return PathNodeType.OPEN;
    }
}

