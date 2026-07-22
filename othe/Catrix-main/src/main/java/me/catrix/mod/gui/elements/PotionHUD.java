package me.catrix.mod.gui.elements;

import me.catrix.Catrix;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.PotionHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.awt.*;

public class PotionHUD extends Tab {
    public PotionHUD() {
        this.width = 120;
        this.height = 120;
        this.x = (int) Catrix.CONFIG.getFloat("potion_x", 10);
        this.y = (int) Catrix.CONFIG.getFloat("potion_y", 260);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && PotionHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
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
        MatrixStack matrixStack = drawContext.getMatrices();
        if (ClickGui.INSTANCE.isOn() && !HudEditor.INSTANCE.isOn()) {
            return;
        }
        if (PotionHud.INSTANCE.isOn()) {
            drawPotionHUD(matrixStack, drawContext);
        }
    }

    private void drawPotionHUD(MatrixStack matrixStack, DrawContext drawContext) {
        if (mc.player != null && mc.player.getStatusEffects().isEmpty()) return;
        float maxWidth = 125;
        float yOffset = 0;
        if (mc.player != null) {
            for (StatusEffectInstance effect : mc.player.getStatusEffects()) {maxWidth = Math.max(maxWidth, Math.max(FontRenderers.ui.getWidth(effect.getEffectType().getName().getString() + " " + getAmplifierString(effect.getAmplifier())), FontRenderers.ui.getWidth(getDurationString(effect))) + 25);}
        }
        if (mc.player != null) {
            for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
                RenderShadersUtil.drawRoundedBlur(matrixStack, x, y + yOffset, maxWidth, 25, 5.0f,  new Color(0x35000000, true), 15.0f, 0.55f);
                RenderShadersUtil.drawRect(matrixStack,
                        x + maxWidth - 5,
                        y + yOffset + 6.25f,
                        3, 12.5f, 1f, new Color(effect.getEffectType().getColor()));
                RenderShadersUtil.drawBlurredShadow(matrixStack,
                        x + maxWidth - 5,
                        y + yOffset + 6.25f,
                        3, 12.5f, 10, new Color(effect.getEffectType().getColor()));
                matrixStack.push();
                matrixStack.translate(x + 8, y + yOffset + 5, 0);
                drawContext.drawSprite(0, 0, 0, 15, 15, mc.getStatusEffectSpriteManager().getSprite(effect.getEffectType()));
                matrixStack.pop();
                FontRenderers.ui.drawString(matrixStack,
                        effect.getEffectType().getName().getString() + " " + getAmplifierString(effect.getAmplifier()),
                        x + 27,
                        y + yOffset + 15.0f - FontRenderers.ui.getFontHeight(),
                        new Color(effect.getEffectType().getColor()).getRGB());
                FontRenderers.ui.drawString(matrixStack,
                        getDurationString(effect),
                        x + 27,
                        y + yOffset + 15.0f,
                        new Color(0xFFFFFF).getRGB());
                yOffset += 30;
            }
        }
    }

    private String getAmplifierString(int amplifier) {
        return switch (amplifier) {
            case 0 -> "I";
            case 1 -> "II";
            case 2 -> "III";
            case 3 -> "IV";
            case 4 -> "V";
            default -> String.valueOf(amplifier + 1);
        };
    }

    private String getDurationString(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "∞";
        }
        int seconds = effect.getDuration() / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}