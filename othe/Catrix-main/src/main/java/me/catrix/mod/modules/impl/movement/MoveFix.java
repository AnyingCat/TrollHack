package me.catrix.mod.modules.impl.movement;

import me.catrix.api.events.eventbus.EventHandler;
import me.catrix.api.events.impl.JumpEvent;
import me.catrix.api.events.impl.KeyboardInputEvent;
import me.catrix.api.events.impl.TravelEvent;
import me.catrix.api.events.impl.UpdateVelocityEvent;
import me.catrix.mod.modules.Module;
import me.catrix.mod.modules.impl.client.BaritoneModule;
import me.catrix.mod.modules.impl.player.Freecam;
import me.catrix.mod.modules.settings.impl.BooleanSetting;
import me.catrix.mod.modules.settings.impl.EnumSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MoveFix extends Module {
    public static MoveFix INSTANCE;

    public MoveFix() {
        super("MoveFix", Category.Movement);
        INSTANCE = this;
        setChinese("移动修复");
    }

    public EnumSetting<UpdateMode> updateMode = add(new EnumSetting<>("UpdateMode", UpdateMode.UpdateMouse));
    public final BooleanSetting grim = add(new BooleanSetting("Grim", true)).setParent();
    private final BooleanSetting travel = add(new BooleanSetting("Travel", false, grim::isOpen));

    public enum UpdateMode {
        MovementPacket,
        UpdateMouse,
        All
    }

    public static float fixRotation;
    public static float fixPitch;
    private float savedYaw;
    private float savedPitch;

    @EventHandler
    public void onTravel(TravelEvent e) {
        if (shouldSkip()) return;

        if (e.isPre()) {
            saveCurrentRotation();
            applyFixedRotation();
        } else {
            restoreRotation();
        }
    }

    @EventHandler
    public void onJump(JumpEvent e) {
        if (shouldSkip()) return;

        if (e.isPre()) {
            saveCurrentRotation();
            applyFixedRotation();
        } else {
            restoreRotation();
        }
    }

    @EventHandler
    public void onUpdateVelocity(UpdateVelocityEvent event) {
        if (shouldSkip() || travel.getValue()) return;

        event.cancel();
        Vec3d velocity = movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixRotation);
        event.setVelocity(velocity);
    }

    @EventHandler(priority = -999)
    public void onKeyboardInput(KeyboardInputEvent e) {
        if (shouldSkip()) return;

        float forward = mc.player.input.movementForward;
        float sideways = mc.player.input.movementSideways;

        float deltaYaw = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(deltaYaw);
        float sin = MathHelper.sin(deltaYaw);

        mc.player.input.movementForward = forward * cos + sideways * sin;
        mc.player.input.movementSideways = sideways * cos - forward * sin;
    }

    private boolean shouldSkip() {
        return BaritoneModule.isActive() ||
                Freecam.INSTANCE.isOn() ||
                mc.player.isRiding() ||
                !grim.getValue();
    }

    private void saveCurrentRotation() {
        savedYaw = mc.player.getYaw();
        savedPitch = mc.player.getPitch();
    }

    private void applyFixedRotation() {
        mc.player.setYaw(fixRotation);
        mc.player.setPitch(fixPitch);
    }

    private void restoreRotation() {
        mc.player.setYaw(savedYaw);
        mc.player.setPitch(savedPitch);
    }

    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double lengthSq = movementInput.lengthSquared();
        if (lengthSq < 1.0E-7) {
            return Vec3d.ZERO;
        }

        Vec3d normalized = lengthSq > 1.0 ? movementInput.normalize() : movementInput;
        Vec3d scaled = normalized.multiply(speed);

        float sinYaw = MathHelper.sin(yaw * MathHelper.RADIANS_PER_DEGREE);
        float cosYaw = MathHelper.cos(yaw * MathHelper.RADIANS_PER_DEGREE);

        return new Vec3d(
                scaled.x * cosYaw - scaled.z * sinYaw,
                scaled.y,
                scaled.z * cosYaw + scaled.x * sinYaw
        );
    }
}