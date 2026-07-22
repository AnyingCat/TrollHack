package github.trollhack.gui.clickgui.component;

import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.Setting;
import github.trollhack.utils.animation.AnimatedValue;
import github.trollhack.utils.animation.AnimationUtil;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public abstract class SettingComponent extends Component {
    protected final AnimatedValue progressAnimation;
    protected final Setting<?> setting;
    protected float protectedWidth = 0.0f;
    protected float clipX, clipY, clipW, clipH;
    protected boolean clipped = false;

    protected SettingComponent(float x, float y, float width) {
        super(x, y, width, getFontHeight() + 3.0f);
        this.setting = null;
        this.progressAnimation = new AnimatedValue(0.0, 300, AnimationUtil.Easing.EASE_OUT_QUART);
    }

    protected SettingComponent(Setting<?> setting, float x, float y, float width) {
        super(x, y, width, getFontHeight() + 3.0f);
        this.setting = setting;
        this.progressAnimation = new AnimatedValue(0.0, 300, AnimationUtil.Easing.EASE_OUT_QUART);
    }

    protected SettingComponent(Setting<?> setting, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.setting = setting;
        this.progressAnimation = new AnimatedValue(0.0, 300, AnimationUtil.Easing.EASE_OUT_QUART);
    }

    @Override
    public boolean isVisible() {
        return visible && (setting == null || setting.isVisible());
    }

    protected static float getFontHeight() {
        if (FontRenderers.ducksans == null) return 9.0f;
        return FontRenderers.ducksans.getStringHeight(1.0f);
    }

    protected abstract double getProgressTarget();

    protected abstract String getDisplayText();

    protected String getValueText() {
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;
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
        renderValue(matrices);
    }

    public void setClip(float x, float y, float w, float h) {
        this.clipX = x;
        this.clipY = y;
        this.clipW = w;
        this.clipH = h;
        this.clipped = true;
    }

    protected void renderMainText(MatrixStack matrices) {
        if (FontRenderers.ducksans == null) return;
        float scale = getHoverScale();
        float clickedScale = getClickedScale();
        float fontHeight = getFontHeight();
        float textX = x + 2.0f + 2.0f * scale;
        float textY = y + 1.5f - 0.025f * scale * fontHeight + 0.05f * clickedScale * fontHeight;
        float textScale = 1.0f + 0.05f * scale - 0.1f * clickedScale;
        if (clipped) {
            FontRenderers.ducksans.drawText(matrices, getDisplayText(), textX, textY, textScale, GuiSetting.INSTANCE.getText(), clipX, clipY, clipW, clipH);
        } else {
            FontRenderers.ducksans.drawText(matrices, getDisplayText(), textX, textY, textScale, GuiSetting.INSTANCE.getText());
        }
    }

    protected void renderValue(MatrixStack matrices) {
        if (FontRenderers.ducksans == null) return;
        String valueText = getValueText();
        if (valueText == null || valueText.isEmpty()) return;
        float valueScale = 0.75f;
        float valueWidth = FontRenderers.ducksans.getStringWidth(valueText, valueScale);
        float posX = x + width - valueWidth - 2.0f;
        float posY = y + height - 2.0f - FontRenderers.ducksans.getStringHeight(valueScale);
        if (clipped) {
            FontRenderers.ducksans.drawText(matrices, valueText, posX, posY, valueScale, GuiSetting.INSTANCE.getText(), clipX, clipY, clipW, clipH);
        } else {
            FontRenderers.ducksans.drawText(matrices, valueText, posX, posY, valueScale, GuiSetting.INSTANCE.getText());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public float getMinWidth() {
        if (FontRenderers.ducksans == null) return 80.0f;
        return FontRenderers.ducksans.getStringWidth(getDisplayText(), 1.0f) + 20.0f + protectedWidth;
    }
}
