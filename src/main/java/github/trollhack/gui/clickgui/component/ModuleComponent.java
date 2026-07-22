package github.trollhack.gui.clickgui.component;

import github.trollhack.gui.clickgui.ClickGUIScreen;
import github.trollhack.modules.Module;
import github.trollhack.utils.render.font.FontRenderers;

public class ModuleComponent extends SettingComponent {
    private final Module module;

    public ModuleComponent(Module module, float x, float y, float width) {
        super(x, y, width);
        this.module = module;
    }

    @Override
    protected double getProgressTarget() {
        return module.isEnabled() ? 1.0 : 0.0;
    }

    @Override
    protected String getDisplayText() {
        return module.getName();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) return;
        clicking = true;
        setMouseState(MouseState.CLICK);
        if (button == 0) {
            module.toggle();
        } else if (button == 1 && !module.getSettings().isEmpty()) {
            ClickGUIScreen.getInstance().openSettingPanel(module, (float) mouseX, (float) mouseY);
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

    public Module getModule() {
        return module;
    }

    @Override
    public float getMinWidth() {
        if (FontRenderers.ducksans == null) return 80.0f;
        return FontRenderers.ducksans.getStringWidth(module.getName(), 1.0f) + 20.0f;
    }
}
