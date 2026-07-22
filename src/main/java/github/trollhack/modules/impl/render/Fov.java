package github.trollhack.modules.impl.render;

import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BindSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.FloatSetting;
import meteordevelopment.orbit.EventHandler;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class Fov extends Module {
    public static final Fov INSTANCE = new Fov();

    public final FloatSetting fovValue = floatSetting("Fov", 120.0f, 1.0f, 180.0f, 0.5f);
    public final BooleanSetting dynamicFov = booleanSetting("DynamicFov", false);
    public final BindSetting zoomBind = bindSetting("ZoomBind", -1);
    public final BooleanSetting switchZoom = booleanSetting("SwitchZoom", false, () -> !zoomBind.isEmpty());
    public final FloatSetting zoomFov = floatSetting("ZoomFov", 40.0f, 1.0f, 180.0f, 0.5f, () -> !zoomBind.isEmpty());
    public final FloatSetting sensitivityMultiplier = floatSetting("SensitivityMultiplier", 1.0f, 0.1f, 2.0f, 0.1f, () -> !zoomBind.isEmpty());
    public final BooleanSetting smoothCamera = booleanSetting("SmoothCamera", false, () -> !zoomBind.isEmpty());

    private boolean zooming = false;

    public Fov() {
        super("Fov", Category.RENDER);
    }

    @Override
    public String getHudInfo() {
        return String.format("%.1f", fovValue.getValue());
    }

    @Override
    public void onDisable() {
        zooming = false;
        mc.options.smoothCameraEnabled = false;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        if (nullCheck()) return;

        if (zooming && smoothCamera.getValue()) {
            mc.options.smoothCameraEnabled = true;
        }

        updateSmoothCamera();
    }

    private void updateSmoothCamera() {
        if (smoothCamera.getValue()) {
            mc.options.smoothCameraEnabled = zooming;
        }
    }

    public void onZoomKeyPress(int key) {
        if (!isEnabled() || zoomBind.isEmpty()) return;
        if (key != zoomBind.getValue()) return;

        if (switchZoom.getValue()) {
            zooming = !zooming;
            updateSmoothCamera();
        } else {
            zooming = true;
            updateSmoothCamera();
        }
    }

    public void onZoomKeyRelease(int key) {
        if (!isEnabled() || zoomBind.isEmpty()) return;
        if (key != zoomBind.getValue()) return;

        if (!switchZoom.getValue()) {
            zooming = false;
            updateSmoothCamera();
        }
    }

    public double getFOVModifierDynamic(double value) {
        if (isEnabled() && dynamicFov.getValue()) {
            return getFov();
        }
        return value;
    }

    public void getFOVModifierNoDynamic(CallbackInfoReturnable<Double> cir) {
        if (isEnabled() && !dynamicFov.getValue()) {
            cir.setReturnValue((double) getFov());
        }
    }

//    public double getMouseSensitivity(double value) {
//        if (isEnabled() && zooming) {
//            return mc.options.getMouseSensitivity().getValue() * 0.6f * sensitivityMultiplier.getValue() + 0.2f;
//        }
//        return value;
//    }

    public float getSensitivityMultiplier() {
        return sensitivityMultiplier.getValue();
    }

    public boolean isZooming() {
        return zooming;
    }

    private float getFov() {
        return zooming ? zoomFov.getValue() : fovValue.getValue();
    }
}
