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
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class Speed extends Module {
    public static Speed INSTANCE = new Speed();

    public enum Mode {
        NCP,
        Custom
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.NCP);
    public final BooleanSetting autoJump = booleanSetting("AutoJump", true, () -> mode.getValue() == Mode.Custom);
    public final FloatSetting speed = floatSetting("Speed", 0.2768f, 0.1f, 2.0f, 0.01f, () -> mode.getValue() == Mode.Custom);
    public final BooleanSetting strafe = booleanSetting("Strafe", true, () -> mode.getValue() == Mode.Custom);

    private int stage;
    private double baseSpeed;
    private double lastDistance;
    private int ticks;
    private float prevForward;

    public Speed() {
        super("Speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            baseSpeed = getBaseMoveSpeed();
            lastDistance = 0;
        }
        stage = 1;
        ticks = 0;
        prevForward = 0;
    }

    public String getInfo() {
        return mode.getValue().name();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;
        double dx = mc.player.getX() - mc.player.prevX;
        double dz = mc.player.getZ() - mc.player.prevZ;
        lastDistance = Math.sqrt(dx * dx + dz * dz);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMotion(MotionEvent event) {
        if (nullCheck() || !event.isPre()) return;

        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) {
            if (mode.getValue() == Mode.Custom) {
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            }
            reset();
            return;
        }

        if (shouldPause()) {
            reset();
            return;
        }

        if (mode.getValue() == Mode.NCP) {
            handleNCP();
        } else {
            handleCustom();
        }
    }

    private void handleNCP() {
        boolean onGround = mc.player.isOnGround();
        boolean canJump = !mc.player.horizontalCollision;

        float currentSpeed = mc.player.input.movementForward <= 0 && prevForward > 0 ? (float) lastDistance * 0.66f : (float) lastDistance;

        if (stage == 1 && onGround && canJump) {
            mc.player.setVelocity(mc.player.getVelocity().x, getJumpSpeed(), mc.player.getVelocity().z);
            baseSpeed *= 2.149;
            stage = 2;
        } else if (stage == 2) {
            baseSpeed = currentSpeed - (0.66 * (currentSpeed - getBaseMoveSpeed()));
            stage = 3;
        } else {
            if (onGround || mc.player.verticalCollision) {
                stage = 1;
            }
            baseSpeed = currentSpeed - currentSpeed / 159.0;
        }

        baseSpeed = Math.max(baseSpeed, getBaseMoveSpeed());

        double ncpSpeed = mc.player.input.movementForward < 1 ? 0.465 : 0.576;
        double ncpBypassSpeed = mc.player.input.movementForward < 1 ? 0.44 : 0.57;

        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            ncpSpeed *= 1 + (0.2 * (amplifier + 1));
            ncpBypassSpeed *= 1 + (0.2 * (amplifier + 1));
        }

        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            ncpSpeed /= 1 + (0.2 * (amplifier + 1));
            ncpBypassSpeed /= 1 + (0.2 * (amplifier + 1));
        }

        baseSpeed = Math.min(baseSpeed, ticks > 25 ? ncpSpeed : ncpBypassSpeed);

        if (ticks++ > 50) {
            ticks = 0;
        }

        setSpeed(baseSpeed);
        prevForward = mc.player.input.movementForward;
    }

    private void handleCustom() {
        double targetSpeed = speed.getValue();
        if (autoJump.getValue() && mc.player.isOnGround()) {
            mc.player.jump();
        }
        if (strafe.getValue()) {
            setSpeed(targetSpeed);
        }
    }

    private void setSpeed(double speed) {
        double forward = mc.player.input.movementForward;
        double strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            return;
        }

        if (forward != 0) {
            if (strafe > 0) {
                yaw += forward > 0 ? -45 : 45;
            } else if (strafe < 0) {
                yaw += forward > 0 ? 45 : -45;
            }
            strafe = 0;
            if (forward > 0) {
                forward = 1;
            } else if (forward < 0) {
                forward = -1;
            }
        }

        double rad = Math.toRadians(yaw);
        mc.player.setVelocity(
                (forward * speed * -Math.sin(rad) + strafe * speed * Math.cos(rad)),
                mc.player.getVelocity().y,
                (forward * speed * Math.cos(rad) - strafe * speed * -Math.sin(rad))
        );
    }

    private void reset() {
        stage = 1;
        baseSpeed = getBaseMoveSpeed();
        ticks = 0;
    }

    private boolean shouldPause() {
        if (mc.player.isInFluid() || mc.player.isRiding() || mc.player.isClimbing()) return true;
        if (mc.player.getAbilities().flying || mc.player.isFallFlying()) return true;
        if (isInWeb()) return true;
        return mc.player.getHungerManager().getFoodLevel() <= 6;
    }

    private boolean isInWeb() {
        if (mc.world == null || mc.player == null) return false;
        Box playerBox = mc.player.getBoundingBox();
        BlockPos blockPos = BlockPos.ofFloored(mc.player.getPos());
        for (int x = blockPos.getX() - 2; x <= blockPos.getX() + 2; x++) {
            for (int y = blockPos.getY() - 1; y <= blockPos.getY() + 4; y++) {
                for (int z = blockPos.getZ() - 2; z <= blockPos.getZ() + 2; z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (playerBox.intersects(new Box(bp)) && mc.world.getBlockState(bp).isOf(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private double getBaseMoveSpeed() {
        double base = 0.2873;
        if (mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            base *= 1 + (0.2 * (amplifier + 1));
        }
        if (mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            base /= 1 + (0.2 * (amplifier + 1));
        }
        return base;
    }

    private double getJumpSpeed() {
        double jump = 0.3999999463558197;
        if (mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            int amplifier = mc.player.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier();
            jump += (amplifier + 1) * 0.1;
        }
        return jump;
    }
}
