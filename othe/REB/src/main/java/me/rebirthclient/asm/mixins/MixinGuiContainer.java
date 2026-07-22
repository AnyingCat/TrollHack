//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import com.google.common.collect.Sets;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.Set;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.items.other.Particle;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.GuiAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiContainerEvent.DrawForeground;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({GuiContainer.class})
public abstract class MixinGuiContainer extends GuiScreen {
   @Shadow
   public Container inventorySlots;
   @Shadow
   protected int guiLeft;
   @Shadow
   protected int guiTop;
   @Shadow
   private Slot hoveredSlot;
   @Shadow
   private boolean isRightMouseClick;
   @Shadow
   private ItemStack draggedStack = ItemStack.EMPTY;
   @Shadow
   private int touchUpX;
   @Shadow
   private int touchUpY;
   @Shadow
   private Slot returningStackDestSlot;
   @Shadow
   private long returningStackTime;
   @Shadow
   private ItemStack returningStack = ItemStack.EMPTY;
   @Final
   @Shadow
   protected final Set<Slot> dragSplittingSlots = Sets.newHashSet();
   @Shadow
   protected boolean dragSplitting;
   @Shadow
   private int dragSplittingRemnant;
   private final Particle.Util particles = new Particle.Util(300);

   /**
    * @author makecat
    * @reason makecat
    */
   @Overwrite
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      if (ClickGui.INSTANCE.background.getValue() && this.mc.world != null) {
         RenderUtil.drawVGradientRect(
            0.0F,
            0.0F,
            (float)Managers.TEXT.scaledWidth,
            (float)Managers.TEXT.scaledHeight,
            new Color(0, 0, 0, 0).getRGB(),
            Managers.COLORS.getCurrentWithAlpha(ClickGui.INSTANCE.alpha.getValue())
         );
      }

      if (ClickGui.INSTANCE.particles.getValue()) {
         this.particles.drawParticles();
      }

      float size = (float)GuiAnimation.inventoryFade.easeOutQuad();
      GlStateManager.pushMatrix();
      GL11.glScaled((double)size, (double)size, (double)size);
      int i = this.guiLeft;
      int j = this.guiTop;
      this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
      GlStateManager.disableLighting();
      GlStateManager.disableDepth();
      super.drawScreen(mouseX, mouseY, partialTicks);
      RenderHelper.enableGUIStandardItemLighting();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)i, (float)j, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableRescaleNormal();
      this.hoveredSlot = null;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

      for(int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
         Slot slot = (Slot)this.inventorySlots.inventorySlots.get(i1);
         if (slot.isEnabled()) {
            this.drawSlot(slot);
         }

         if (this.isMouseOverSlot(slot, mouseX, mouseY) && slot.isEnabled()) {
            this.hoveredSlot = slot;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int j1 = slot.xPos;
            int k1 = slot.yPos;
            GlStateManager.colorMask(true, true, true, false);
            this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
         }
      }

      RenderHelper.disableStandardItemLighting();
      this.drawGuiContainerForegroundLayer(mouseX, mouseY);
      RenderHelper.enableGUIStandardItemLighting();
      MinecraftForge.EVENT_BUS.post(new DrawForeground((GuiContainer)(GuiScreen)this, mouseX, mouseY));
      InventoryPlayer inventoryplayer = this.mc.player.inventory;
      ItemStack itemstack = this.draggedStack.isEmpty() ? inventoryplayer.getItemStack() : this.draggedStack;
      if (!itemstack.isEmpty()) {
         int k2 = this.draggedStack.isEmpty() ? 8 : 16;
         String s = null;
         if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
            itemstack = itemstack.copy();
            itemstack.setCount(MathHelper.ceil((float)itemstack.getCount() / 2.0F));
         } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
            itemstack = itemstack.copy();
            itemstack.setCount(this.dragSplittingRemnant);
            if (itemstack.isEmpty()) {
               s = TextFormatting.YELLOW + "0";
            }
         }

         this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
      }

      if (!this.returningStack.isEmpty()) {
         float f = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0F;
         if (f >= 1.0F) {
            f = 1.0F;
            this.returningStack = ItemStack.EMPTY;
         }

         int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
         int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
         int l1 = this.touchUpX + (int)((float)l2 * f);
         int i2 = this.touchUpY + (int)((float)i3 * f);
         this.drawItemStack(this.returningStack, l1, i2, null);
      }

      GlStateManager.popMatrix();
      GlStateManager.enableLighting();
      GlStateManager.enableDepth();
      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
      GuiScreen screen = this.mc.currentScreen;
      if (ClickGui.INSTANCE.waterMark.getValue()) {
         if (this.mc.world == null) {
            Managers.TEXT
               .drawString(
                  "Rebirth " + ChatFormatting.WHITE + "alpha",
                  1.0F,
                  (float)(screen.height - Managers.TEXT.getFontHeight2()),
                  Managers.COLORS.getNormalCurrent().getRGB(),
                  true
               );
            Managers.TEXT.drawRollingRainbowString("powered by iMadCat", 1.0F, (float)(screen.height - Managers.TEXT.getFontHeight2() * 2), true);
         } else {
            Managers.TEXT
               .drawString(
                  "Rebirth " + ChatFormatting.WHITE + "alpha",
                  (float)screen.width - 1.0F - (float)Managers.TEXT.getStringWidth("Rebirth alpha"),
                  (float)(screen.height - Managers.TEXT.getFontHeight2()),
                  Managers.COLORS.getNormalCurrent().getRGB(),
                  true
               );
            Managers.TEXT
               .drawRollingRainbowString(
                  "powered by iMadCat",
                  (float)screen.width - 1.0F - (float)Managers.TEXT.getStringWidth("powered by iMadCat"),
                  (float)(screen.height - Managers.TEXT.getFontHeight2() * 2),
                  true
               );
         }
      }
   }

   @Shadow
   protected abstract void drawGuiContainerBackgroundLayer(float var1, int var2, int var3);

   @Shadow
   private void drawSlot(Slot slotIn) {
   }

   @Shadow
   protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
   }

   @Shadow
   private void drawItemStack(ItemStack stack, int x, int y, String altText) {
      GlStateManager.translate(0.0F, 0.0F, 32.0F);
      this.zLevel = 200.0F;
      this.itemRender.zLevel = 200.0F;
      FontRenderer font = stack.getItem().getFontRenderer(stack);
      if (font == null) {
         font = this.fontRenderer;
      }

      this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
      this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (this.draggedStack.isEmpty() ? 0 : 8), altText);
      this.zLevel = 0.0F;
      this.itemRender.zLevel = 0.0F;
   }

   @Shadow
   private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
      return false;
   }
}
