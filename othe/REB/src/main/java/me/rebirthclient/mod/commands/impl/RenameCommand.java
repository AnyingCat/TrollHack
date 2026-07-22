//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.asm.accessors.IMinecraft;
import me.rebirthclient.mod.commands.Command;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.Session;

public class RenameCommand extends Command {
   public RenameCommand() {
      super("rename", new String[]{"name"});
   }

   @Override
   public void execute(String[] commands) {
      if (commands.length == 1) {
         Command.sendMessage("rename <name>");
      } else {
         String name = commands[0];
         if (!name.contains(".") && !name.contains("*") && !name.contains("?") && !name.contains("+") && !name.contains("/")) {
            Session user = new Session(name, "uuid", "token", "legacy");
            ((IMinecraft)mc).setSession(user);
            Command.sendMessage(ChatFormatting.YELLOW + "New name: " + ChatFormatting.GREEN + name);
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(null));
         } else {
            Command.sendMessage(ChatFormatting.RED + "error name !!!");
         }
      }
   }
}
