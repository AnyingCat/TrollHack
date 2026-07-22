package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.ClickGUIScreen;
import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ColorSettingComponent extends SettingComponent {
    private final ColorSetting colorSetting;

    public ColorSettingComponent(ColorSetting setting) {
        super(setting, 0, 0, 0);
        this.colorSetting = setting;
        updateProtectedWidth();
    }

    private void updateProtectedWidth() {
        if (FontRenderers.ducksans == null) {
            protectedWidth = 0.0f;
            return;
        }
        float colorBoxSize = FontRenderers.ducksans.getStringHeight(1.0f);
        protectedWidth = colorBoxSize + 6.0f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateProtectedWidth();
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected double getProgressTarget() {
        return 0.0;
    }

    @Override
    protected String getDisplayText() {
        return colorSetting.getName();
    }

    @Override
    protected String getValueText() {
        return null;
    }

    @Override
    protected void renderValue(MatrixStack matrices) {
        if (FontRenderers.ducksans == null) return;
        Color currentColor = colorSetting.getValue();
        float colorBoxSize = FontRenderers.ducksans.getStringHeight(1.0f);
        float colorBoxX = x + width - colorBoxSize - 4.0f;
        float colorBoxY = y + (height - colorBoxSize) / 2.0f;
        Render2DUtil.drawRect(matrices, colorBoxX - 1, colorBoxY - 1, colorBoxSize + 2, colorBoxSize + 2, new Color(0, 0, 0, 80));
        Render2DUtil.drawRect(matrices, colorBoxX, colorBoxY, colorBoxSize, colorBoxSize, currentColor);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) return;
        clicking = true;
        setMouseState(MouseState.CLICK);
        if (button == 1) {
            ClickGUIScreen.getInstance().openColorPicker(colorSetting, (float) mouseX, (float) mouseY);
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
