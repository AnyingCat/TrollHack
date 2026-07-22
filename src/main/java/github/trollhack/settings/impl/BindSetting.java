package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.util.function.Supplier;

public class BindSetting extends Setting<Integer> {
    private boolean isHold;

    public BindSetting(String name, Module module, int defaultValue, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
        this.isHold = false;
    }

    public BindSetting(String name, Module module, int defaultValue) {
        this(name, module, defaultValue, null);
    }

    public boolean isHold() {
        return isHold;
    }

    public void setHold(boolean hold) {
        isHold = hold;
    }

    public boolean isEmpty() {
        return getValue() == -1;
    }

    @Override
    public String getStringValue() {
        return isEmpty() ? "None" : getKeyName(getValue());
    }

    public static String getKeyName(int keyCode) {
        return switch (keyCode) {
            case -1 -> "None";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_TAB -> "Tab";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER -> "Enter";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE -> "Delete";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT -> "Insert";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE -> "Space";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_UP -> "Up";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN -> "Down";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT -> "Left";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT -> "Right";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_CAPS_LOCK -> "CapsLock";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_NUM_LOCK -> "NumLock";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_SCROLL_LOCK -> "ScrollLock";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP -> "PageUp";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN -> "PageDown";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_HOME -> "Home";
            case org.lwjgl.glfw.GLFW.GLFW_KEY_END -> "End";
            default -> {
                String name = org.lwjgl.glfw.GLFW.glfwGetKeyName(keyCode, 0);
                yield name != null ? name.toUpperCase() : "Key" + keyCode;
            }
        };
    }
}
