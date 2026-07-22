/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(value=EnvType.CLIENT)
public class SplashTextRenderer {
    public static final SplashTextRenderer MERRY_X_MAS_ = new SplashTextRenderer("Merry X-mas!");
    public static final SplashTextRenderer HAPPY_NEW_YEAR_ = new SplashTextRenderer("Happy new year!");
    public static final SplashTextRenderer OOOOO_O_O_OOOOO__SPOOKY_ = new SplashTextRenderer("OOoooOOOoooo! Spooky!");
    private static final int TEXT_X = 123;
    private static final int TEXT_Y = 69;
    private final String text;

    public SplashTextRenderer(String text) {
        this.text = text;
    }

    /**
     * @param alpha a color with the appropriate alpha component bits set
     */
    public void render(DrawContext context, int screenWidth, TextRenderer textRenderer, int alpha) {
        context.getMatrices().push();
        context.getMatrices().translate((float)screenWidth / 2.0f + 123.0f, 69.0f, 0.0f);
        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-20.0f));
        float f = 1.8f - MathHelper.abs(MathHelper.sin((float)(Util.getMeasuringTimeMs() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
        f = f * 100.0f / (float)(textRenderer.getWidth(this.text) + 32);
        context.getMatrices().scale(f, f, f);
        context.drawCenteredTextWithShadow(textRenderer, this.text, 0, -8, 0xFFFF00 | alpha);
        context.getMatrices().pop();
    }
}

