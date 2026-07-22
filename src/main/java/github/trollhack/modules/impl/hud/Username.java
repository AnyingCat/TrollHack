package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.StringSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Username extends HudModule {
    public static final Username INSTANCE = new Username();

    private final StringSetting prefix = stringSetting("Prefix", "Welcome ");
    private final StringSetting suffix = stringSetting("Suffix", "");
    private final FloatSetting scale = floatSetting("Scale", 1.0f, 0.5f, 2.0f, 0.05f);

    public Username() {
        super("Username", 2, 120, 60, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        MatrixStack matrices = context.getMatrices();
        float s = scale.getValue();
        String prefixText = prefix.getValue();
        String username = mc.getSession().getUsername();
        String suffixText = suffix.getValue();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float totalWidth = 0f;
        if (!prefixText.isEmpty()) {
            FontRenderers.ducksans.drawText(matrices, prefixText, x, y, s, textColor);
            float w = FontRenderers.ducksans.getStringWidth(prefixText, s);
            x += w;
            totalWidth += w;
        }
        FontRenderers.ducksans.drawText(matrices, username, x, y, s, primaryColor);
        float usernameW = FontRenderers.ducksans.getStringWidth(username, s);
        x += usernameW;
        totalWidth += usernameW;
        if (!suffixText.isEmpty()) {
            FontRenderers.ducksans.drawText(matrices, suffixText, x, y, s, textColor);
            totalWidth += FontRenderers.ducksans.getStringWidth(suffixText, s);
        }
        setWidth(totalWidth + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
