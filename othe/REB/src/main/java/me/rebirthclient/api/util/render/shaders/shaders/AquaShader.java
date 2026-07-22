//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class AquaShader extends FramebufferShader {
   private static AquaShader INSTANCE;
   public float time = 0.0F;

   private AquaShader() {
      super("aqua.frag");
   }

   public static FramebufferShader INSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new AquaShader();
      }

      return INSTANCE;
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("resolution");
      this.setupUniform("time");
   }

   @Override
   public void updateUniforms() {
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth(), (float)new ScaledResolution(this.mc).getScaledHeight());
      if (this.animation) {
         this.time = this.time > 100.0F ? 0.0F : (float)((double)this.time + 0.01 * (double)this.animationSpeed);
      }
   }
}
