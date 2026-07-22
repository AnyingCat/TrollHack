//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoReconnect extends Module {
   public static AutoReconnect INSTANCE;
   private static ServerData serverData;
   private final Setting<Integer> delay = this.add(new Setting<>("Delay", 5));

   public AutoReconnect() {
      super("AutoReconnect", "Reconnects you if you disconnect", Category.MISC);
      INSTANCE = this;
   }

   @SubscribeEvent
   public void sendPacket(GuiOpenEvent event) {
      if (event.getGui() instanceof GuiDisconnected) {
         this.updateLastConnectedServer();
         GuiDisconnected disconnected = (GuiDisconnected)event.getGui();
         event.setGui(new AutoReconnect.GuiDisconnectedHook(disconnected));
      }
   }

   @SubscribeEvent
   public void onWorldUnload(Unload event) {
      this.updateLastConnectedServer();
   }

   public void updateLastConnectedServer() {
      ServerData data = mc.getCurrentServerData();
      if (data != null) {
         serverData = data;
      }
   }

   private class GuiDisconnectedHook extends GuiDisconnected {
      private final Timer timer = new Timer();

      public GuiDisconnectedHook(GuiDisconnected disconnected) {
         super(disconnected.parentScreen, disconnected.reason, disconnected.message);
         this.timer.reset();
      }

      public void updateScreen() {
         if (this.timer.passedS((double)AutoReconnect.this.delay.getValue().intValue())) {
            this.mc
               .displayGuiScreen(
                  new GuiConnecting(
                     this.parentScreen, this.mc, AutoReconnect.serverData == null ? this.mc.currentServerData : AutoReconnect.serverData
                  )
               );
         }
      }

      public void drawScreen(int mouseX, int mouseY, float partialTicks) {
         super.drawScreen(mouseX, mouseY, partialTicks);
         String reconnectString = "Reconnecting in "
            + MathUtil.round((double)((long)(AutoReconnect.this.delay.getValue() * 1000) - this.timer.getPassedTimeMs()) / 1000.0, 1);
         this.mc
            .fontRenderer
            .drawString(
               reconnectString,
               (float)this.width / 2.0F - (float)this.mc.fontRenderer.getStringWidth(reconnectString) / 2.0F,
               (float)(this.height - 16),
               16777215,
               true
            );
      }
   }
}
