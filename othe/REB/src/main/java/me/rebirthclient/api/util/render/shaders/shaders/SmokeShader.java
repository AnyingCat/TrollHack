//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class SmokeShader extends FramebufferShader {
   private static SmokeShader INSTANCE;
   protected float time = 0.0F;

   private SmokeShader() {
      super("smoke.frag");
   }

   public static SmokeShader INSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new SmokeShader();
      }

      return INSTANCE;
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("time");
      this.setupUniform("resolution");
      this.setupUniform("radius");
      this.setupUniform("divider");
      this.setupUniform("maxSample");
      this.setupUniform("texelSize");
   }

   @Override
   public void updateUniforms() {
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform2f(
         this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth() / 2.0F, (float)new ScaledResolution(this.mc).getScaledHeight() / 2.0F
      );
      GL20.glUniform1f(this.getUniform("radius"), this.radius);
      GL20.glUniform1f(this.getUniform("divider"), this.divider);
      GL20.glUniform1f(this.getUniform("maxSample"), this.maxSample);
      GL20.glUniform2f(
         this.getUniform("texelSize"),
         1.0F / (float)this.mc.displayWidth * this.radius * this.quality,
         1.0F / (float)this.mc.displayHeight * this.radius * this.quality
      );
      if (this.animation) {
         this.time = this.time > 100.0F ? 0.0F : (float)((double)this.time + 0.05 * (double)this.animationSpeed);
      }
   }
}
