package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.settings.impl.BooleanSetting;

public class BooleanSettingComponent extends SettingComponent {
    private final BooleanSetting boolSetting;

    public BooleanSettingComponent(BooleanSetting setting) {
        super(setting, 0, 0, 0);
        this.boolSetting = setting;
    }

    @Override
    protected double getProgressTarget() {
        return boolSetting.getValue() ? 1.0 : 0.0;
    }

    @Override
    protected String getDisplayText() {
        return boolSetting.getName();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) return;
        clicking = true;
        setMouseState(MouseState.CLICK);
        if (button == 0) {
            boolSetting.setValue(!boolSetting.getValue());
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
}
