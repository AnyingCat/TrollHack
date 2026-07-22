package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, Module module, String defaultValue, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
    }

    public StringSetting(String name, Module module, String defaultValue) {
        super(name, module, defaultValue);
    }

    @Override
    public String getStringValue() {
        return getValue();
    }
}