//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.asm.mixins;

import java.awt.Color;
import me.rebirthclient.api.util.render.shader.GLSLShader;
import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import me.rebirthclient.mod.gui.screen.AltGui;
import me.rebirthclient.mod.gui.screen.ConsoleGui;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.MenuShader;
import me.rebirthclient.mod.modules.impl.render.ShaderChams;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiMainMenu.class})
public abstract class MixinGuiMainMenu extends GuiScreen {
   @Shadow
   private int widthCopyright;
   @Shadow
   private int widthCopyrightRest;
   @Shadow
   private float minceraftRoll;
   @Shadow
   private static ResourceLocation MINECRAFT_TITLE_TEXTURES;
   @Shadow
   private static ResourceLocation field_194400_H;
   @Shadow
   private float panoramaTimer;
   @Shadow
   private int openGLWarning2Width;
   @Shadow
   private int openGLWarningX1;
   @Shadow
   private int openGLWarningY1;
   @Shadow
   private int openGLWarningX2;
   @Shadow
   private int openGLWarningY2;
   @Shadow
   private String openGLWarning1;
   @Shadow
   private String openGLWarning2;
   @Shadow
   private ResourceLocation backgroundTexture;
   private boolean isGuiOpen;
   public GLSLShader shader;
   public long initTime;
   private final ResourceLocation background = new ResourceLocation("textures/rebirth/constant/empty.png");

   @Inject(
      method = {"keyTyped"},
      at = {@At("HEAD")},
      cancellable = true
   )
   protected void keyTyped(char typedChar, int keyCode, CallbackInfo info) {
      if (keyCode == ClickGui.INSTANCE.bind.getValue().getKey()) {
         ClickGui.INSTANCE.enable();
         this.isGuiOpen = true;
      }

      if (keyCode == 1) {
         ClickGui.INSTANCE.disable();
         this.isGuiOpen = false;
      }

      if (this.isGuiOpen) {
         try {
            Gui.INSTANCE.keyTyped(typedChar, keyCode);
         } catch (Exception var5) {
         }

         info.cancel();
      }
   }

   @Inject(
      method = {"drawScreen(IIF)V"},
      at = {@At("TAIL")}
   )
   public void drawScreenTailHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
      if (this.isGuiOpen) {
         Gui.INSTANCE.drawScreen(mouseX, mouseY, partialTicks);
      }
   }

   @Inject(
      method = {"mouseClicked"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {
      if (this.isGuiOpen) {
         Gui.INSTANCE.mouseClicked(mouseX, mouseY, mouseButton);
         info.cancel();
      }
   }

   protected void mouseReleased(int mouseX, int mouseY, int state) {
      if (this.isGuiOpen) {
         Gui.INSTANCE.mouseReleased(mouseX, mouseY, state);
      }
   }

   @Inject(
      method = {"initGui"},
      at = {@At("HEAD")}
   )
   private void initHook(CallbackInfo info) {
      this.initTime = System.currentTimeMillis();
   }

   @Inject(
      method = {"actionPerformed"},
      at = {@At("HEAD")}
   )
   public void actionPerformed(GuiButton button, CallbackInfo info) {
      if (button.id == 114515) {
         MenuShader.INSTANCE.shader.increaseEnum();
      }

      if (button.id == 114516) {
         ClickGui.INSTANCE.enable();
      }

      if (button.id == 114517) {
         this.mc.displayGuiScreen(new AltGui());
      }

      if (button.id == 114518) {
         this.mc.displayGuiScreen(new ConsoleGui());
      }
   }

   /**
    * @author makecat
    * @reason makecat
    */
   @Overwrite
   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      if (Display.isActive() || Display.isVisible()) {
         this.renderLogo(this.background);
         FramebufferShader shader = MenuShader.INSTANCE.shader.getValue().getShader();
         if (shader != null) {
            GL11.glBlendFunc(770, 771);
            shader.setShaderParams(true, MenuShader.INSTANCE.animationSpeed.getValue(), new Color(255, 255, 255), 0.0F, 0.0F, 0.0F);
            shader.startDraw(ShaderChams.mc.getRenderPartialTicks());
            this.renderLogo(this.background);
            shader.stopDraw();
         }
      }

      this.panoramaTimer += partialTicks;
      int j = this.width / 2 - 137;
      this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      if ((double)this.minceraftRoll < 1.0E-4) {
         this.drawTexturedModalRect(j, 30, 0, 0, 99, 44);
         this.drawTexturedModalRect(j + 99, 30, 129, 0, 27, 44);
         this.drawTexturedModalRect(j + 99 + 26, 30, 126, 0, 3, 44);
         this.drawTexturedModalRect(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
         this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
      } else {
         this.drawTexturedModalRect(j, 30, 0, 0, 155, 44);
         this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
      }

      this.mc.getTextureManager().bindTexture(field_194400_H);
      drawModalRectWithCustomSizedTexture(j + 88, 67, 0.0F, 0.0F, 98, 14, 128.0F, 16.0F);
      if (mouseX > this.widthCopyrightRest
         && mouseX < this.widthCopyrightRest + this.widthCopyright
         && mouseY > this.height - 10
         && mouseY < this.height
         && Mouse.isInsideWindow()) {
         drawRect(this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, -1);
      }

      if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty()) {
         drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 1428160512);
         this.drawString(this.fontRenderer, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
         this.drawString(
            this.fontRenderer,
            this.openGLWarning2,
            (this.width - this.openGLWarning2Width) / 2,
            ((GuiButton)this.buttonList.get(0)).y - 12,
            -1
         );
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void renderLogo(ResourceLocation location) {
      this.mc.renderEngine.bindTexture(location);
      GlStateManager.color(255.0F, 255.0F, 255.0F);
      Gui.drawScaledCustomSizeModalRect(
         0,
         0,
         0.0F,
         0.0F,
         this.mc.displayWidth,
         this.mc.displayHeight,
         this.mc.displayWidth,
         this.mc.displayHeight,
         (float)this.mc.displayWidth,
         (float)this.mc.displayHeight
      );
   }

   /**
    * @author makecat
    * @reason makecat
    */
   @Overwrite
   private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer", new Object[0])));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer", new Object[0])));
      this.buttonList
         .add(new GuiButton(6, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("fml.menu.mods", new Object[0])));
      this.buttonList.add(new GuiButton(114515, this.width / 2 + 2 + 102, p_73969_1_, 44, 20, "Shader"));
      this.buttonList.add(new GuiButton(114516, this.width / 2 + 2 + 102, p_73969_1_ + p_73969_2_, 44, 20, "Gui"));
      this.buttonList.add(new GuiButton(114517, this.width / 2 + 2, this.height / 4 + 48 + 48, 98, 20, "AltManager"));
      this.buttonList.add(new GuiButton(114518, this.width / 2 + 2 + 102, this.height / 4 + 48 + 48, 44, 20, "Console"));
   }
}
