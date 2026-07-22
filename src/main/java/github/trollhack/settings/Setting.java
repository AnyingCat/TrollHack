package github.trollhack.settings;

import github.trollhack.modules.Module;

import java.util.function.Supplier;

public abstract class Setting<T> {
    protected final String name;
    protected final T defaultValue;
    protected T value;
    protected final Supplier<Boolean> visibility;
    protected final Module module;
    protected Setting<?> parent;

    public Setting(String name, Module module, T defaultValue, Supplier<Boolean> visibility) {
        this.name = name;
        this.module = module;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.visibility = visibility != null ? visibility : () -> true;
    }

    public Setting(String name, Module module, T defaultValue) {
        this(name, module, defaultValue, null);
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public boolean isVisible() {
        return visibility.get();
    }

    public Module getModule() {
        return module;
    }

    public Setting<?> getParent() {
        return parent;
    }

    public void setParent(Setting<?> parent) {
        this.parent = parent;
    }

    public void reset() {
        setValue(defaultValue);
    }

    public abstract String getStringValue();
}
