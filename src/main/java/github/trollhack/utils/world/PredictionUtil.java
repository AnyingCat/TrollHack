package github.trollhack.utils.world;

import github.trollhack.utils.interfaces.Mc;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PredictionUtil implements Mc {
    private static final int MOTION_HISTORY_SIZE = 5;
    private static final Map<UUID, MotionData> motionMap = new HashMap<>();

    private static final class MotionData {
        private final Vec3d[] history = new Vec3d[MOTION_HISTORY_SIZE];
        private int count;
        private int index;
        private Vec3d lastPos;

        void update(Vec3d currentPos) {
            if (lastPos != null) {
                Vec3d motion = currentPos.subtract(lastPos);
                history[index] = motion;
                index = (index + 1) % MOTION_HISTORY_SIZE;
                if (count < MOTION_HISTORY_SIZE) count++;
            }
            lastPos = currentPos;
        }

        Vec3d averageMotion() {
            if (count == 0) return Vec3d.ZERO;
            double x = 0.0, y = 0.0, z = 0.0;
            for (int i = 0; i < count; i++) {
                Vec3d m = history[i];
                if (m != null) {
                    x += m.x;
                    y += m.y;
                    z += m.z;
                }
            }
            return new Vec3d(x / count, y / count, z / count);
        }

        void reset() {
            count = 0;
            index = 0;
            lastPos = null;
            Arrays.fill(history, null);
        }
    }

    public static void tick() {
        if (mc.world == null) return;
        for (PlayerEntity player : mc.world.getPlayers()) {
            MotionData data = motionMap.computeIfAbsent(player.getUuid(), k -> new MotionData());
            data.update(player.getPos());
        }
    }

    public static Vec3d predictPos(PlayerEntity player, int ticks) {
        if (player == null) return Vec3d.ZERO;
        if (ticks <= 0) return player.getPos();
        MotionData data = motionMap.get(player.getUuid());
        if (data == null || data.count == 0) {
            return player.getPos().add(player.getVelocity().multiply(ticks));
        }
        Vec3d avgMotion = data.averageMotion();
        if (avgMotion.lengthSquared() < 1.0E-8) return player.getPos();
        Box bbox = player.getBoundingBox();
        Vec3d moved = Vec3d.ZERO;
        for (int i = 0; i < ticks; i++) {
            Vec3d full = moved.add(avgMotion);
            if (canMove(bbox, full)) {
                moved = full;
            } else if (canMove(bbox, new Vec3d(avgMotion.x, 0.0, avgMotion.z))) {
                moved = moved.add(avgMotion.x, 0.0, avgMotion.z);
            } else if (canMove(bbox, new Vec3d(0.0, avgMotion.y, 0.0))) {
                moved = moved.add(0.0, avgMotion.y, 0.0);
            } else {
                break;
            }
        }
        return player.getPos().add(moved);
    }

    public static Box getPredictedBox(PlayerEntity player, int ticks) {
        Vec3d predicted = predictPos(player, ticks);
        double halfWidth = player.getWidth() / 2.0;
        return new Box(
                predicted.x - halfWidth, predicted.y, predicted.z - halfWidth,
                predicted.x + halfWidth, predicted.y + player.getHeight(), predicted.z + halfWidth
        );
    }

    private static boolean canMove(Box bbox, Vec3d offset) {
        if (mc.world == null) return true;
        for (VoxelShape shape : mc.world.getBlockCollisions(null, bbox.offset(offset))) {
            if (!shape.isEmpty()) return false;
        }
        return true;
    }

    public static void clear() {
        motionMap.clear();
    }

    public static void remove(UUID uuid) {
        motionMap.remove(uuid);
    }
}
