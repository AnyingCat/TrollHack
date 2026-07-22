//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryPreview extends Module {
   public final Setting<InventoryPreview.XOffset> xOffset = this.add(new Setting<>("XOffset", InventoryPreview.XOffset.CUSTOM));
   public final Setting<Integer> x = this.add(new Setting<>("X", 500, 0, 1000, v -> this.xOffset.getValue() == InventoryPreview.XOffset.CUSTOM));
   public final Setting<InventoryPreview.YOffset> yOffset = this.add(new Setting<>("YOffset", InventoryPreview.YOffset.CUSTOM));
   public final Setting<Integer> y = this.add(new Setting<>("Y", 2, 0, 1000, v -> this.yOffset.getValue() == InventoryPreview.YOffset.CUSTOM));
   public final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
   public final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(10, 10, 10, 100), v -> this.outline.isOpen()));
   public final Setting<Color> secondColor = this.add(new Setting<>("SecondColor", new Color(30, 30, 30, 100), v -> this.outline.isOpen()).injectBoolean(true));
   public final Setting<Boolean> rect = this.add(new Setting<>("Rect", true).setParent());
   public final Setting<Color> rectColor = this.add(new Setting<>("RectColor", new Color(10, 10, 10, 50), v -> this.rect.isOpen()));

   public InventoryPreview() {
      super("Inventory", "Allows you to see your own inventory without opening it", Category.HUD);
   }

   @Override
   public void onRender2D(Render2DEvent event) {
      if (!fullNullCheck()) {
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         GlStateManager.disableDepth();
         int x = this.xOffset.getValue() == InventoryPreview.XOffset.CUSTOM
            ? this.x.getValue()
            : (this.xOffset.getValue() == InventoryPreview.XOffset.LEFT ? 0 : Managers.TEXT.scaledWidth - 172);
         int y = this.yOffset.getValue() == InventoryPreview.YOffset.CUSTOM
            ? this.y.getValue()
            : (this.yOffset.getValue() == InventoryPreview.YOffset.TOP ? 0 : Managers.TEXT.scaledHeight - 74);
         if (this.outline.getValue()) {
            RenderUtil.drawNameTagOutline(
               (float)x + 6.5F,
               (float)y + 16.5F,
               (float)x + 171.5F,
               (float)y + 73.5F,
               1.0F,
               this.lineColor.getValue().getRGB(),
               this.secondColor.booleanValue ? this.secondColor.getValue().getRGB() : this.lineColor.getValue().getRGB(),
               false
            );
         }

         if (this.rect.getValue()) {
            RenderUtil.drawRect((float)(x + 7), (float)(y + 17), (float)(x + 171), (float)(y + 73), this.rectColor.getValue().getRGB());
         }

         GlStateManager.enableDepth();
         RenderHelper.enableGUIStandardItemLighting();
         GlStateManager.enableRescaleNormal();
         GlStateManager.enableColorMaterial();
         GlStateManager.enableLighting();
         NonNullList<ItemStack> items = mc.player.inventory.mainInventory;

         for(int i = 0; i < items.size() - 9; ++i) {
            int iX = x + i % 9 * 18 + 8;
            int iY = y + i / 9 * 18 + 18;
            ItemStack stack = (ItemStack)items.get(i + 9);
            mc.getItemRenderer().itemRenderer.zLevel = 501.0F;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iX, iY);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, stack, iX, iY, null);
            mc.getItemRenderer().itemRenderer.zLevel = 0.0F;
         }

         GlStateManager.disableLighting();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }

   private static enum XOffset {
      CUSTOM,
      LEFT,
      RIGHT;
   }

   private static enum YOffset {
      CUSTOM,
      TOP,
      BOTTOM;
   }
}
