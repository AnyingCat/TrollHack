//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import org.lwjgl.opengl.GL20;

public class GlowShader extends FramebufferShader {
   private static GlowShader INSTANCE;

   private GlowShader() {
      super("glow.frag");
   }

   public static GlowShader INSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new GlowShader();
      }

      return INSTANCE;
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("texture");
      this.setupUniform("texelSize");
      this.setupUniform("color");
      this.setupUniform("divider");
      this.setupUniform("radius");
      this.setupUniform("maxSample");
   }

   @Override
   public void updateUniforms() {
      GL20.glUniform1i(this.getUniform("texture"), 0);
      GL20.glUniform2f(
         this.getUniform("texelSize"),
         Float.intBitsToFloat(Float.floatToIntBits(1531.2186F) ^ 2067752703) / (float)this.mc.displayWidth * this.radius * this.quality,
         Float.intBitsToFloat(Float.floatToIntBits(103.132805F) ^ 2102281215) / (float)this.mc.displayHeight * this.radius * this.quality
      );
      GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
      GL20.glUniform1f(this.getUniform("divider"), Float.intBitsToFloat(Float.floatToIntBits(0.060076397F) ^ 2121929387));
      GL20.glUniform1f(this.getUniform("radius"), this.radius);
      GL20.glUniform1f(this.getUniform("maxSample"), Float.intBitsToFloat(Float.floatToIntBits(0.08735179F) ^ 2090001791));
   }
}
