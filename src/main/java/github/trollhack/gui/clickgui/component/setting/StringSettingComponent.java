package github.trollhack.gui.clickgui.component.setting;

import github.trollhack.gui.clickgui.component.SettingComponent;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.StringSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class StringSettingComponent extends SettingComponent {
    private final StringSetting stringSetting;
    private boolean editing = false;
    private String tempText = "";
    private int cursorPos = 0;
    private long lastBlinkTime = 0;
    private boolean cursorVisible = true;

    public StringSettingComponent(StringSetting setting) {
        super(setting, 0, 0, 0);
        this.stringSetting = setting;
    }

    @Override
    protected double getProgressTarget() {
        return editing ? 0.0 : 1.0;
    }

    @Override
    protected String getDisplayText() {
        return editing ? tempText : stringSetting.getName();
    }

    @Override
    protected String getValueText() {
        return editing ? null : stringSetting.getValue();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        updateProtectedWidth();
        updateMouseState(mouseX, mouseY);
        progressAnimation.setTarget(getProgressTarget());
        progressAnimation.update();

        MatrixStack matrices = context.getMatrices();
        float progress = progressAnimation.getValueFloat();

        Render2DUtil.drawRect(matrices, x, y, width, height, new Color(0, 0, 0, 40));

        if (progress > 0.0f) {
            Render2DUtil.drawRect(matrices, x, y, width * progress, height, GuiSetting.INSTANCE.getPrimary());
        }

        Render2DUtil.drawRect(matrices, x, y, width, height, getOverlayColor());

        renderMainText(matrices);

        if (editing && cursorVisible && FontRenderers.ducksans != null) {
            float scale = getHoverScale();
            float clickedScale = getClickedScale();
            float textX = x + 2.0f + 2.0f * scale;
            float textScale = 1.0f + 0.05f * scale - 0.1f * clickedScale;
            String beforeCursor = tempText.substring(0, Math.min(cursorPos, tempText.length()));
            float cursorX = textX + FontRenderers.ducksans.getStringWidth(beforeCursor, textScale);
            float textY = y + 1.5f - 0.025f * scale * getFontHeight() + 0.05f * clickedScale * getFontHeight();
            float cursorHeight = FontRenderers.ducksans.getStringHeight(textScale);
            Render2DUtil.drawRect(matrices, cursorX, textY, 1.0f, cursorHeight, GuiSetting.INSTANCE.getText());
        }

        if (!editing) {
            renderValue(matrices);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlinkTime > 530) {
            cursorVisible = !cursorVisible;
            lastBlinkTime = currentTime;
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return;
        boolean hovered = isHovered(mouseX, mouseY);
        if (hovered && button == 0) {
            clicking = true;
            setMouseState(MouseState.CLICK);
            if (!editing) {
                editing = true;
                tempText = stringSetting.getValue();
                cursorPos = tempText.length();
                lastBlinkTime = System.currentTimeMillis();
                cursorVisible = true;
            } else {
                finishEditing();
            }
            return;
        }
        if (editing && button == 0 && !hovered) {
            finishEditing();
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
        if (!editing) return false;
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                finishEditing();
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                editing = false;
                setMouseState(MouseState.NONE);
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (cursorPos > 0) {
                    tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
                    cursorPos--;
                    resetCursor();
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (cursorPos < tempText.length()) {
                    tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
                    resetCursor();
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursorPos > 0) {
                    cursorPos--;
                    resetCursor();
                }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursorPos < tempText.length()) {
                    cursorPos++;
                    resetCursor();
                }
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursorPos = 0;
                resetCursor();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                cursorPos = tempText.length();
                resetCursor();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!editing) return false;
        if (chr == '\n' || chr == '\r' || chr == '\t') return false;
        tempText = tempText.substring(0, cursorPos) + chr + tempText.substring(cursorPos);
        cursorPos++;
        resetCursor();
        return true;
    }

    private void finishEditing() {
        editing = false;
        if (!tempText.isEmpty()) {
            stringSetting.setValue(tempText);
        }
        setMouseState(MouseState.NONE);
    }

    private void resetCursor() {
        lastBlinkTime = System.currentTimeMillis();
        cursorVisible = true;
    }

    private void updateProtectedWidth() {
        if (FontRenderers.ducksans == null) {
            protectedWidth = 0.0f;
            return;
        }
        String valueText = stringSetting.getValue();
        if (valueText == null || valueText.isEmpty()) {
            protectedWidth = 0.0f;
            return;
        }
        protectedWidth = FontRenderers.ducksans.getStringWidth(valueText, 0.75f);
    }
}
