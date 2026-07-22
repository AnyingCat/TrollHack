//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.api.managers.impl;

import java.util.LinkedList;
import net.minecraft.client.Minecraft;

public final class FpsManager {
   private final LinkedList<Long> frames = new LinkedList<>();
   private int fps;

   public void update() {
      long time = System.nanoTime();
      this.frames.add(time);

      while(true) {
         long f = this.frames.getFirst();
         long ONE_SECOND = 1000000000L;
         if (time - f <= 1000000000L) {
            this.fps = this.frames.size();
            return;
         }

         this.frames.remove();
      }
   }

   public int getFPS() {
      return this.fps;
   }

   public int getMcFPS() {
      return Minecraft.getDebugFPS();
   }

   public float getFrametime() {
      return 0.004166667F;
   }
}
