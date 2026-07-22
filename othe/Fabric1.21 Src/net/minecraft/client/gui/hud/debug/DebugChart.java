/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.hud.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.log.MultiValueDebugSampleLog;

@Environment(value=EnvType.CLIENT)
public abstract class DebugChart {
    protected static final int TEXT_COLOR = 0xE0E0E0;
    protected static final int field_45916 = 60;
    protected static final int field_45917 = 1;
    protected final TextRenderer textRenderer;
    protected final MultiValueDebugSampleLog log;

    protected DebugChart(TextRenderer textRenderer, MultiValueDebugSampleLog log) {
        this.textRenderer = textRenderer;
        this.log = log;
    }

    public int getWidth(int centerX) {
        return Math.min(this.log.getDimension() + 2, centerX);
    }

    public void render(DrawContext context, int x, int width) {
        int i = context.getScaledWindowHeight();
        context.fill(RenderLayer.getGuiOverlay(), x, i - 60, x + width, i, -1873784752);
        long l = 0L;
        long m = Integer.MAX_VALUE;
        long n = Integer.MIN_VALUE;
        int j = Math.max(0, this.log.getDimension() - (width - 2));
        int k = this.log.getLength() - j;
        for (int o = 0; o < k; ++o) {
            int p = x + o + 1;
            int q = j + o;
            long r = this.get(q);
            m = Math.min(m, r);
            n = Math.max(n, r);
            l += r;
            this.drawBar(context, i, p, q);
        }
        context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, i - 60, Colors.WHITE);
        context.drawHorizontalLine(RenderLayer.getGuiOverlay(), x, x + width - 1, i - 1, Colors.WHITE);
        context.drawVerticalLine(RenderLayer.getGuiOverlay(), x, i - 60, i, Colors.WHITE);
        context.drawVerticalLine(RenderLayer.getGuiOverlay(), x + width - 1, i - 60, i, Colors.WHITE);
        if (k > 0) {
            String string = this.format(m) + " min";
            String string2 = this.format((double)l / (double)k) + " avg";
            String string3 = this.format(n) + " max";
            context.drawTextWithShadow(this.textRenderer, string, x + 2, i - 60 - this.textRenderer.fontHeight, 0xE0E0E0);
            context.drawCenteredTextWithShadow(this.textRenderer, string2, x + width / 2, i - 60 - this.textRenderer.fontHeight, 0xE0E0E0);
            context.drawTextWithShadow(this.textRenderer, string3, x + width - this.textRenderer.getWidth(string3) - 2, i - 60 - this.textRenderer.fontHeight, 0xE0E0E0);
        }
        this.renderThresholds(context, x, width, i);
    }

    protected void drawBar(DrawContext context, int y, int x, int index) {
        this.drawTotalBar(context, y, x, index);
        this.drawOverlayBar(context, y, x, index);
    }

    protected void drawTotalBar(DrawContext context, int y, int x, int index) {
        long l = this.log.get(index);
        int i = this.getHeight(l);
        int j = this.getColor(l);
        context.fill(RenderLayer.getGuiOverlay(), x, y - i, x + 1, y, j);
    }

    protected void drawOverlayBar(DrawContext context, int y, int x, int index) {
    }

    protected long get(int index) {
        return this.log.get(index);
    }

    protected void renderThresholds(DrawContext context, int x, int width, int height) {
    }

    protected void drawBorderedText(DrawContext context, String string, int x, int y) {
        context.fill(RenderLayer.getGuiOverlay(), x, y, x + this.textRenderer.getWidth(string) + 1, y + this.textRenderer.fontHeight, -1873784752);
        context.drawText(this.textRenderer, string, x + 1, y + 1, 0xE0E0E0, false);
    }

    protected abstract String format(double var1);

    protected abstract int getHeight(double var1);

    protected abstract int getColor(long var1);

    protected int getColor(double value, double min, int minColor, double median, int medianColor, double max, int maxColor) {
        if ((value = MathHelper.clamp(value, min, max)) < median) {
            return ColorHelper.Argb.lerp((float)((value - min) / (median - min)), minColor, medianColor);
        }
        return ColorHelper.Argb.lerp((float)((value - median) / (max - median)), medianColor, maxColor);
    }
}

