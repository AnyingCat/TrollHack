package me.catrix.mod.gui.elements;

import me.catrix.Catrix;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.core.impl.GuiManager;
import me.catrix.api.utils.entity.EntityUtil;
import me.catrix.api.utils.render.ColorUtil;
import me.catrix.api.utils.render.Render2DUtil;
import me.catrix.api.utils.render.TextUtil;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.ArmorHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ArmorHUD extends Tab{
    public ArmorHUD() {
        this.width = 80;
        this.height = 34;
        this.x = (int) Catrix.CONFIG.getFloat("armor_x", 0);
        this.y = (int) Catrix.CONFIG.getFloat("armor_y", 200);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && ArmorHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
            if (mouseX >= (x) && mouseX <= (x + width)) {
                if (mouseY >= (y) && mouseY <= (y + height)) {
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
        if (ArmorHud.INSTANCE.isOn()) {
            if (HudEditor.INSTANCE.isOn()) {
                Render2DUtil.drawRect(drawContext.getMatrices(), x, y, width, height, new Color(0, 0, 0, 70));
            }
            int xOff = 0;
            for (ItemStack armor : mc.player.getInventory().armor) {
                xOff += 20;

                if (armor.isEmpty()) continue;
                matrixStack.push();
                int damage = EntityUtil.getDamagePercent(armor);
                int yOffset = height / 2;
                drawContext.drawItem(armor, this.x + width - xOff, this.y + yOffset);
                drawContext.drawItemInSlot(mc.textRenderer, armor, this.x + width - xOff, this.y + yOffset);
                TextUtil.drawStringScale(drawContext, damage + "%",
                        (float) (x + width + 2 - xOff),
                        (float) (y + yOffset - mc.textRenderer.fontHeight / 4d),
                        ColorUtil.fadeColor(new Color(196, 0, 0), new Color(0, 227, 0), damage / 100f).getRGB(), 0.5F);
/*				drawContext.drawText(mc.textRenderer,
						String.valueOf(damage),
						x + width + 8 - xOff - mc.textRenderer.getWidth(String.valueOf(damage)) / 2,
						y + yOffset - mc.textRenderer.fontHeight - 2,
						new Color((int) (255f * (1f - ((float) damage / 100f))), (int) (255f * ((float) damage / 100f)), 0).getRGB(),
						true);*/
                matrixStack.pop();
            }
        }
    }
}
