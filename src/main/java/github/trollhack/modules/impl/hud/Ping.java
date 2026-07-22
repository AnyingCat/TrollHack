package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class Ping extends HudModule {
    public static final Ping INSTANCE = new Ping();

    public Ping() {
        super("Ping", 2, 185, 40, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        String pingText = String.valueOf(entry != null ? entry.getLatency() : 0);
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float x = getPosX() + 2f;
        float y = getPosY() + 2f;
        float s = 1.0f;
        FontRenderers.ducksans.drawText(matrices, pingText, x, y, s, textColor);
        float w1 = FontRenderers.ducksans.getStringWidth(pingText, s);
        FontRenderers.ducksans.drawText(matrices, " ms", x + w1, y, s, primaryColor);
        float totalW = w1 + FontRenderers.ducksans.getStringWidth(" ms", s);
        setWidth(totalW + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
