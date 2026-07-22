package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, Module module, boolean defaultValue, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
    }

    public BooleanSetting(String name, Module module, boolean defaultValue) {
        super(name, module, defaultValue);
    }

    public boolean isEnabled() {
        return getValue();
    }

    @Override
    public String getStringValue() {
        return getValue() ? "Enabled" : "Disabled";
    }
}