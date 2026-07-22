package github.trollhack.modules.impl.hud;

import github.trollhack.modules.HudModule;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.render.font.FontRenderers;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ServerBrand extends HudModule {
    public static final ServerBrand INSTANCE = new ServerBrand();

    public ServerBrand() {
        super("ServerBrand", 2, 200, 80, 12);
    }

    @Override
    public void onHudRender(DrawContext context) {
        if (FontRenderers.ducksans == null) return;
        String text;
        if (mc.isIntegratedServerRunning()) {
            text = "Singleplayer";
        } else {
            ServerInfo serverInfo = mc.getCurrentServerEntry();
            text = serverInfo != null ? serverInfo.address : "Unknown";
        }
        MatrixStack matrices = context.getMatrices();
        Color textColor = GuiSetting.INSTANCE.getText();
        Color primaryColor = GuiSetting.INSTANCE.getPrimary();
        float s = 1.0f;
        FontRenderers.ducksans.drawText(matrices, "IP: ", getPosX() + 2f, getPosY() + 2f, s, textColor);
        float labelWidth = FontRenderers.ducksans.getStringWidth("IP: ", s);
        FontRenderers.ducksans.drawText(matrices, text, getPosX() + 2f + labelWidth, getPosY() + 2f, s, primaryColor);
        setWidth(FontRenderers.ducksans.getStringWidth("IP: " + text, s) + 4f);
        setHeight(FontRenderers.ducksans.getStringHeight(s) + 4f);
    }
}
