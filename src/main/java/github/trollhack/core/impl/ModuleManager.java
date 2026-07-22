package github.trollhack.core.impl;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.ClickGUI;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.modules.impl.client.HudEditor;
import github.trollhack.modules.impl.client.Rotations;
import github.trollhack.modules.impl.combat.AutoCrystal;
import github.trollhack.modules.impl.combat.AutoEXP;
import github.trollhack.modules.impl.combat.AutoOffhand;
import github.trollhack.modules.impl.combat.KillAura;
import github.trollhack.modules.impl.combat.TotemPopCounter;
import github.trollhack.modules.impl.misc.FakePlayer;
import github.trollhack.modules.impl.misc.Friends;
import github.trollhack.modules.impl.movement.*;
import github.trollhack.modules.impl.player.NoFall;
import github.trollhack.modules.impl.player.Replenish;
import github.trollhack.modules.impl.render.Ambience;
import github.trollhack.modules.impl.render.CrystalChams;
import github.trollhack.modules.impl.render.ESP;
import github.trollhack.modules.impl.render.ESP2D;
import github.trollhack.modules.impl.render.Fov;
import github.trollhack.modules.impl.render.NameTags;
import github.trollhack.modules.impl.render.NewChunks;
import github.trollhack.modules.impl.render.NoRender;
import github.trollhack.modules.impl.render.ShulkerPreview;
import github.trollhack.modules.impl.render.StorageESP;
import github.trollhack.modules.impl.render.ViewModel;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.util.*;

public class ModuleManager {
    private final List<Module> modules;
    private final Map<Category, List<Module>> modulesByCategory = new EnumMap<>(Category.class);

    public ModuleManager() {
        for (Category category : Category.values()) {
            modulesByCategory.put(category, new ArrayList<>());
        }
        modules = List.of(
                ClickGUI.INSTANCE,
                GuiSetting.INSTANCE,
                HudEditor.INSTANCE,
                Rotations.INSTANCE,
                AutoCrystal.INSTANCE,
                AutoEXP.INSTANCE,
                AutoOffhand.INSTANCE,
                KillAura.INSTANCE,
                TotemPopCounter.INSTANCE,
                FakePlayer.INSTANCE,
                Friends.INSTANCE,
                NoSlow.INSTANCE,
                Speed.INSTANCE,
                Sprint.INSTANCE,
                Step.INSTANCE,
                Velocity.INSTANCE,
                NoFall.INSTANCE,
                Replenish.INSTANCE,
                Ambience.INSTANCE,
                CrystalChams.INSTANCE,
                ESP.INSTANCE,
                ESP2D.INSTANCE,
                Fov.INSTANCE,
                NameTags.INSTANCE,
                NewChunks.INSTANCE,
                NoRender.INSTANCE,
                ShulkerPreview.INSTANCE,
                StorageESP.INSTANCE,
                ViewModel.INSTANCE
        );
        for (Module module : modules) {
            modulesByCategory.get(module.getCategory()).add(module);
        }
        EventBusHolder.INSTANCE.subscribe(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        modules.stream()
                .filter(Module::isEnabled)
                .forEach(Module::onUpdate);
    }

    public void handleKeyPress(int key) {
        for (Module module : modules) {
            if (module.getKeyBind() == key) {
                if (module.isHold()) {
                    module.setEnabled(true);
                } else {
                    module.toggle();
                }
            }
        }
    }

    public void handleKeyRelease(int key) {
        for (Module module : modules) {
            if (module.getKeyBind() == key && module.isHold()) {
                module.setEnabled(false);
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modulesByCategory.get(category);
    }
}
