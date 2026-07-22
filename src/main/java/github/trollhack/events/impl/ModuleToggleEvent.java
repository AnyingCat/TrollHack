package github.trollhack.events.impl;

import github.trollhack.events.Event;
import github.trollhack.modules.Module;

public class ModuleToggleEvent extends Event {
    private final Module module;
    private final boolean enabled;

    public ModuleToggleEvent(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }

    public Module getModule() { return module; }
    public boolean isEnabled() { return enabled; }
}
