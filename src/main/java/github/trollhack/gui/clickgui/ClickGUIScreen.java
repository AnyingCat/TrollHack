package github.trollhack.gui.clickgui;

import github.trollhack.core.Managers;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.ClickGUI;
import github.trollhack.settings.impl.ColorSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static github.trollhack.gui.clickgui.component.Component.mc;

public class ClickGUIScreen extends Screen {
    public static ClickGUIScreen INSTANCE;

    private final List<CategoryPanel> panels = new ArrayList<>();
    private SettingPanel settingPanel = null;
    private ColorPickerWindow colorPicker = null;
    private boolean initialized = false;

    public ClickGUIScreen() {
        super(Text.literal("ClickGUI"));
        INSTANCE = this;
    }

    public static ClickGUIScreen getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ClickGUIScreen();
        }
        return INSTANCE;
    }

    @Override
    protected void init() {
        super.init();
        if (!initialized) {
            panels.clear();
            float posX = 0.0f;
            float posY = 0.0f;
            float spacing = 2.0f;
            float rowHeight = 400.0f + spacing;
            for (Category category : Category.values()) {
                CategoryPanel panel = new CategoryPanel(
                    category,
                    Managers.MODULE,
                    posX,
                    posY
                );
                panels.add(panel);
                posX += panel.getWidth() + spacing;

                if (posX + panel.getWidth() > mc.getWindow().getScaledWidth()) {
                    posX = 0.0f;
                    posY += rowHeight;
                }
            }
            initialized = true;
        }
    }

    public void openSettingPanel(Module module, float mouseX, float mouseY) {
        settingPanel = new SettingPanel(module, mouseX, mouseY);
    }

    public void closeSettingPanel() {
        settingPanel = null;
    }

    public void openColorPicker(ColorSetting setting, float mouseX, float mouseY) {
        colorPicker = new ColorPickerWindow(setting, mouseX, mouseY);
    }

    public void closeColorPicker() {
        colorPicker = null;
    }

    public void cancelColorPicker() {
        if (colorPicker != null) {
            colorPicker.cancel();
            colorPicker = null;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (ClickGUI.INSTANCE.blur.getValue()) {
            applyBlur(delta);
        }
        if (ClickGUI.INSTANCE.background.getValue()) {
            renderBackground(context, mouseX, mouseY, delta);
        }
        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, delta);
        }
        if (settingPanel != null) {
            settingPanel.render(context, mouseX, mouseY, delta);
        }
        if (colorPicker != null) {
            colorPicker.render(context);
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (colorPicker != null) {
            if (colorPicker.isHovered(mouseX, mouseY)) {
                colorPicker.mouseClicked(mouseX, mouseY, button);
                return true;
            } else {
                cancelColorPicker();
                return true;
            }
        }

        if (settingPanel != null) {
            if (settingPanel.isHovered(mouseX, mouseY)) {
                settingPanel.mouseClicked(mouseX, mouseY, button);
            } else {
                closeSettingPanel();
            }
            return true;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = panels.get(i);
            if (panel.isHovered(mouseX, mouseY)) {
                panels.remove(i);
                panels.add(panel);
                panel.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (colorPicker != null) {
            colorPicker.mouseReleased();
        }
        if (settingPanel != null) {
            settingPanel.mouseReleased(mouseX, mouseY, button);
        }
        for (CategoryPanel panel : panels) {
            panel.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (colorPicker != null) {
            colorPicker.mouseDragged(mouseX, mouseY);
        }
        if (settingPanel != null) {
            settingPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        for (CategoryPanel panel : panels) {
            panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingPanel != null && settingPanel.isHovered(mouseX, mouseY)) {
            settingPanel.handleScroll(verticalAmount);
            return true;
        }
        for (CategoryPanel panel : panels) {
            if (panel.isHovered(mouseX, mouseY)) {
                panel.handleScroll(verticalAmount);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (colorPicker != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                cancelColorPicker();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                closeColorPicker();
                return true;
            }
        }
        if (settingPanel != null) {
            if (settingPanel.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                closeSettingPanel();
                return true;
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            ClickGUI.INSTANCE.setEnabled(false);
            return true;
        }
        for (CategoryPanel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (settingPanel != null) {
            if (settingPanel.charTyped(chr, modifiers)) {
                return true;
            }
        }
        for (CategoryPanel panel : panels) {
            if (panel.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public List<CategoryPanel> getPanels() {
        return panels;
    }
}
