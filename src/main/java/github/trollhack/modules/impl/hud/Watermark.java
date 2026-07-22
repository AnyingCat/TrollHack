package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Watermark extends HudModule {
    public static final Watermark INSTANCE = new Watermark();

    private final FloatSetting scale = floatSetting("Scale", 1.0f, 0.5f, 2.0f, 0.05f);

    public Watermark() {
        super("Watermark", 2, 2, 60, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        MatrixStack matrices = context.getMatrices();
        float s = scale.getValue();
        String text = "TrollHack 1.0";
        setWidth(FontRenderers.ducksans.getStringWidth(text, s) + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
        FontRenderers.ducksans.drawText(matrices, text, getPosX() + 2f, getPosY() + 2f, s, GuiSetting.INSTANCE.primaryColor.getValue());
    }
}
