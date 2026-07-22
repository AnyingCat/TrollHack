//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class BasicShader extends FramebufferShader {
   private static BasicShader INSTANCE;
   private float time = 0.0F;
   private final float timeMult;

   private BasicShader(String fragmentShader) {
      super(fragmentShader);
      this.timeMult = 0.1F;
   }

   private BasicShader(String fragmentShader, float timeMult) {
      super(fragmentShader);
      this.timeMult = timeMult;
   }

   public static FramebufferShader INSTANCE(String fragmentShader) {
      if (INSTANCE == null || !INSTANCE.fragmentShader.equals(fragmentShader)) {
         INSTANCE = new BasicShader(fragmentShader);
      }

      return INSTANCE;
   }

   public static FramebufferShader INSTANCE(String fragmentShader, float timeMult) {
      if (INSTANCE == null || !INSTANCE.fragmentShader.equals(fragmentShader)) {
         INSTANCE = new BasicShader(fragmentShader, timeMult);
      }

      return INSTANCE;
   }

   @Override
   public void setupUniforms() {
      this.setupUniform("time");
      this.setupUniform("resolution");
   }

   @Override
   public void updateUniforms() {
      GL20.glUniform1f(this.getUniform("time"), this.time);
      GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth(), (float)new ScaledResolution(this.mc).getScaledHeight());
      if (this.animation) {
         int timeLimit = 10000;
         this.time = this.time > 10000.0F ? 0.0F : this.time + this.timeMult * (float)this.animationSpeed;
      }
   }
}
