package github.trollhack.utils.world;

import github.trollhack.utils.interfaces.Mc;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class BlockUtil implements Mc {

    public static final BlockPos[] SURROUND_OFFSETS = new BlockPos[]{
            new BlockPos(0, -1, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 1, 0)
    };

    private static final Direction[] HORIZONTAL_DIRS = new Direction[]{
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public static boolean isObsidian(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN;
    }

    public static boolean isBedrock(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    public static boolean isHoleBlock(BlockPos pos) {
        if (mc.world == null) return false;
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK;
    }

    public static boolean isInHole(BlockPos pos) {
        if (mc.world == null) return false;
        BlockPos feet = pos;
        BlockPos head = feet.up();
        if (!isHoleBlock(feet.down())) return false;
        for (Direction dir : HORIZONTAL_DIRS) {
            if (!isHoleBlock(feet.offset(dir))) return false;
            if (!isHoleBlock(head.offset(dir))) return false;
        }
        return true;
    }

    public static boolean isCrystalBase(BlockPos pos) {
        if (mc.world == null) return false;
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock() == Blocks.OBSIDIAN || state.getBlock() == Blocks.BEDROCK;
    }

    public static boolean isAir(BlockPos pos) {
        if (mc.world == null) return false;
        return mc.world.getBlockState(pos).isAir();
    }

    public static boolean canPlaceCrystal(BlockPos base) {
        return canPlaceCrystal(base, false);
    }

    public static boolean canPlaceCrystal(BlockPos base, boolean ignoreExistingCrystals) {
        if (mc.world == null) return false;
        if (!isCrystalBase(base)) return false;
        BlockPos up = base.up();
        if (!mc.world.isAir(up)) return false;
        Box box = new Box(up.getX(), up.getY(), up.getZ(),
                up.getX() + 1.0, up.getY() + 2.0, up.getZ() + 1.0);
        for (Entity entity : mc.world.getOtherEntities(null, box)) {
            if (entity instanceof ExperienceOrbEntity) continue;
            if (entity instanceof ItemEntity) continue;
            if (entity instanceof ArmorStandEntity) continue;
            if (ignoreExistingCrystals && entity instanceof EndCrystalEntity) continue;
            return false;
        }
        return true;
    }

    public static boolean canPlaceCrystalNoEntityCheck(BlockPos base) {
        if (mc.world == null) return false;
        if (!isCrystalBase(base)) return false;
        BlockState upState = mc.world.getBlockState(base.up());
        return upState.isAir() || upState.getBlock() == Blocks.AIR;
    }

    public static boolean hasExistingCrystal(BlockPos base) {
        if (mc.world == null) return false;
        BlockPos up = base.up();
        Box box = new Box(up.getX(), up.getY(), up.getZ(),
                up.getX() + 1.0, up.getY() + 2.0, up.getZ() + 1.0);
        for (Entity entity : mc.world.getOtherEntities(null, box)) {
            if (entity instanceof EndCrystalEntity && !entity.isRemoved()) return true;
        }
        return false;
    }

    public static List<BlockPos> getSphere(BlockPos center, float radius, boolean sphere) {
        List<BlockPos> list = new ArrayList<>();
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        int r = (int) Math.ceil(radius);
        double radiusSq = radius * radius;
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (!sphere && y != 0) continue;
                    if (x * x + y * y + z * z <= radiusSq) {
                        list.add(new BlockPos(cx + x, cy + y, cz + z));
                    }
                }
            }
        }
        return list;
    }

    public static List<BlockPos> getPlacePositions(BlockPos center, float radius) {
        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos pos : getSphere(center, radius, true)) {
            if (canPlaceCrystal(pos)) {
                positions.add(pos);
            }
        }
        return positions;
    }

    public static Direction getPlaceSide(BlockPos pos) {
        if (mc.player == null || mc.world == null) return Direction.UP;
        Vec3d eye = mc.player.getEyePos();
        Direction closest = Direction.UP;
        double minDist = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            if (dir != Direction.UP && mc.world.getBlockState(pos.offset(dir)).isAir()) continue;
            double dist = eye.squaredDistanceTo(new Vec3d(pos.getX() + 0.5 + dir.getOffsetX(),
                    pos.getY() + 0.5 + dir.getOffsetY(),
                    pos.getZ() + 0.5 + dir.getOffsetZ()));
            if (dist < minDist) {
                minDist = dist;
                closest = dir;
            }
        }
        return closest;
    }

    public static BlockHitResult createPlaceHit(BlockPos base, Direction side) {
        Vec3d hitVec = new Vec3d(base.getX() + 0.5 + side.getOffsetX() * 0.5,
                base.getY() + 0.5 + side.getOffsetY() * 0.5,
                base.getZ() + 0.5 + side.getOffsetZ() * 0.5);
        return new BlockHitResult(hitVec, side, base, false);
    }

    public static BlockHitResult createAirPlaceHit(BlockPos base) {
        Vec3d hitVec = new Vec3d(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
        return new BlockHitResult(hitVec, Direction.DOWN, base, true);
    }

    public static boolean rayTraceHit(BlockPos pos) {
        if (mc.player == null || mc.world == null) return false;
        Vec3d eye = mc.player.getEyePos();
        Vec3d target = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        BlockHitResult result = mc.world.raycast(
                new net.minecraft.world.RaycastContext(eye, target,
                        net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                        net.minecraft.world.RaycastContext.FluidHandling.NONE,
                        mc.player));
        return result == null || result.getBlockPos().equals(pos) || result.getType() == net.minecraft.util.hit.HitResult.Type.MISS;
    }

    public static BlockPos fromEntity(net.minecraft.entity.Entity entity) {
        return BlockPos.ofFloored(entity.getX(), entity.getY(), entity.getZ());
    }
}
