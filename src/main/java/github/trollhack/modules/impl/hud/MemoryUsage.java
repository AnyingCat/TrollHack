package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class MemoryUsage extends HudModule {
    public static final MemoryUsage INSTANCE = new MemoryUsage();

    private final BooleanSetting showAllocated = booleanSetting("ShowAllocated", false);
    private final BooleanSetting showMax = booleanSetting("ShowMax", false);

    private static final long BYTE_TO_MB = 1048576L;

    public MemoryUsage() {
        super("MemoryUsage", 2, 170, 60, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        Runtime runtime = Runtime.getRuntime();
        String usedText = String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / BYTE_TO_MB);
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        float totalW = 0f;
        FontRenderers.ducksans.drawText(matrices, usedText, x, y, s, textColor);
        totalW += FontRenderers.ducksans.getStringWidth(usedText, s);
        if (showAllocated.getValue()) {
            String allocText = " " + runtime.totalMemory() / BYTE_TO_MB;
            FontRenderers.ducksans.drawText(matrices, allocText, x + totalW, y, s, textColor);
            totalW += FontRenderers.ducksans.getStringWidth(allocText, s);
        }
        if (showMax.getValue()) {
            String maxText = " " + runtime.maxMemory() / BYTE_TO_MB;
            FontRenderers.ducksans.drawText(matrices, maxText, x + totalW, y, s, textColor);
            totalW += FontRenderers.ducksans.getStringWidth(maxText, s);
        }
        String mbText = " MB";
        FontRenderers.ducksans.drawText(matrices, mbText, x + totalW, y, s, primaryColor);
        totalW += FontRenderers.ducksans.getStringWidth(mbText, s);
        setWidth(totalW + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
