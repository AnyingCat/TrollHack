package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.Setting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class NumberSettingComponent extends SettingComponent {
    private final double range;
    private final double step;
    private final double fineStep;
    private final int decimalPlaces;

    private double preDragMouseX = 0.0;
    private boolean editing = false;
    private String inputField = "";

    public NumberSettingComponent(Setting<?> setting) {
        super(setting, 0, 0, 0);
        this.range = getMax() - getMin();
        this.step = getStep();
        this.fineStep = step / 10.0;
        if (setting instanceof IntegerSetting) {
            this.decimalPlaces = 0;
        } else {
            this.decimalPlaces = Math.max(1, (int) Math.ceil(-Math.log10(step)));
        }
    }

    @Override
    protected double getProgressTarget() {
        if (range <= 0) return 0.0;
        return (getValue() - getMin()) / range;
    }

    @Override
    protected String getDisplayText() {
        return editing ? inputField : setting.getName();
    }

    @Override
    protected String getValueText() {
        if (editing) return null;
        return formatValue(getValue());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        updateProtectedWidth();
        updateMouseState(mouseX, mouseY);
        
        if (dragging && !editing) {
            updateValue(mouseX);
        } else {
            progressAnimation.setTarget(getProgressTarget());
            progressAnimation.update();
        }

        MatrixStack matrices = context.getMatrices();
        float progress = progressAnimation.getValueFloat();

        Render2DUtil.drawRect(matrices, x, y, width, height, new Color(0, 0, 0, 40));

        if (progress > 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width * progress, height, GuiSetting.INSTANCE.getPrimary());
        }

        if (editing) {
            Render2DUtil.drawRect(matrices, x, y, width, height, new Color(50, 50, 70, 180));
        } else {
            Render2DUtil.drawRect(matrices, x, y, width, height, getOverlayColor());
        }

        renderMainText(matrices);
        if (!editing) {
            renderValue(matrices);
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        if (!isHovered(mouseX, mouseY)) {
            if (editing && button == 0) {
                finishEditing(true);
            }
            return;
        }
        clicking = true;
        setMouseState(MouseState.CLICK);
        if (button == 0) {
            if (editing) {
                finishEditing(true);
            } else {
                dragging = true;
                preDragMouseX = mouseX;
                updateValue(mouseX);
            }
        } else if (button == 1) {
            if (!editing) {
                editing = true;
                inputField = formatValue(getValue());
            } else {
                finishEditing(false);
            }
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            clicking = false;
            setMouseState(isHovered(mouseX, mouseY) ? MouseState.HOVER : MouseState.NONE);
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!visible) return;
        if (dragging && button == 0 && !editing) {
            updateValue(mouseX);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!editing) return false;
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                finishEditing(true);
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                finishEditing(false);
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!inputField.isEmpty()) {
                    inputField = inputField.substring(0, inputField.length() - 1);
                    if (inputField.isEmpty()) inputField = "0";
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                inputField = "0";
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!editing) return false;
        if (Character.isDigit(chr) || chr == '-' || chr == '.' || chr == 'e' || chr == 'E') {
            if (inputField.equals("0") && (Character.isDigit(chr) || chr == '-')) {
                inputField = "";
            }
            inputField += chr;
            return true;
        }
        return false;
    }

    private void updateValue(double mouseX) {
        boolean isAltPressed = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                               GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
        double effectiveStep = isAltPressed ? fineStep : step;
        double value;
        if (!isAltPressed) {
            value = (mouseX - x) / width;
        } else {
            value = (preDragMouseX + (mouseX - preDragMouseX) * 0.1) / width;
        }
        double roundedValue = Math.round((value * range + getMin()) / effectiveStep) * effectiveStep;
        if (Math.abs(roundedValue) < 0.0001) roundedValue = 0.0;
        roundedValue = clamp(roundedValue, getMin(), getMax());
        setValue(roundedValue);
        progressAnimation.setTo((roundedValue - getMin()) / range);
    }

    private void finishEditing(boolean success) {
        editing = false;
        if (success) {
            try {
                double value = Double.parseDouble(inputField);
                value = clamp(value, getMin(), getMax());
                setValue(value);
            } catch (NumberFormatException ignored) {
            }
        }
        inputField = "";
        setMouseState(MouseState.NONE);
    }

    private double getValue() {
        if (setting instanceof FloatSetting fs) return fs.getValue();
        if (setting instanceof IntegerSetting is) return is.getValue();
        return 0;
    }

    private double getMin() {
        if (setting instanceof FloatSetting fs) return fs.getMin();
        if (setting instanceof IntegerSetting is) return is.getMin();
        return 0;
    }

    private double getMax() {
        if (setting instanceof FloatSetting fs) return fs.getMax();
        if (setting instanceof IntegerSetting is) return is.getMax();
        return 100;
    }

    private double getStep() {
        if (setting instanceof FloatSetting fs) return fs.getStep();
        if (setting instanceof IntegerSetting is) return is.getStep();
        return 1;
    }

    private void setValue(double value) {
        if (setting instanceof FloatSetting fs) {
            fs.setValue((float) value);
        } else if (setting instanceof IntegerSetting is) {
            is.setValue((int) Math.round(value));
        }
    }

    private String formatValue(double value) {
        if (setting instanceof IntegerSetting) {
            return String.valueOf((int) Math.round(value));
        }
        if (decimalPlaces <= 0) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format("%." + decimalPlaces + "f", value);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateProtectedWidth() {
        if (FontRenderers.ducksans == null) {
            protectedWidth = 0.0f;
            return;
        }
        String valueText = formatValue(getValue());
        protectedWidth = FontRenderers.ducksans.getStringWidth(valueText, 0.75f);
    }
}
