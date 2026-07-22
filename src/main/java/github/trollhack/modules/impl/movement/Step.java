package github.trollhack.modules.impl.movement;

import github.trollhack.events.impl.MotionEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Step extends Module {
    public static Step INSTANCE = new Step();

    public enum Mode {
        Vanilla,
        NCP
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.Vanilla);
    public final FloatSetting height = floatSetting("Height", 1.0f, 0.5f, 2.5f, 0.5f);
    public final BooleanSetting onlyMoving = booleanSetting("OnlyMoving", true);
    public final BooleanSetting pauseSneaking = booleanSetting("PauseSneaking", true);
    public final BooleanSetting pauseInLiquid = booleanSetting("PauseInLiquid", true);

    private boolean stepping;
    private double stepY;

    public Step() {
        super("Step", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        stepping = false;
        stepY = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            setStepHeight(0.6f);
        }
        stepping = false;
    }

    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;
        if (shouldPause()) {
            setStepHeight(0.6f);
            return;
        }
        setStepHeight(height.getValue());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMotion(MotionEvent event) {
        if (nullCheck() || !event.isPre()) return;
        if (mode.getValue() != Mode.NCP) return;

        double currentStepHeight = mc.player.getY() - mc.player.prevY;
        if (currentStepHeight <= 0.6 || currentStepHeight > height.getValue()) {
            stepping = false;
            return;
        }
        if (stepping && Math.abs(currentStepHeight - stepY) < 0.001) return;

        stepping = true;
        stepY = currentStepHeight;

        double[] offsets = getOffsets(currentStepHeight);
        if (offsets == null || offsets.length == 0) return;

        double baseY = mc.player.prevY;
        for (double offset : offsets) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(),
                            baseY + offset,
                            mc.player.getZ(),
                            false
                    )
            );
        }
    }

    private double[] getOffsets(double stepHeight) {
        if (stepHeight <= 0.75) return new double[]{0.42, 0.753};
        if (stepHeight <= 0.875) return new double[]{0.39, 0.7};
        if (stepHeight <= 1.0) return new double[]{0.42, 0.753, 1.0};
        if (stepHeight <= 1.5) return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.5};
        if (stepHeight <= 2.0) return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.7};
        if (stepHeight <= 2.5) return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 2.5};
        return null;
    }

    private boolean shouldPause() {
        if (pauseSneaking.getValue() && mc.player.isSneaking()) return true;
        if (pauseInLiquid.getValue() && mc.player.isInFluid()) return true;
        if (mc.player.getAbilities().flying || mc.player.isFallFlying() || mc.player.isRiding()) return true;
        if (!mc.player.isOnGround()) return true;
        return onlyMoving.getValue() && mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0;
    }

    private void setStepHeight(float value) {
        var attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.setBaseValue(value);
        }
    }
}
