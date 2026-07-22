package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;

public class EnumSettingComponent<E extends Enum<E>> extends SettingComponent {
    private final EnumSetting<E> enumSetting;
    private final E[] enumValues;
    private float dragProgress = 0.0f;

    public EnumSettingComponent(EnumSetting<E> setting) {
        super(setting, 0, 0, 0);
        this.enumSetting = setting;
        this.enumValues = setting.getValues();
        updateProtectedWidth();
    }

    private void updateProtectedWidth() {
        if (FontRenderers.ducksans == null) {
            protectedWidth = 0.0f;
            return;
        }
        String valueText = enumSetting.getValue().name();
        protectedWidth = FontRenderers.ducksans.getStringWidth(valueText, 0.75f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateProtectedWidth();
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected double getProgressTarget() {
        if (dragging) return dragProgress;
        int total = enumValues.length;
        if (total <= 1) return 0.0;
        int ordinal = enumSetting.getValue().ordinal();
        return (ordinal + ordinal / (double) (total - 1)) / total;
    }

    @Override
    protected String getDisplayText() {
        return enumSetting.getName();
    }

    @Override
    protected String getValueText() {
        return enumSetting.getValue().name();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) return;
        if (button == 0) {
            clicking = true;
            dragging = true;
            setMouseState(MouseState.CLICK);
            dragProgress = (float) ((mouseX - x) / width);
            dragProgress = Math.max(0.0f, Math.min(1.0f, dragProgress));
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (mouseState == MouseState.CLICK || mouseState == MouseState.DRAG)) {
            boolean wasDragging = mouseState == MouseState.DRAG;
            clicking = false;
            dragging = false;
            setMouseState(isHovered(mouseX, mouseY) ? MouseState.HOVER : MouseState.NONE);
            if (!wasDragging) {
                enumSetting.cycle();
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!visible) return;
        if (dragging && button == 0) {
            setMouseState(MouseState.DRAG);
            dragProgress = (float) ((mouseX - x) / width);
            dragProgress = Math.max(0.0f, Math.min(1.0f, dragProgress));
            int index = Math.max(0, Math.min((int) Math.floor(dragProgress * enumValues.length), enumValues.length - 1));
            if (index >= 0 && index < enumValues.length) {
                enumSetting.setValue(enumValues[index]);
            }
        }
    }
}
