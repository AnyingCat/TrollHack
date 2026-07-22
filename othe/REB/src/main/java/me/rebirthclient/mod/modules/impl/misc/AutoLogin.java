//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;

public class AutoLogin extends Module {
   final Timer timer = new Timer();
   private final Setting<String> password = this.add(new Setting<>("password", "password"));
   boolean needLogin = false;

   public AutoLogin() {
      super("AutoLogin", "Auto login", Category.MISC);
   }

   @Override
   public void onTick() {
      if (this.needLogin && this.timer.passedMs(1000L)) {
         this.needLogin = false;
         mc.player.connection.sendPacket(new CPacketChatMessage("/login " + (String)this.password.getValue()));
      }
   }

   @Override
   public void onLogin() {
      this.needLogin = true;
      this.timer.reset();
   }
}
