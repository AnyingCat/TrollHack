package github.trollhack.modules;

import github.trollhack.events.EventBusHolder;
import github.trollhack.events.impl.Render2DEvent;
import github.trollhack.settings.Setting;
import github.trollhack.settings.impl.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public abstract class HudModule {
    protected static final net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();

    private final String name;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    private float posX;
    private float posY;
    private float width;
    private float height;

    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public HudModule(String name, float posX, float posY, float width, float height) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public void onEnable() {}
    public void onDisable() {}
    public abstract void onHudRender(DrawContext context);

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                EventBusHolder.INSTANCE.subscribe(this);
                onEnable();
            } else {
                EventBusHolder.INSTANCE.unsubscribe(this);
                onDisable();
            }
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }

    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public void setPosX(float posX) { this.posX = posX; }
    public void setPosY(float posY) { this.posY = posY; }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public void setWidth(float width) { this.width = width; }
    public void setHeight(float height) { this.height = height; }

    public boolean isDragging() { return dragging; }
    public void setDragging(boolean dragging) { this.dragging = dragging; }
    public float getDragOffsetX() { return dragOffsetX; }
    public float getDragOffsetY() { return dragOffsetY; }
    public void setDragOffset(float x, float y) { this.dragOffsetX = x; this.dragOffsetY = y; }

    public boolean isHovered(double mouseX, double mouseY) {
        float rx = getRenderX();
        return mouseX >= rx && mouseX <= rx + width && mouseY >= posY && mouseY <= posY + height;
    }

    public boolean isRightAligned() {
        return false;
    }

    public float getRenderX() {
        return posX;
    }

    public float clampDragX(float newX) {
        return Math.max(0, Math.min(mc.getWindow().getScaledWidth() - width, newX));
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettings() { return settings; }

    protected BooleanSetting booleanSetting(String name, boolean defaultValue) {
        return addSetting(new BooleanSetting(name, null, defaultValue));
    }

    protected BooleanSetting booleanSetting(String name, boolean defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new BooleanSetting(name, null, defaultValue, visibility));
    }

    protected IntegerSetting integerSetting(String name, int defaultValue, int min, int max, int step) {
        return addSetting(new IntegerSetting(name, null, defaultValue, min, max, step));
    }

    protected IntegerSetting integerSetting(String name, int defaultValue, int min, int max, int step, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new IntegerSetting(name, null, defaultValue, min, max, step, visibility));
    }

    protected FloatSetting floatSetting(String name, float defaultValue, float min, float max, float step) {
        return addSetting(new FloatSetting(name, null, defaultValue, min, max, step));
    }

    protected FloatSetting floatSetting(String name, float defaultValue, float min, float max, float step, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new FloatSetting(name, null, defaultValue, min, max, step, visibility));
    }

    protected StringSetting stringSetting(String name, String defaultValue) {
        return addSetting(new StringSetting(name, null, defaultValue));
    }

    protected ColorSetting colorSetting(String name, java.awt.Color defaultValue) {
        return addSetting(new ColorSetting(name, null, defaultValue));
    }

    protected ColorSetting colorSetting(String name, java.awt.Color defaultValue, boolean allowAlpha) {
        return addSetting(new ColorSetting(name, null, defaultValue, allowAlpha));
    }

    protected <E extends Enum<E>> EnumSetting<E> enumSetting(String name, E defaultValue) {
        return addSetting(new EnumSetting<>(name, null, defaultValue));
    }

    protected <E extends Enum<E>> EnumSetting<E> enumSetting(String name, E defaultValue, java.util.function.Supplier<Boolean> visibility) {
        return addSetting(new EnumSetting<>(name, null, defaultValue, visibility));
    }
}
