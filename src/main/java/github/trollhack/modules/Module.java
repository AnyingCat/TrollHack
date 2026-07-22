package github.trollhack.modules;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.ModuleToggleEvent;
import github.trollhack.events.impl.Render2DEvent;
import github.trollhack.events.impl.Render3DEvent;
import github.trollhack.settings.Setting;
import github.trollhack.settings.impl.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Module.class);
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    public final BindSetting bind = bindSetting("Bind", -1);

    private boolean alwaysEnabled = false;

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public boolean shouldShowBind() {
        return !alwaysEnabled;
    }

    protected void setAlwaysEnabled() {
        this.alwaysEnabled = true;
        setEnabled(true);
    }

    public void onEnable() {}
    public void onDisable() {}

    public void onUpdate() {}
    public void onRender2D(DrawContext context) {}
    public void onRender3D(MatrixStack matrices) {}

    public String getHudInfo() {
        return "";
    }


    @EventHandler(priority = EventPriority.LOW)
    private void onRender2DEvent(Render2DEvent event) {
        if (isEnabled() && !nullCheck()) {
            onRender2D(event.getContext());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onRender3DEvent(Render3DEvent event) {
        if (isEnabled() && !nullCheck()) {
            onRender3D(event.getMatrices());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (alwaysEnabled && !enabled) return;
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                EventBusHolder.INSTANCE.subscribe(this);
                onEnable();
                LOGGER.info("Enabled module: {}", getName());
            } else {
                EventBusHolder.INSTANCE.unsubscribe(this);
                onDisable();
                LOGGER.info("Disabled module: {}", getName());
            }
            EventBusHolder.INSTANCE.post(new ModuleToggleEvent(this, enabled));
        }
    }

    public void toggle() {
        if (alwaysEnabled) return;
        setEnabled(!enabled);
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public int getKeyBind() {
        return bind.getValue();
    }

    public void setKeyBind(int keyBind) {
        bind.setValue(keyBind);
    }

    public boolean isHold() {
        return bind.isHold();
    }

    public void setHold(boolean hold) {
        bind.setHold(hold);
    }

    public static boolean nullCheck() {
        return mc.player == null || mc.world == null;
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    protected BooleanSetting booleanSetting(String name, boolean defaultValue) {
        return addSetting(new BooleanSetting(name, this, defaultValue));
    }

    protected BooleanSetting booleanSetting(String name, boolean defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new BooleanSetting(name, this, defaultValue, visibility));
    }

    protected IntegerSetting integerSetting(String name, int defaultValue, int min, int max, int step) {
        return addSetting(new IntegerSetting(name, this, defaultValue, min, max, step));
    }

    protected IntegerSetting integerSetting(String name, int defaultValue, int min, int max, int step, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new IntegerSetting(name, this, defaultValue, min, max, step, visibility));
    }

    protected FloatSetting floatSetting(String name, float defaultValue, float min, float max, float step) {
        return addSetting(new FloatSetting(name, this, defaultValue, min, max, step));
    }

    protected FloatSetting floatSetting(String name, float defaultValue, float min, float max, float step, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new FloatSetting(name, this, defaultValue, min, max, step, visibility));
    }

    protected <E extends Enum<E>> EnumSetting<E> enumSetting(String name, E defaultValue) {
        return addSetting(new EnumSetting<>(name, this, defaultValue));
    }

    protected <E extends Enum<E>> EnumSetting<E> enumSetting(String name, E defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new EnumSetting<>(name, this, defaultValue, visibility));
    }

    protected StringSetting stringSetting(String name, String defaultValue) {
        return addSetting(new StringSetting(name, this, defaultValue));
    }

    protected StringSetting stringSetting(String name, String defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new StringSetting(name, this, defaultValue, visibility));
    }

    protected BindSetting bindSetting(String name, int defaultValue) {
        return addSetting(new BindSetting(name, this, defaultValue));
    }

    protected BindSetting bindSetting(String name, int defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new BindSetting(name, this, defaultValue, visibility));
    }

    protected ColorSetting colorSetting(String name, java.awt.Color defaultValue) {
        return addSetting(new ColorSetting(name, this, defaultValue));
    }

    protected ColorSetting colorSetting(String name, java.awt.Color defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new ColorSetting(name, this, defaultValue, visibility));
    }

    protected ColorSetting colorSetting(String name, java.awt.Color defaultValue, boolean allowAlpha) {
        return addSetting(new ColorSetting(name, this, defaultValue, allowAlpha));
    }

    public void resetSettings() {
        settings.forEach(Setting::reset);
    }

    @Override
    public String toString() {
        return name;
    }
}
