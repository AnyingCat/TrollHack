package github.trollhack.modules.impl.client;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;

import java.awt.Color;

public class GuiSetting extends Module {
    public static final GuiSetting INSTANCE = new GuiSetting();

    public final FloatSetting backgroundBlur = floatSetting("Background Blur", 0.5f, 0.0f, 1.0f, 0.05f);
    public final BooleanSetting windowOutline = booleanSetting("Window Outline", true);
    public final BooleanSetting titleBar = booleanSetting("Title Bar", false);
    public final FloatSetting xMargin = floatSetting("X Margin", 2.0f, 0.0f, 10.0f, 0.5f);
    public final FloatSetting yMargin = floatSetting("Y Margin", 1.5f, 0.0f, 10.0f, 0.5f);
    public final ColorSetting primaryColor = colorSetting("Primary Color", new Color(255, 140, 180, 220));
    public final ColorSetting backgroundColor = colorSetting("Background Color", new Color(0, 0, 0, 160));
    public final ColorSetting textColor = colorSetting("Text Color", new Color(255, 250, 253, 255));
    public final IntegerSetting hoverAlpha = integerSetting("Hover Alpha", 32, 0, 255, 1);

    public GuiSetting() {
        super("Gui Setting", Category.CLIENT);
        setAlwaysEnabled();
    }

    public Color getPrimary() {
        return primaryColor.getValue();
    }

    public Color getIdle() {
        Color primary = getPrimary();
        float r = primary.getRed() / 255.0f;
        float g = primary.getGreen() / 255.0f;
        float b = primary.getBlue() / 255.0f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float lightness = (max + min) / 2.0f;
        return lightness < 0.9f ? new Color(255, 255, 255, 0) : new Color(0, 0, 0, 0);
    }

    public Color getHover() {
        Color idle = getIdle();
        return new Color(idle.getRed(), idle.getGreen(), idle.getBlue(), hoverAlpha.getValue());
    }

    public Color getClick() {
        Color hover = getHover();
        return new Color(hover.getRed(), hover.getGreen(), hover.getBlue(), hoverAlpha.getValue() * 2);
    }

    public Color getBackGround() {
        return backgroundColor.getValue();
    }

    public Color getText() {
        return textColor.getValue();
    }
}
