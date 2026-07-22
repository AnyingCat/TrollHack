package me.catrix.mod.gui.elements;

import me.catrix.Catrix;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.gui.font.FontRenderers;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.InventoryHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.awt.*;

public class InventoryHUD extends Tab {

    public InventoryHUD() {
        this.width = 5 * 2 + 9 * (16 + 2) - 2;
        this.height = 15 + 5 + 3 * (16 + 2) - 2;
        this.x = (int) Catrix.CONFIG.getFloat("inventory_hud_x", 593);
        this.y = (int) Catrix.CONFIG.getFloat("inventory_hud_y", 6);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && InventoryHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
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
        if (InventoryHud.INSTANCE.isOn() && mc.player != null) {
            renderInventoryHud(drawContext);
        }
    }

    private void renderInventoryHud(DrawContext drawContext) {
        RenderShadersUtil.drawRoundedBlur(drawContext.getMatrices(), x, y, width, height, 9f, new Color(0x35000000, true), 15.0f, 0.55f);
        RenderShadersUtil.drawRect2(drawContext.getMatrices(), x, y, width, height, 9f, new Color(0x4F000000, true),new Color(0x6E3A3A3A, true),0.79f);
//        if (HudEditor.INSTANCE.isOff() && ClickGui.INSTANCE.isOff()) {
//            RenderShadersUtil.drawBlurredShadow(drawContext.getMatrices(), x, y, width, height, 10, new Color(0x54000000, true));
//        }
        FontRenderers.icon2.drawString(drawContext.getMatrices(), "e",
                x + 5, y + 4,
                Color.WHITE.getRGB());
        FontRenderers.ui2.drawString(drawContext.getMatrices(), "Inventory: ",
                x + 21, y + 5,
                Color.WHITE.getRGB());
        DefaultedList<ItemStack> mainInventory = mc.player.getInventory().main;
        MatrixStack matrices = drawContext.getMatrices();
        int startX = x + 5;
        int startY = y + 15;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col + 9;
                if (slotIndex < mainInventory.size()) {
                    ItemStack stack = mainInventory.get(slotIndex);
                    int xPos = startX + col * (16 + 2);
                    int yPos = startY + row * (16 + 2);
                    if (!stack.isEmpty()) {
                        matrices.push();
                        matrices.translate(0, 0, 200);
                        drawContext.drawItem(stack, xPos, yPos);
                        drawContext.drawItemInSlot(mc.textRenderer, stack, xPos, yPos);
                        matrices.pop();
                    }
                }
            }
        }
    }
}