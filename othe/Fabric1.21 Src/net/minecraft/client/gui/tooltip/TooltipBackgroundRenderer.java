/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;

/**
 * A class for rendering a background box for a tooltip.
 */
@Environment(value=EnvType.CLIENT)
public class TooltipBackgroundRenderer {
    public static final int field_41688 = 12;
    private static final int field_41693 = 3;
    public static final int field_41689 = 3;
    public static final int field_41690 = 3;
    public static final int field_41691 = 3;
    public static final int field_41692 = 3;
    private static final int BACKGROUND_COLOR = -267386864;
    private static final int START_Y_BORDER_COLOR = 0x505000FF;
    private static final int END_Y_BORDER_COLOR = 1344798847;

    public static void render(DrawContext context, int x, int y, int width, int height, int z) {
        int i = x - 3;
        int j = y - 3;
        int k = width + 3 + 3;
        int l = height + 3 + 3;
        TooltipBackgroundRenderer.renderHorizontalLine(context, i, j - 1, k, z, -267386864);
        TooltipBackgroundRenderer.renderHorizontalLine(context, i, j + l, k, z, -267386864);
        TooltipBackgroundRenderer.renderRectangle(context, i, j, k, l, z, -267386864);
        TooltipBackgroundRenderer.renderVerticalLine(context, i - 1, j, l, z, -267386864);
        TooltipBackgroundRenderer.renderVerticalLine(context, i + k, j, l, z, -267386864);
        TooltipBackgroundRenderer.renderBorder(context, i, j + 1, k, l, z, 0x505000FF, 1344798847);
    }

    private static void renderBorder(DrawContext context, int x, int y, int width, int height, int z, int startColor, int endColor) {
        TooltipBackgroundRenderer.renderVerticalLine(context, x, y, height - 2, z, startColor, endColor);
        TooltipBackgroundRenderer.renderVerticalLine(context, x + width - 1, y, height - 2, z, startColor, endColor);
        TooltipBackgroundRenderer.renderHorizontalLine(context, x, y - 1, width, z, startColor);
        TooltipBackgroundRenderer.renderHorizontalLine(context, x, y - 1 + height - 1, width, z, endColor);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int color) {
        context.fill(x, y, x + 1, y + height, z, color);
    }

    private static void renderVerticalLine(DrawContext context, int x, int y, int height, int z, int startColor, int endColor) {
        context.fillGradient(x, y, x + 1, y + height, z, startColor, endColor);
    }

    private static void renderHorizontalLine(DrawContext context, int x, int y, int width, int z, int color) {
        context.fill(x, y, x + width, y + 1, z, color);
    }

    private static void renderRectangle(DrawContext context, int x, int y, int width, int height, int z, int color) {
        context.fill(x, y, x + width, y + height, z, color);
    }
}

