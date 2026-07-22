//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.play.client.CPacketChatMessage;

public class AutoKit extends Module {
   public final Setting<String> Name = this.add(new Setting<>("KitName", "1"));
   boolean needKit = false;

   public AutoKit() {
      super("AutoKit", "Auto select kit", Category.MISC);
   }

   @Override
   public void onUpdate() {
      if (mc.currentScreen instanceof GuiGameOver) {
         this.needKit = true;
      } else if (this.needKit) {
         mc.player.connection.sendPacket(new CPacketChatMessage("/kit " + (String)this.Name.getValue()));
         this.needKit = false;
      }
   }
}
