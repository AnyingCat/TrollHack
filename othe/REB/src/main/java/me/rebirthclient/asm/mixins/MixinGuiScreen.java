//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.events.impl.RenderToolTipEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.items.other.Particle;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.misc.ToolTips;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiScreen.class})
public abstract class MixinGuiScreen extends Gui {
   private boolean hoveringShulker;
   private ItemStack shulkerStack;
   private String shulkerName;
   private final Particle.Util particles = new Particle.Util(300);

   @Inject(
      method = {"renderToolTip"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderToolTipHook(ItemStack stack, int x, int y, CallbackInfo info) {
      RenderToolTipEvent event = new RenderToolTipEvent(stack, x, y);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         info.cancel();
      }

      if (stack.getItem() instanceof ItemShulkerBox) {
         this.hoveringShulker = true;
         this.shulkerStack = stack;
         this.shulkerName = stack.getDisplayName();
      } else {
         this.hoveringShulker = false;
      }
   }

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")}
   )
   public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {
      if (mouseButton == 2 && this.hoveringShulker && ToolTips.INSTANCE.wheelPeek.getValue() && ToolTips.INSTANCE.isOn()) {
         ToolTips.drawShulkerGui(this.shulkerStack, this.shulkerName);
      }
   }

   @Inject(
      method = {"drawScreen"},
      at = {@At("HEAD")}
   )
   public void drawScreenHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
      if (Wrapper.mc.currentScreen != null && !(Wrapper.mc.currentScreen instanceof GuiContainer)) {
         if (ClickGui.INSTANCE.background.getValue() && Wrapper.mc.world != null && !(Wrapper.mc.currentScreen instanceof GuiChat)) {
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

         if (ClickGui.INSTANCE.waterMark.getValue()) {
            GuiScreen screen = Wrapper.mc.currentScreen;
            if (Wrapper.mc.world == null) {
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
   }

   @Inject(
      method = {"drawWorldBackground(I)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void drawWorldBackgroundHook(int tint, CallbackInfo info) {
      if (Wrapper.mc.world != null && ClickGui.INSTANCE.cleanGui.getValue()) {
         info.cancel();
      }
   }
}
