package me.catrix.mod.gui.elements;

import me.catrix.Catrix;
import me.catrix.api.utils.render.RenderShadersUtil;
import me.catrix.core.impl.GuiManager;
import me.catrix.mod.gui.clickgui.ClickGuiScreen;
import me.catrix.mod.gui.clickgui.tabs.Tab;
import me.catrix.mod.modules.impl.client.ClickGui;
import me.catrix.mod.modules.impl.client.HudEditor;
import me.catrix.mod.modules.impl.hud.ItemsCountHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.*;

public class ItemsCountHUD extends Tab {
    public ItemsCountHUD() {
        this.width = 200;
        this.height = 20;
        this.x = (int) Catrix.CONFIG.getFloat("items_count_x", 574);
        this.y = (int) Catrix.CONFIG.getFloat("items_count_y", 518);
    }

    @Override
    public void update(double mouseX, double mouseY) {
        if (GuiManager.currentGrabbed == null && ItemsCountHud.INSTANCE.isOn() && HudEditor.INSTANCE.isOn()) {
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
        if (ItemsCountHud.INSTANCE.isOn()) {
            MatrixStack matrices = drawContext.getMatrices();
            RenderShadersUtil.drawRoundedBlur(matrices, x, y + 2, width + 1, height, 3.0f, new Color(0x35000000, true), 15.0f, 0.55f);
            int xOffset = 0;
            int yOffset = (int) (height / 4.5f);

            drawItemWithCount(drawContext, Items.END_CRYSTAL, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.EXPERIENCE_BOTTLE, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.ENDER_PEARL, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.OBSIDIAN, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.ENCHANTED_GOLDEN_APPLE, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.TOTEM_OF_UNDYING, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.COBWEB, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.RESPAWN_ANCHOR, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.GLOWSTONE, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.PISTON, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.REDSTONE_BLOCK, xOffset, yOffset);
            xOffset += 20;

            drawItemWithCount(drawContext, Items.ENDER_CHEST, xOffset, yOffset);
        }
    }

    private void drawItemWithCount(DrawContext drawContext, Item item, int xOffset, int yOffset) {
        int count = me.catrix.api.utils.entity.InventoryUtil.getItemCount(item);
        if (count == 0 && ItemsCountHud.INSTANCE.isOn()) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        stack.setCount(Math.max(count, 1));
        drawContext.drawItem(stack, x + xOffset, y + yOffset);
        drawContext.drawItemInSlot(mc.textRenderer, stack, x + xOffset, y + yOffset);
    }
}