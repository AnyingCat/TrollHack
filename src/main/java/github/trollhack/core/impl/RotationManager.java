package github.trollhack.core.impl;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.MotionEvent;
import github.trollhack.events.impl.PacketEvent;
import github.trollhack.modules.impl.client.Rotations;
import github.trollhack.utils.math.RotationUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private float rotationYaw;
    private float rotationPitch;
    private float lastYaw;
    private float lastPitch;

    private Vec3d directionVec;
    private long rotateTime;
    private boolean rotating;
    private boolean directRotation;
    private float directYaw;
    private float directPitch;

    private float renderPitch;
    private float renderYawOffset;
    private float prevRenderPitch;
    private float prevRenderYawOffset;
    private float rotationYawHead;
    private float prevRotationYawHead;
    private int lastTick = -1;

    public RotationManager() {
        EventBusHolder.INSTANCE.subscribe(this);
    }

    public float getYaw() {
        return rotationYaw;
    }

    public float getPitch() {
        return rotationPitch;
    }

    public boolean isRotating() {
        return rotating;
    }

    public void lookAt(Vec3d vec) {
        if (mc.player == null || vec == null) return;
        directionVec = vec;
        directRotation = false;
        rotateTime = System.currentTimeMillis();
        rotating = true;
    }

    public void setRotation(float yaw, float pitch) {
        if (mc.player == null) return;
        directYaw = yaw;
        directPitch = pitch;
        directRotation = true;
        directionVec = null;
        rotateTime = System.currentTimeMillis();
        rotating = true;
    }

    public void stopRotating() {
        rotating = false;
        directionVec = null;
        directRotation = false;
    }

    public float[] getRotation(Vec3d vec) {
        return RotationUtils.calcRotation(RotationUtils.getEyePos(), vec);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMotion(MotionEvent event) {
        if (mc.player == null || !event.isPre()) return;
        Rotations rotations = Rotations.INSTANCE;

        if (!rotating) {
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
            setRenderRotation(rotationYaw, rotationPitch, false);
            return;
        }

        if (System.currentTimeMillis() - rotateTime >= (long) (rotations.rotateTimeout.getValue() * 1000)) {
            stopRotating();
            rotationYaw = mc.player.getYaw();
            rotationPitch = mc.player.getPitch();
            setRenderRotation(rotationYaw, rotationPitch, true);
            return;
        }

        float[] target;
        if (directRotation) {
            target = new float[]{directYaw, directPitch};
        } else {
            if (directionVec == null) {
                stopRotating();
                return;
            }
            target = RotationUtils.calcRotation(RotationUtils.getEyePos(), directionVec);
        }
        float prevYaw = rotationYaw;
        float prevPitch = rotationPitch;

        rotationYaw = RotationUtils.clampYawStep(rotationYaw, target[0], 180f * rotations.yawStep.getValue());
        rotationPitch = RotationUtils.clampPitchStep(rotationPitch, target[1], 90f * rotations.pitchStep.getValue());

        double gcd = RotationUtils.getMouseGcd();
        if (gcd > 0.0) {
            rotationYaw = RotationUtils.fixGcd(rotationYaw, prevYaw, gcd);
            rotationPitch = RotationUtils.fixGcd(rotationPitch, prevPitch, gcd);
        }

        event.setYaw(rotationYaw);
        event.setPitch(rotationPitch);
        setRenderRotation(rotationYaw, rotationPitch, true);
        if (rotations.serverRotate.getValue()) {
            mc.player.setYaw(rotationYaw);
            mc.player.setPitch(rotationPitch);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onMotionPost(MotionEvent event) {
        if (mc.player == null || !event.isPost()) return;
        setRenderRotation(lastYaw, lastPitch, false);
    }

    @EventHandler(priority = -999)
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || event.isCancelled()) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket packet && packet.changesLook()) {
            lastYaw = packet.getYaw(lastYaw);
            lastPitch = packet.getPitch(lastPitch);
            setRenderRotation(lastYaw, lastPitch, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            lastYaw = packet.getFlags().contains(PositionFlag.X_ROT) ? lastYaw + packet.getYaw() : packet.getYaw();
            lastPitch = packet.getFlags().contains(PositionFlag.Y_ROT) ? lastPitch + packet.getPitch() : packet.getPitch();
            rotationYaw = lastYaw;
            rotationPitch = lastPitch;
            setRenderRotation(lastYaw, lastPitch, true);
        }
    }

    private void setRenderRotation(float yaw, float pitch, boolean force) {
        if (mc.player == null) return;
        if (mc.player.age == lastTick && !force) return;
        lastTick = mc.player.age;
        prevRenderPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        double xDif = mc.player.getX() - mc.player.prevX;
        double zDif = mc.player.getZ() - mc.player.prevZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            float offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            result = (95f < wrap && wrap < 265f) ? offset - 180f : offset;
        }

        if (mc.player.handSwingProgress > 0f) result = yaw;

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        float offset = MathHelper.wrapDegrees(yaw - result);
        if (offset < -75f) offset = -75f;
        else if (offset >= 75f) offset = 75f;

        result = yaw - offset;
        if (offset * offset > 2500f) result += offset * 0.2f;
        return result;
    }

    public float getRenderPitch() {
        return renderPitch;
    }

    public float getRenderYawOffset() {
        return renderYawOffset;
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    public float getPrevRenderPitch() {
        return prevRenderPitch;
    }

    public float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }
}
