//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.shaders.impl.outline;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.util.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class SmokeOutlineShader extends FramebufferShader {
   public float time = 0.0F;
   public static final SmokeOutlineShader INSTANCE = new SmokeOutlineShader();

   public void update(double d) {
      this.time = (float)((double)this.time + d);
   }

   public void stopDraw(Color color, float f, float f2, boolean bl, int n, float f3, Color color2, Color color3, int n2) {
      this.mc.gameSettings.entityShadows = this.entityShadows;
      this.framebuffer.unbindFramebuffer();
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      this.mc.getFramebuffer().bindFramebuffer(true);
      this.mc.entityRenderer.disableLightmap();
      RenderHelper.disableStandardItemLighting();
      this.startShader(color, f, f2, bl, n, f3, color2, color3, n2);
      this.mc.entityRenderer.setupOverlayRendering();
      this.drawFramebuffer(this.framebuffer);
      this.stopShader();
      this.mc.entityRenderer.disableLightmap();
      GlStateManager.popMatrix();
      GlStateManager.popAttrib();
   }

   public SmokeOutlineShader() {
      super("smokeOutline.frag");
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("texture");
      this.setupUniform("texelSize");
      this.setupUniform("divider");
      this.setupUniform("radius");
      this.setupUniform("maxSample");
      this.setupUniform("alpha0");
      this.setupUniform("resolution");
      this.setupUniform("time");
      this.setupUniform("first");
      this.setupUniform("second");
      this.setupUniform("third");
      this.setupUniform("oct");
   }

   public void startShader(Color color, float f, float f2, boolean bl, int n, float f3, Color color2, Color color3, int n2) {
      GL11.glPushMatrix();
      GL20.glUseProgram(this.program);
      if (this.uniformsMap == null) {
         this.uniformsMap = new HashMap<>();
         this.setupUniforms();
      }

      this.updateUniforms(color, f, f2, bl, n, f3, color2, color3, n2);
   }

   public void updateUniforms(Color color, float f, float f2, boolean bl, int n, float f3, Color color2, Color color3, int n2) {
      GL20.glUniform1i(this.getUniform("texture"), 0);
      GL20.glUniform2f(this.getUniform("texelSize"), 1.0F / (float)this.mc.displayWidth * f * f2, 1.0F / (float)this.mc.displayHeight * f * f2);
      GL20.glUniform1f(this.getUniform("divider"), 140.0F);
      GL20.glUniform1f(this.getUniform("radius"), f);
      GL20.glUniform1f(this.getUniform("maxSample"), 10.0F);
      GL20.glUniform1f(this.getUniform("alpha0"), bl ? -1.0F : (float)n / 255.0F);
      GL20.glUniform2f(
         this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth() / f3, (float)new ScaledResolution(this.mc).getScaledHeight() / f3
      );
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform4f(
         this.getUniform("first"),
         (float)color.getRed() / 255.0F * 5.0F,
         (float)color.getGreen() / 255.0F * 5.0F,
         (float)color.getBlue() / 255.0F * 5.0F,
         (float)color.getAlpha() / 255.0F
      );
      GL20.glUniform3f(
         this.getUniform("second"), (float)color2.getRed() / 255.0F * 5.0F, (float)color2.getGreen() / 255.0F * 5.0F, (float)color2.getBlue() / 255.0F * 5.0F
      );
      GL20.glUniform3f(
         this.getUniform("third"), (float)color3.getRed() / 255.0F * 5.0F, (float)color3.getGreen() / 255.0F * 5.0F, (float)color3.getBlue() / 255.0F * 5.0F
      );
      GL20.glUniform1i(this.getUniform("oct"), n2);
   }
}
