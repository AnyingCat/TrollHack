package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class IntegerSetting extends Setting<Integer> {
    private final int min;
    private final int max;
    private final int step;

    public IntegerSetting(String name, Module module, int defaultValue, int min, int max, int step, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public IntegerSetting(String name, Module module, int defaultValue, int min, int max, int step) {
        this(name, module, defaultValue, min, max, step, null);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }

    @Override
    public String getStringValue() {
        return String.valueOf(getValue());
    }
}