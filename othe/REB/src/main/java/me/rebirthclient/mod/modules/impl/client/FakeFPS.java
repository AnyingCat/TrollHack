//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings.Options;

public class FakeFPS extends Module {
   public static FakeFPS INSTANCE;
   public final Setting<Integer> times = this.add(new Setting<>("times", 5, 1, 100));
   int lastFps = 0;

   public FakeFPS() {
      super("FakeFPS", "FakeFPS", Category.CLIENT);
      INSTANCE = this;
   }

   @Override
   public void onRender3D(Render3DEvent event) {
      Minecraft.getMinecraft().debug = String.format(
         "%d fps (%d chunk update%s) T: %s%s%s%s%s",
         Minecraft.getDebugFPS(),
         RenderChunk.renderChunksUpdated,
         RenderChunk.renderChunksUpdated == 1 ? "" : "s",
         (float)Minecraft.getMinecraft().gameSettings.limitFramerate == Options.FRAMERATE_LIMIT.getValueMax()
            ? "inf"
            : Minecraft.getMinecraft().gameSettings.limitFramerate,
         Minecraft.getMinecraft().gameSettings.enableVsync ? " vsync" : "",
         Minecraft.getMinecraft().gameSettings.fancyGraphics ? "" : " fast",
         Minecraft.getMinecraft().gameSettings.clouds == 0
            ? ""
            : (Minecraft.getMinecraft().gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"),
         OpenGlHelper.useVbo() ? " vbo" : ""
      );
      if (Minecraft.getDebugFPS() != this.lastFps) {
         Minecraft.debugFPS *= this.times.getValue();
         this.lastFps = Minecraft.getDebugFPS();
      }
   }
}
