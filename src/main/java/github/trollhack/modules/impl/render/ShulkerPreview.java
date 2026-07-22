package github.trollhack.modules.impl.render;

import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.modules.impl.client.GuiSetting;
import github.trollhack.utils.render.Render2DUtil;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.awt.Color;

public class ShulkerPreview extends Module {
    public static final ShulkerPreview INSTANCE = new ShulkerPreview();

    public ShulkerPreview() {
        super("Shulker Preview", Category.RENDER);
    }

    public static ContainerComponent getShulkerData(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        }
        return null;
    }

    public static void renderShulkerAndItems(DrawContext context, ItemStack stack, int originalX, int originalY, ContainerComponent container) {
        DefaultedList<ItemStack> shulkerInventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
        container.copyTo(shulkerInventory);

        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 400);

        MatrixStack matrices = context.getMatrices();
        int boxWidth = 146;
        int boxX = originalX + 11;
        int boxY = originalY - 12;
        int boxHeight = 52;

        Render2DUtil.drawRect(matrices, boxX, boxY, boxWidth, boxHeight, GuiSetting.INSTANCE.getBackGround());

        Color primary = GuiSetting.INSTANCE.getPrimary();
        Color outlineColor = new Color(primary.getRed(), primary.getGreen(), primary.getBlue(), 255);
        Render2DUtil.drawRectOutline(matrices, boxX, boxY, boxWidth, boxHeight, 0.5f, outlineColor);

        for (int i = 0; i < shulkerInventory.size(); i++) {
            int itemX = originalX + i % 9 * 16 + 12;
            int itemY = originalY + i / 9 * 16 - 10;
            ItemStack itemStack = shulkerInventory.get(i);
            if (itemStack.isEmpty()) continue;

            context.drawItem(itemStack, itemX, itemY);
            context.drawItemInSlot(mc.textRenderer, itemStack, itemX, itemY);
        }

        context.getMatrices().pop();
    }
}
