package github.trollhack.utils.world;

import github.trollhack.core.Managers;
import github.trollhack.utils.interfaces.Mc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class EntityUtil implements Mc {

    public static List<PlayerEntity> getPlayersInRange(double range) {
        if (mc.world == null || mc.player == null) return new ArrayList<>();
        List<PlayerEntity> list = new ArrayList<>();
        Vec3d playerPos = mc.player.getPos();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead() || player.getHealth() <= 0.0f) continue;
            if (player.isSpectator()) continue;
            if (Managers.FRIEND.isFriend(player)) continue;
            if (playerPos.squaredDistanceTo(player.getPos()) > range * range) continue;
            list.add(player);
        }
        return list;
    }

    public static List<EndCrystalEntity> getCrystalsInRange(double range) {
        if (mc.world == null || mc.player == null) return new ArrayList<>();
        List<EndCrystalEntity> list = new ArrayList<>();
        Vec3d playerPos = mc.player.getPos();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (crystal.isRemoved()) continue;
            if (playerPos.squaredDistanceTo(crystal.getPos()) > range * range) continue;
            list.add(crystal);
        }
        return list;
    }

    public static boolean isInRange(Entity entity, double range) {
        if (mc.player == null || entity == null) return false;
        return mc.player.squaredDistanceTo(entity) <= range * range;
    }

    public static Vec3d getHitPos(Entity entity) {
        return entity.getPos().add(0, entity.getHeight() / 2.0, 0);
    }

    public static Vec3d getEyePos(Entity entity) {
        return entity.getPos().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static List<PlayerEntity> getTargets(double range, boolean includeFriends) {
        if (mc.world == null || mc.player == null) return new ArrayList<>();
        List<PlayerEntity> list = new ArrayList<>();
        Vec3d playerPos = mc.player.getPos();
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            if (player.isDead() || player.getHealth() <= 0.0f) continue;
            if (player.isSpectator()) continue;
            if (!includeFriends && Managers.FRIEND.isFriend(player)) continue;
            if (playerPos.distanceTo(player.getPos()) > range) continue;
            list.add(player);
        }
        return list;
    }

    public static Vec3d predictPos(PlayerEntity player, int ticks) {
        return PredictionUtil.predictPos(player, ticks);
    }

    public static int getNeededTicks(PlayerEntity player, double distance) {
        double speed = Math.sqrt(player.getX() - player.prevX) * (player.getX() - player.prevX)
                + (player.getZ() - player.prevZ) * (player.getZ() - player.prevZ);
        if (speed < 0.001) return 0;
        return MathHelper.clamp((int) (distance / speed), 0, 20);
    }
}
