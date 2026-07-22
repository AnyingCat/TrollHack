package github.trollhack.settings.impl;

import github.trollhack.modules.Module;
import github.trollhack.settings.Setting;

import java.awt.*;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Color> {
    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;
    private float alpha = 1;
    private final boolean allowAlpha;
    private final Color defaultColor;

    public ColorSetting(String name, Module module, Color defaultValue, Supplier<Boolean> visibility) {
        super(name, module, defaultValue, visibility);
        this.defaultColor = defaultValue;
        this.allowAlpha = true;
        set(defaultValue);
    }

    public ColorSetting(String name, Module module, Color defaultValue) {
        this(name, module, defaultValue, null);
    }

    public ColorSetting(String name, Module module, Color defaultValue, boolean allowAlpha) {
        super(name, module, defaultValue, null);
        this.defaultColor = defaultValue;
        this.allowAlpha = allowAlpha;
        set(defaultValue);
    }

    @Override
    public void setValue(Color color) {
        set(color);
    }

    @Override
    public Color getValue() {
        return get();
    }

    public Color get() {
        Color hsbColor = Color.getHSBColor(hue, saturation, brightness);
        return new Color(hsbColor.getRed(), hsbColor.getGreen(), hsbColor.getBlue(), (int) (alpha * 255));
    }

    public void set(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = color.getAlpha() / 255.0f;
    }

    public boolean isAllowAlpha() {
        return allowAlpha;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public float getAlphaFloat() {
        return alpha;
    }

    public int getAlpha() {
        return (int) (alpha * 255);
    }

    public void setHue(float hue) {
        this.hue = Math.max(0, Math.min(1, hue));
    }

    public void setSaturation(float saturation) {
        this.saturation = Math.max(0, Math.min(1, saturation));
    }

    public void setBrightness(float brightness) {
        this.brightness = Math.max(0, Math.min(1, brightness));
    }

    public void setAlphaFloat(float alpha) {
        this.alpha = Math.max(0, Math.min(1, alpha));
    }

    public void setFromRGB(int r, int g, int b, int a) {
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = a / 255.0f;
    }

    public int getRed() {
        return getValue().getRed();
    }

    public int getGreen() {
        return getValue().getGreen();
    }

    public int getBlue() {
        return getValue().getBlue();
    }

    public String getHex() {
        Color c = getValue();
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    public String getStringValue() {
        return getHex();
    }
}
