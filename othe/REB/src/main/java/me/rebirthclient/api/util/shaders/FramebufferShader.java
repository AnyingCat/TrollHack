//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.shaders;

import java.awt.Color;
import me.rebirthclient.asm.accessors.IEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FramebufferShader extends Shader {
   protected float quality;
   protected float green;
   protected float alpha = 1.0F;
   protected boolean entityShadows;
   protected float radius = 2.0F;
   protected Framebuffer framebuffer;
   protected float red;
   protected Minecraft mc;
   protected float blue;

   public void stopDraw(Color color, float f, float f2, float f3) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.red = (float)color.getRed() / 255.0F;
      this.green = (float)color.getGreen() / 255.0F;
      this.blue = (float)color.getBlue() / 255.0F;
      this.alpha = (float)color.getAlpha() / 255.0F;
      this.radius = f;
      this.quality = f2;
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      this.startShader(f3);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public Framebuffer setupFrameBuffer(Framebuffer framebuffer) {
      if (framebuffer == null) {
         return new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
      } else {
         if (framebuffer.framebufferWidth != this.mc.displayWidth || framebuffer.framebufferHeight != this.mc.displayHeight) {
            framebuffer.deleteFramebuffer();
            framebuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
         }

         return framebuffer;
      }
   }

   public void startDraw(float f) {
      GlStateManager.enableAlpha();
      GlStateManager.pushMatrix();
      GlStateManager.pushAttrib();
      this.framebuffer = this.setupFrameBuffer(this.framebuffer);
      this.framebuffer.framebufferClear();
      this.framebuffer.bindFramebuffer(true);
      this.entityShadows = this.mc.gameSettings.entityShadows;
      this.mc.gameSettings.entityShadows = false;
      ((IEntityRenderer)this.mc.entityRenderer).invokeSetupCameraTransform(f, 0);
   }

   public void drawFramebuffer(Framebuffer framebuffer) {
      ScaledResolution scaledResolution = new ScaledResolution(this.mc);
      GL11.glBindTexture(3553, framebuffer.framebufferTexture);
      GL11.glBegin(7);
      GL11.glTexCoord2d(0.0, 1.0);
      GL11.glVertex2d(0.0, 0.0);
      GL11.glTexCoord2d(0.0, 0.0);
      GL11.glVertex2d(0.0, (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 0.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), (double)scaledResolution.getScaledHeight());
      GL11.glTexCoord2d(1.0, 1.0);
      GL11.glVertex2d((double)scaledResolution.getScaledWidth(), 0.0);
      GL11.glEnd();
      GL20.glUseProgram(0);
   }

   public FramebufferShader(String string) {
      super(string);
      this.quality = 1.0F;
      this.mc = Minecraft.getMinecraft();
   }
}
