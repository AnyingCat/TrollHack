//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class HolyFuckShader extends FramebufferShader {
   private static HolyFuckShader INSTANCE;
   protected float time = 0.0F;

   private HolyFuckShader() {
      super("holyfuck.frag");
   }

   public static HolyFuckShader INSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new HolyFuckShader();
      }

      return INSTANCE;
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("time");
      this.setupUniform("resolution");
      this.setupUniform("speed");
      this.setupUniform("shift");
      this.setupUniform("color");
      this.setupUniform("radius");
      this.setupUniform("quality");
      this.setupUniform("divider");
      this.setupUniform("texelSize");
      this.setupUniform("maxSample");
   }

   @Override
   public void updateUniforms() {
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth(), (float)new ScaledResolution(this.mc).getScaledHeight());
      GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
      GL20.glUniform1f(this.getUniform("radius"), this.radius);
      GL20.glUniform1f(this.getUniform("quality"), this.quality);
      GL20.glUniform1f(this.getUniform("divider"), this.divider);
      GL20.glUniform2f(this.getUniform("speed"), (float)this.animationSpeed, (float)this.animationSpeed);
      GL20.glUniform1f(this.getUniform("shift"), 1.0F);
      GL20.glUniform1f(this.getUniform("maxSample"), this.maxSample);
      if (this.animation) {
         this.time = this.time > 100.0F ? 0.0F : this.time + 0.01F * (float)this.animationSpeed;
      }
   }
}
