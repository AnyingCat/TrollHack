package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> enumClass;

    public EnumSetting(String name, Module module, E defaultValue, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
        this.enumClass = defaultValue.getDeclaringClass();
    }

    public EnumSetting(String name, Module module, E defaultValue) {
        this(name, module, defaultValue, null);
    }

    public E[] getValues() {
        return enumClass.getEnumConstants();
    }

    public void cycle() {
        E[] values = getValues();
        setValue(values[(getValue().ordinal() + 1) % values.length]);
    }

    @Override
    public String getStringValue() {
        return getValue().name();
    }

    public boolean setValueFromString(String name) {
        for (E value : getValues()) {
            if (value.name().equals(name)) {
                setValue(value);
                return true;
            }
        }
        return false;
    }
}
