package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class FloatSetting extends Setting<Float> {
    private final float min;
    private final float max;
    private final float step;

    public FloatSetting(String name, Module module, float defaultValue, float min, float max, float step, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public FloatSetting(String name, Module module, float defaultValue, float min, float max, float step) {
        this(name, module, defaultValue, min, max, step, null);
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    @Override
    public void setValue(Float value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    @Override
    public String getStringValue() {
        return String.format("%.2f", getValue());
    }
}