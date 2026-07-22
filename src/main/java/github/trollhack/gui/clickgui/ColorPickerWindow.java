package github.trollhack.gui.clickgui;

import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.utils.render.Render2DUtil;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ColorPickerWindow {
    private final ColorSetting setting;
    private final float x, y;
    private float width;
    private float height;

    private float hue;
    private float saturation;
    private float brightness;
    private int alphaValue;

    private final int prevR, prevG, prevB, prevA;

    private boolean draggingField = false;
    private boolean draggingHue = false;
    private int draggingSlider = -1;

    private final float sliderWidth = 128.0f;
    private float sliderHeight;
    private final float sliderGap = 2.0f;
    private final float buttonWidth = 50.0f;

    private float fieldPosX, fieldPosY, fieldSize;
    private float huePosX, huePosY, hueWidth, hueHeight;
    private float sliderPosX;
    private float sliderRPosY, sliderGPosY, sliderBPosY, sliderAPosY;
    private float buttonOkayPosY, buttonCancelPosY, buttonApplyPosY;
    private float buttonPosX;
    private float prevColorX1, prevColorY1, prevColorX2, prevColorY2;
    private float currColorX1, currColorY1, currColorX2, currColorY2;

    public ColorPickerWindow(ColorSetting setting, float x, float y) {
        this.setting = setting;
        this.x = x;
        this.y = y;

        Color color = setting.getValue();
        this.prevR = color.getRed();
        this.prevG = color.getGreen();
        this.prevB = color.getBlue();
        this.prevA = color.getAlpha();

        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alphaValue = color.getAlpha();

        updateLayout();
    }

    private void updateLayout() {
        float draggableHeight = FontRenderers.ducksans != null ? FontRenderers.ducksans.getStringHeight(1.0f) + 6.0f : 15.0f;
        sliderHeight = FontRenderers.ducksans != null ? FontRenderers.ducksans.getStringHeight(1.0f) + 3.0f : 12.0f;

        boolean hasAlpha = setting.isAllowAlpha();

        sliderRPosY = draggableHeight;
        sliderGPosY = sliderRPosY + sliderHeight + sliderGap;
        sliderBPosY = sliderGPosY + sliderHeight + sliderGap;
        sliderAPosY = sliderBPosY + sliderHeight + sliderGap;

        float lastSliderPosY = hasAlpha ? sliderAPosY : sliderBPosY;

        buttonOkayPosY = lastSliderPosY + sliderHeight + sliderGap;
        buttonCancelPosY = buttonOkayPosY + sliderHeight + sliderGap;
        buttonApplyPosY = buttonCancelPosY + sliderHeight + sliderGap;

        float forceHeight = buttonApplyPosY + sliderHeight + 4.0f;
        float forceWidth = forceHeight - draggableHeight + 4.0f + 8.0f + 4.0f + sliderWidth + 8.0f;

        this.width = forceWidth;
        this.height = forceHeight;

        sliderPosX = forceWidth - 4.0f - sliderWidth;
        buttonPosX = forceWidth - 4.0f - buttonWidth;

        float fieldHeight = forceHeight - draggableHeight - 4.0f;

        fieldPosX = 4.0f;
        fieldPosY = draggableHeight;
        fieldSize = fieldHeight;

        huePosX = 4.0f + fieldHeight + 6.0f;
        huePosY = draggableHeight;
        hueWidth = 8.0f;
        hueHeight = fieldHeight;

        prevColorX1 = sliderPosX;
        prevColorY1 = buttonOkayPosY;
        prevColorX2 = sliderPosX + 35.0f;
        prevColorY2 = forceHeight - 4.0f;

        currColorX1 = sliderPosX + 35.0f + 4.0f;
        currColorY1 = buttonOkayPosY;
        currColorX2 = sliderPosX + 35.0f + 4.0f + 35.0f;
        currColorY2 = forceHeight - 4.0f;
    }

    public void render(DrawContext context) {
        MatrixStack matrices = context.getMatrices();

        Render2DUtil.drawRect(matrices, x, y, width, height, GuiSetting.INSTANCE.getBackGround());

        {
            float fx = x + fieldPosX;
            float fy = y + fieldPosY;
            Color hueColor = Color.getHSBColor(hue, 1.0f, 1.0f);
            Render2DUtil.drawGradientRect(matrices, fx, fy, fieldSize, fieldSize,
                Color.WHITE, hueColor, Color.WHITE, hueColor);
            Render2DUtil.drawGradientRect(matrices, fx, fy, fieldSize, fieldSize,
                new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), Color.BLACK, Color.BLACK);
            float pointerX = fx + saturation * fieldSize;
            float pointerY = fy + (1.0f - brightness) * fieldSize;
            int val = (int) ((1.0f - (1.0f - saturation) * brightness) * 255.0f);
            Color pointerColor = new Color(val, val, val);
            float circleRadius = 4.0f;
            Render2DUtil.drawRoundedRect(matrices, pointerX - circleRadius, pointerY - circleRadius, circleRadius * 2, circleRadius * 2, circleRadius, pointerColor);
        }

        {
            float hx = x + huePosX;
            float hy = y + huePosY;
            Render2DUtil.drawHueBar(matrices, hx, hy, hueWidth, hueHeight);
            float pointerY = hy + hue * hueHeight;
            Render2DUtil.drawRoundedRect(matrices, hx - 3.0f, pointerY - 2.0f, hueWidth + 6.0f, 4.0f, 2.0f, new Color(255, 255, 255, 255));
        }

        {
            float px1 = x + prevColorX1;
            float py1 = y + prevColorY1;
            float px2 = x + prevColorX2;
            float py2 = y + prevColorY2;
            Render2DUtil.drawRect(matrices, px1, py1, px2 - px1, py2 - py1, new Color(prevR, prevG, prevB, 255));
            float cx1 = x + currColorX1;
            float cy1 = y + currColorY1;
            float cx2 = x + currColorX2;
            float cy2 = y + currColorY2;
            Render2DUtil.drawRect(matrices, cx1, cy1, cx2 - cx1, cy2 - cy1, new Color(getRed(), getGreen(), getBlue(), 255));
        }

        boolean hasAlpha = setting.isAllowAlpha();
        drawSlider(matrices, sliderPosX, sliderRPosY, "Red", getRed(), new Color(getRed(), 0, 0));
        drawSlider(matrices, sliderPosX, sliderGPosY, "Green", getGreen(), new Color(0, getGreen(), 0));
        drawSlider(matrices, sliderPosX, sliderBPosY, "Blue", getBlue(), new Color(0, 0, getBlue()));
        if (hasAlpha) {
            drawSlider(matrices, sliderPosX, sliderAPosY, "Alpha", alphaValue, new Color(255, 255, 255));
        }

        drawButton(matrices, buttonPosX, buttonOkayPosY, "Okay");
        drawButton(matrices, buttonPosX, buttonCancelPosY, "Cancel");
        drawButton(matrices, buttonPosX, buttonApplyPosY, "Apply");

        if (GuiSetting.INSTANCE.windowOutline.getValue()) {
            Color primary = GuiSetting.INSTANCE.getPrimary();
            Render2DUtil.drawRectOutline(matrices, x, y, width, height, 0.5f, new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255));
        }

        if (FontRenderers.ducksans != null) {
            FontRenderers.ducksans.drawText(matrices, "Color Picker", x + 3.0f, y + 3.0f, 1.0f, GuiSetting.INSTANCE.getText());
        }
    }

    private void drawSlider(MatrixStack matrices, float sx, float sy, String name, int value, Color fillColor) {
        float absX = x + sx;
        float absY = y + sy;
        float progress = value / 255.0f;

        Render2DUtil.drawRectOutline(matrices, absX, absY, sliderWidth, sliderHeight, 0.5f, new Color(255, 255, 255, 20));

        if (progress > 0.0f) {
            Render2DUtil.drawRect(matrices, absX, absY, sliderWidth * progress, sliderHeight, fillColor);
        }

        Render2DUtil.drawRect(matrices, absX, absY, sliderWidth, sliderHeight, GuiSetting.INSTANCE.getIdle());

        if (FontRenderers.ducksans != null) {
            FontRenderers.ducksans.drawText(matrices, name, absX + 2.0f, absY + 1.5f, 1.0f, GuiSetting.INSTANCE.getText());
            String valueText = String.valueOf(value);
            float valueWidth = FontRenderers.ducksans.getStringWidth(valueText, 0.75f);
            FontRenderers.ducksans.drawText(matrices, valueText, absX + sliderWidth - valueWidth - 2.0f, absY + sliderHeight - 2.0f - FontRenderers.ducksans.getStringHeight(0.75f), 0.75f, GuiSetting.INSTANCE.getText());
        }
    }

    private void drawButton(MatrixStack matrices, float bx, float by, String text) {
        float absX = x + bx;
        float absY = y + by;

        Render2DUtil.drawRectOutline(matrices, absX, absY, buttonWidth, sliderHeight, 0.5f, new Color(255, 255, 255, 20));
        Render2DUtil.drawRect(matrices, absX, absY, buttonWidth, sliderHeight, GuiSetting.INSTANCE.getIdle());

        if (FontRenderers.ducksans != null) {
            float tw = FontRenderers.ducksans.getStringWidth(text, 0.75f);
            float tx = absX + (buttonWidth - tw) / 2.0f;
            float ty = absY + (sliderHeight - FontRenderers.ducksans.getStringHeight(0.75f)) / 2.0f;
            FontRenderers.ducksans.drawText(matrices, text, tx, ty, 0.75f, GuiSetting.INSTANCE.getText());
        }
    }

    private int getRed() {
        return Color.getHSBColor(hue, saturation, brightness).getRed();
    }

    private int getGreen() {
        return Color.getHSBColor(hue, saturation, brightness).getGreen();
    }

    private int getBlue() {
        return Color.getHSBColor(hue, saturation, brightness).getBlue();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return;

        float relX = (float) mouseX - x;
        float relY = (float) mouseY - y;

        if (relX >= fieldPosX && relX <= fieldPosX + fieldSize && relY >= fieldPosY && relY <= fieldPosY + fieldSize) {
            draggingField = true;
            updateFieldFromMouse(relX, relY);
            return;
        }

        if (relX >= huePosX - 5.0f && relX <= huePosX + hueWidth + 5.0f && relY >= huePosY && relY <= huePosY + hueHeight) {
            draggingHue = true;
            updateHueFromMouse(relY);
            return;
        }

        if (relX >= sliderPosX && relX <= sliderPosX + sliderWidth) {
            boolean hasAlpha = setting.isAllowAlpha();
            if (relY >= sliderRPosY && relY <= sliderRPosY + sliderHeight) { draggingSlider = 0; updateSliderFromMouse(0, relX); return; }
            if (relY >= sliderGPosY && relY <= sliderGPosY + sliderHeight) { draggingSlider = 1; updateSliderFromMouse(1, relX); return; }
            if (relY >= sliderBPosY && relY <= sliderBPosY + sliderHeight) { draggingSlider = 2; updateSliderFromMouse(2, relX); return; }
            if (hasAlpha && relY >= sliderAPosY && relY <= sliderAPosY + sliderHeight) { draggingSlider = 3; updateSliderFromMouse(3, relX); return; }
        }

        if (relX >= buttonPosX && relX <= buttonPosX + buttonWidth) {
            if (relY >= buttonOkayPosY && relY <= buttonOkayPosY + sliderHeight) {
                setting.setFromRGB(getRed(), getGreen(), getBlue(), alphaValue);
                ClickGUIScreen.getInstance().closeColorPicker();
                return;
            }
            if (relY >= buttonCancelPosY && relY <= buttonCancelPosY + sliderHeight) {
                setting.setFromRGB(prevR, prevG, prevB, prevA);
                ClickGUIScreen.getInstance().cancelColorPicker();
                return;
            }
            if (relY >= buttonApplyPosY && relY <= buttonApplyPosY + sliderHeight) {
                setting.setFromRGB(getRed(), getGreen(), getBlue(), alphaValue);
            }
        }
    }

    public void mouseDragged(double mouseX, double mouseY) {
        float relX = (float) mouseX - x;
        float relY = (float) mouseY - y;

        if (draggingField) {
            updateFieldFromMouse(relX, relY);
        } else if (draggingHue) {
            updateHueFromMouse(relY);
        } else if (draggingSlider >= 0) {
            updateSliderFromMouse(draggingSlider, relX);
        }
    }

    public void mouseReleased() {
        draggingField = false;
        draggingHue = false;
        draggingSlider = -1;
    }

    private void updateFieldFromMouse(float relX, float relY) {
        float localX = relX - fieldPosX;
        float localY = relY - fieldPosY;
        saturation = Math.max(0.0f, Math.min(1.0f, localX / fieldSize));
        brightness = Math.max(0.0f, Math.min(1.0f, 1.0f - localY / fieldSize));
    }

    private void updateHueFromMouse(float relY) {
        float localY = relY - huePosY;
        hue = Math.max(0.0f, Math.min(1.0f, localY / hueHeight));
    }

    private void updateSliderFromMouse(int index, float relX) {
        float progress = Math.max(0.0f, Math.min(1.0f, (relX - sliderPosX) / sliderWidth));
        int value = Math.round(progress * 255);

        int r = getRed();
        int g = getGreen();
        int b = getBlue();

        switch (index) {
            case 0 -> r = value;
            case 1 -> g = value;
            case 2 -> b = value;
            case 3 -> { alphaValue = value; return; }
        }

        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    public void cancel() {
        setting.setFromRGB(prevR, prevG, prevB, prevA);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}