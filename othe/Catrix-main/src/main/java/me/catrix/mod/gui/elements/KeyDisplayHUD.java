package me.catrix.mod.gui.elements;

import me.catrix.Catrix;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.KeyDisplayHud;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class KeyDisplayHUD extends Tab {
    public KeyDisplayHUD() {
        this.width = 120;
        this.height = 100;
        this.x = (int) Catrix.CONFIG.getFloat("key_display_hud_x", 277);
        this.y = (int) Catrix.CONFIG.getFloat("key_display_hud_y", 396);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && KeyDisplayHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
            if (mouseX >= x && mouseX <= x + width) {
                if (mouseY >= y && mouseY <= y + height) {
                    if (ClickGuiScreen.clicked) {
                        GuiManager.currentGrabbed = this;
                    }
                }
            }
        }
    }

    @Override
    public void draw(DrawContext drawContext, float partialTicks, Color color) {
        if (ClickGui.INSTANCE.isOn() && !HudEditor.INSTANCE.isOn()) {
            return;
        }
        if (KeyDisplayHud.INSTANCE.isOn()) {
            Color wColor = mc.options.forwardKey.isPressed() ? new Color(0x60FFFFFF, true) : new Color(0x35000000, true);
            RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x + 15 + 30 + 1.5f, y + 15, 30, 30, 10, wColor, 15.0f, 0.55f);
            FontRenderers.ui2.drawCenteredString(drawContext.getMatrices(), "W", x + 15 + 30 + 1.5f + 15, y + 15 + 15 - FontRenderers.ui2.getMarginHeight() / 2, Color.WHITE.getRGB());

            Color aColor = mc.options.leftKey.isPressed() ? new Color(0x60FFFFFF, true) : new Color(0x35000000, true);
            RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x + 15, y + 15 + 30 + 1.5f, 30, 30, 10, aColor, 15.0f, 0.55f);
            FontRenderers.ui2.drawCenteredString(drawContext.getMatrices(), "A", x + 15 + 15, y + 15 + 30 + 1.5f + 15 - FontRenderers.ui2.getMarginHeight() / 2, Color.WHITE.getRGB());

            Color sColor = mc.options.backKey.isPressed() ? new Color(0x60FFFFFF, true) : new Color(0x35000000, true);
            RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x + 15 + 30 + 1.5f, y + 15 + 30 + 1.5f, 30, 30, 10, sColor, 15.0f, 0.55f);
            FontRenderers.ui2.drawCenteredString(drawContext.getMatrices(), "S", x + 15 + 30 + 1.5f + 15, y + 15 + 30 + 1.5f + 15 - FontRenderers.ui2.getMarginHeight() / 2, Color.WHITE.getRGB());

            Color dColor = mc.options.rightKey.isPressed() ? new Color(0x60FFFFFF, true) : new Color(0x35000000, true);
            RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x + 15 + (30 + 1.5f) * 2, y + 15 + 30 + 1.5f, 30, 30, 10, dColor, 15.0f, 0.55f);
            FontRenderers.ui2.drawCenteredString(drawContext.getMatrices(), "D", x + 15 + (30 + 1.5f) * 2 + 15, y + 15 + 30 + 1.5f + 15 - FontRenderers.ui2.getMarginHeight() / 2, Color.WHITE.getRGB());

            Color spaceColor = mc.options.jumpKey.isPressed() ? new Color(0x60FFFFFF, true) : new Color(0x35000000, true);
            RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x + 15, y + 15 + (30 + 1.5f) * 2, 30 * 3 + 1.5f * 2, 20, 10, spaceColor, 15.0f, 0.55f);
            FontRenderers.ui2.drawCenteredString(drawContext.getMatrices(), "SPACE", x + 15 + (double) (30 * 3 + 1.5f * 2) / 2, y + 15 + (30 + 1.5f) * 2 + 10 - FontRenderers.ui2.getMarginHeight() / 2, Color.WHITE.getRGB());
        }
    }
}