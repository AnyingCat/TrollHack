package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.settings.impl.BindSetting;
import github.trollhack.utils.render.font.FontRenderers;
import org.lwjgl.glfw.GLFW;

public class BindSettingComponent extends SettingComponent {
    private final BindSetting bindSetting;
    private boolean listening = false;

    public BindSettingComponent(BindSetting setting) {
        super(setting, 0, 0, 0);
        this.bindSetting = setting;
    }

    @Override
    protected double getProgressTarget() {
        return listening ? 1.0 : 0.0;
    }

    @Override
    protected String getDisplayText() {
        return bindSetting.getName();
    }

    @Override
    protected String getValueText() {
        if (listening) return "Listening";
        String value = bindSetting.getStringValue();
        if (bindSetting.isHold() && !bindSetting.isEmpty()) {
            return value + " (Hold)";
        }
        return value;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) {
            if (listening && button == 0) {
                listening = false;
            }
            return;
        }
        clicking = true;
        setMouseState(MouseState.CLICK);
        if (button == 0) {
            listening = !listening;
        } else if (button == 1) {
            bindSetting.setHold(!bindSetting.isHold());
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (clicking) {
            clicking = false;
            setMouseState(isHovered(mouseX, mouseY) ? MouseState.HOVER : MouseState.NONE);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listening) return false;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            bindSetting.setValue(-1);
        } else {
            bindSetting.setValue(keyCode);
        }
        listening = false;
        return true;
    }
}
