package github.trollhack.utils.math;

import github.trollhack.utils.interfaces.Mc;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtils implements Mc {
    public static Vec3d getEyePos() {
        if (mc.player == null) return Vec3d.ZERO;
        return new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
    }

    public static float[] calcRotation(Vec3d from, Vec3d to) {
        double diffX = to.x - from.x;
        double diffY = to.y - from.y;
        double diffZ = to.z - from.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }

    public static float angleDiff(float currentYaw, float currentPitch, float targetYaw, float targetPitch) {
        float yawD = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchD = Math.abs(MathHelper.wrapDegrees(targetPitch - currentPitch));
        return (float) Math.sqrt(yawD * yawD + pitchD * pitchD);
    }

    public static float clampYawStep(float currentYaw, float targetYaw, float maxStep) {
        return currentYaw + MathHelper.clamp(MathHelper.wrapDegrees(targetYaw - currentYaw), -maxStep, maxStep);
    }

    public static float clampPitchStep(float currentPitch, float targetPitch, float maxStep) {
        return MathHelper.clamp(currentPitch + MathHelper.clamp(MathHelper.wrapDegrees(targetPitch - currentPitch), -maxStep, maxStep), -90f, 90f);
    }

    public static float fixGcd(float value, float prev, double gcd) {
        return (float) (value - (value - prev) % gcd);
    }

    public static double getMouseGcd() {
        if (mc.options == null || mc.options.getMouseSensitivity() == null) return 0.0;
        return Math.pow(mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2, 3.0) * 1.2;
    }
}
