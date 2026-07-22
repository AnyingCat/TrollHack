package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Fps extends HudModule {
    public static final Fps INSTANCE = new Fps();

    private final BooleanSetting showFrameTime = booleanSetting("ShowFrameTime", false);

    public Fps() {
        super("Fps", 2, 155, 40, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        int fps = mc.getCurrentFps();
        String fpsText = String.valueOf(fps);
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        float w1 = FontRenderers.ducksans.getStringWidth(fpsText, s);
        FontRenderers.ducksans.drawText(matrices, fpsText, x, y, s, textColor);
        float w2 = FontRenderers.ducksans.getStringWidth(" Fps", s);
        FontRenderers.ducksans.drawText(matrices, " Fps", x + w1, y, s, primaryColor);
        float totalW = w1 + w2;
        float totalH = FontRenderers.ducksans.getStringHeight(s) + 4f;
        if (showFrameTime.getValue()) {
            String frameTimeText = String.format(" (%.2f ms)", 1000.0 / Math.max(fps, 1));
            FontRenderers.ducksans.drawText(matrices, frameTimeText, x + w1 + w2, y, s, textColor);
            totalW += FontRenderers.ducksans.getStringWidth(frameTimeText, s);
        }
        setWidth(totalW + 4f);
        setHeight(totalH);
    }
}
