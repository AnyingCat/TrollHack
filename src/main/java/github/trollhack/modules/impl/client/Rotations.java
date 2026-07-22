package github.trollhack.modules.impl.client;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import net.minecraft.util.math.MathHelper;

public class Rotations extends Module {
    public static final Rotations INSTANCE = new Rotations();

    public final FloatSetting yawStep = floatSetting("YawStep", 0.4f, 0.01f, 1.0f, 0.01f);
    public final FloatSetting pitchStep = floatSetting("PitchStep", 0.4f, 0.01f, 1.0f, 0.01f);
    public final FloatSetting rotateTimeout = floatSetting("Timeout", 2.0f, 0.5f, 10.0f, 0.5f);
    public final BooleanSetting serverRotate = booleanSetting("ServerRotate", false);
    public final EnumSetting<MoveFixMode> moveFix = enumSetting("MoveFix", MoveFixMode.Off);

    public enum MoveFixMode {
        Off,
        Focused,
        Free
    }

    private float prevYaw;
    private float prevPitch;
    public float fixRotation = Float.NaN;

    public Rotations() {
        super("Rotations", Category.CLIENT);
        setAlwaysEnabled();
    }

    @Override
    public void onEnable() {
        fixRotation = Float.NaN;
    }

    public void applyMoveFixPre() {
        if (mc.player == null || moveFix.getValue() == MoveFixMode.Off || Float.isNaN(fixRotation)) return;
        prevYaw = mc.player.getYaw();
        prevPitch = mc.player.getPitch();
        mc.player.setYaw(fixRotation);
        if (moveFix.getValue() == MoveFixMode.Focused && Managers.ROTATION.isRotating()) {
            mc.player.setPitch(Managers.ROTATION.getPitch());
        }
    }

    public void applyMoveFixPost() {
        if (mc.player == null || moveFix.getValue() == MoveFixMode.Off || Float.isNaN(fixRotation)) return;
        mc.player.setYaw(prevYaw);
        mc.player.setPitch(prevPitch);
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;
        if (Float.isNaN(fixRotation) || !Managers.ROTATION.isRotating()) {
            fixRotation = Float.NaN;
            return;
        }
        fixRotation = Managers.ROTATION.getYaw();
        if (moveFix.getValue() != MoveFixMode.Free) return;

        float mF = mc.player.input.movementForward;
        float mS = mc.player.input.movementSideways;
        float delta = (mc.player.getYaw() - fixRotation) * MathHelper.RADIANS_PER_DEGREE;
        float cos = MathHelper.cos(delta);
        float sin = MathHelper.sin(delta);
        mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
        mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
    }
}
