package github.trollhack.utils.render.font.nanovg;

import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.nanovg.NVGColor;

import java.awt.*;

import static org.lwjgl.nanovg.NanoVG.*;

public final class NVGFont {
    private static final int ALIGN_LEFT_TOP = NVG_ALIGN_LEFT | NVG_ALIGN_TOP;

    private final String fontName;
    private final float baseSize;
    private volatile int fontId = -1;
    private volatile boolean initialized = false;

    public NVGFont(String fontName, float baseSize) {
        this.fontName = fontName;
        this.baseSize = baseSize;
    }

    public void init() {
        if (initialized) return;
        NanoVGRenderer.INSTANCE.initNanoVG();
        fontId = FontManager.getOrCreate(fontName, baseSize);
        initialized = true;
    }

    private void ensureInitialized() {
        if (!initialized) init();
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, float scale, Color color) {
        ensureInitialized();
        NanoVGRenderer.INSTANCE.draw(ctx -> {
            nvgFontFaceId(ctx, fontId);
            nvgFontSize(ctx, baseSize * scale);
            nvgTextAlign(ctx, ALIGN_LEFT_TOP);
            nvgFillColor(ctx, toNVGColor(color));
            nvgText(ctx, x, y, text);
        });
    }

    public void drawText(MatrixStack matrices, String text, float x, float y, float scale, Color color,
                         float clipX, float clipY, float clipW, float clipH) {
        ensureInitialized();
        NanoVGRenderer.INSTANCE.draw(ctx -> {
            nvgFontFaceId(ctx, fontId);
            nvgFontSize(ctx, baseSize * scale);
            nvgTextAlign(ctx, ALIGN_LEFT_TOP);
            nvgFillColor(ctx, toNVGColor(color));
            nvgScissor(ctx, clipX, clipY, clipW, clipH);
            nvgText(ctx, x, y, text);
        });
    }

    public float getStringWidth(String text, float scale) {
        if (fontId == -1) init();
        long vg = NanoVGRenderer.INSTANCE.getContext();
        nvgFontFaceId(vg, fontId);
        nvgFontSize(vg, baseSize * scale);
        return nvgTextBounds(vg, 0, 0, text, new float[4]);
    }

    public float getStringHeight(float scale) {
        ensureInitialized();
        long ctx = NanoVGRenderer.INSTANCE.getContext();
        nvgFontFaceId(ctx, fontId);
        nvgFontSize(ctx, baseSize * scale);
        float[] ascender = new float[1];
        float[] descender = new float[1];
        float[] lineh = new float[1];
        nvgTextMetrics(ctx, ascender, descender, lineh);
        return ascender[0] - descender[0];
    }

    public float getAscender(float scale) {
        ensureInitialized();
        long ctx = NanoVGRenderer.INSTANCE.getContext();
        nvgFontFaceId(ctx, fontId);
        nvgFontSize(ctx, baseSize * scale);
        float[] ascender = new float[1];
        float[] descender = new float[1];
        float[] lineh = new float[1];
        nvgTextMetrics(ctx, ascender, descender, lineh);
        return ascender[0];
    }

    public float getDescender(float scale) {
        ensureInitialized();
        long ctx = NanoVGRenderer.INSTANCE.getContext();
        nvgFontFaceId(ctx, fontId);
        nvgFontSize(ctx, baseSize * scale);
        float[] ascender = new float[1];
        float[] descender = new float[1];
        float[] lineh = new float[1];
        nvgTextMetrics(ctx, ascender, descender, lineh);
        return descender[0];
    }

    private static NVGColor toNVGColor(Color color) {
        NVGColor nvg = NVGColor.create();
        nvgRGBA(
            (byte) color.getRed(),
            (byte) color.getGreen(),
            (byte) color.getBlue(),
            (byte) color.getAlpha(),
            nvg
        );
        return nvg;
    }

    public int getFontId() { return fontId; }
    public String getFontName() { return fontName; }
    public float getBaseSize() { return baseSize; }
}
