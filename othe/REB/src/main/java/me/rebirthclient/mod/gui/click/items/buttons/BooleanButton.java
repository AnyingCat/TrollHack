//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

public class BooleanButton extends Button {
   private final Setting setting;
   private int progress = 0;

   public BooleanButton(Setting setting) {
      super(setting.getName());
      this.setting = setting;
      this.width = 15;
   }

   @Override
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
      boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
      boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
      if (future) {
         RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + (float)this.width + 7.4F,
            this.y + (float)this.height - 0.5F,
            this.getState()
               ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120))
               : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55))
         );
         Managers.TEXT
            .drawStringWithShadow(
               newStyle ? this.getName().toLowerCase() : this.getName(),
               this.x + 2.3F,
               this.y - 1.7F - (float)Gui.INSTANCE.getTextOffset(),
               this.getState() ? -1 : -5592406
            );
      } else if (dotgod) {
         RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + (float)this.width + 7.4F,
            this.y + (float)this.height - 0.5F,
            this.getState()
               ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90))
               : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55))
         );
         Managers.TEXT
            .drawStringWithShadow(
               this.getName().toLowerCase(),
               this.x + 2.3F,
               this.y - 1.7F - (float)Gui.INSTANCE.getTextOffset(),
               this.getState() ? Managers.COLORS.getCurrentGui(240) : 11579568
            );
      } else {
         RenderUtil.drawRect(
            this.x,
            this.y,
            this.x + (float)this.width + 7.4F,
            this.y + (float)this.height - 0.5F,
            this.getState()
               ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200))
               : (!this.isHovering(mouseX, mouseY) ? 290805077 : -2007673515)
         );
         Managers.TEXT
            .drawStringWithShadow(
               newStyle ? this.getName().toLowerCase() : this.getName(),
               this.x + 2.3F,
               this.y - 1.7F - (float)Gui.INSTANCE.getTextOffset(),
               this.getState() ? -1 : -5592406
            );
      }

      if (this.setting.parent) {
         if (this.setting.open) {
            ++this.progress;
         }

         if (future) {
            if (!this.setting.open) {
               this.progress = 0;
            }

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/gear.png"));
            GlStateManager.translate(this.getX() + (float)this.getWidth() - 6.7F + 8.0F, this.getY() + 7.7F - 0.3F, 0.0F);
            GlStateManager.rotate(Component.calculateRotation((float)this.progress), 0.0F, 0.0F, 1.0F);
            RenderUtil.drawModalRect(-5, -5, 0.0F, 0.0F, 10, 10, 10, 10, 10.0F, 10.0F);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
         } else {
            String color = !this.getState() && !newStyle ? "" + ChatFormatting.GRAY : "";
            String gear = this.setting.open ? "-" : "+";
            Managers.TEXT
               .drawStringWithShadow(color + gear, this.x - 1.5F + (float)this.width - 7.4F + 8.0F, this.y - 2.2F - (float)Gui.INSTANCE.getTextOffset(), -1);
         }
      }
   }

   @Override
   public void update() {
      this.setHidden(!this.setting.isVisible());
   }

   @Override
   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }

      if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
         this.setting.open = !this.setting.open;
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
   }

   @Override
   public int getHeight() {
      return ClickGui.INSTANCE.getButtonHeight() - 1;
   }

   @Override
   public void toggle() {
      this.setting.setValue(!((Setting<Boolean>)this.setting).getValue());
   }

   @Override
   public boolean getState() {
      return ((Setting<Boolean>)this.setting).getValue();
   }
}
