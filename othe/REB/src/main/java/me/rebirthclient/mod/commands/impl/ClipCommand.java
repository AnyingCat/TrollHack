//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.commands.impl;

import java.util.regex.Pattern;
import me.rebirthclient.mod.commands.Command;

public class ClipCommand extends Command {
   public ClipCommand() {
      super("clip", new String[]{"<x>", "<y>", "<z>"});
   }

   @Override
   public void execute(String[] commands) {
      if (commands.length < 4) {
         Command.sendMessage("clip <x> <y> <z>");
      } else if (this.isInteger(commands[0]) && this.isInteger(commands[1]) && this.isInteger(commands[2])) {
         int x = Integer.parseInt(commands[0]);
         int y = Integer.parseInt(commands[1]);
         int z = Integer.parseInt(commands[2]);
         Command.sendMessage(
            "clip to "
               + (int)(mc.player.posX + (double)x)
               + " "
               + (int)(mc.player.posY + (double)y)
               + " "
               + (int)(mc.player.posZ + (double)z)
         );
         mc.player
            .setPosition(mc.player.posX + (double)x, mc.player.posY + (double)y, mc.player.posZ + (double)z);
      } else {
         Command.sendMessage("invalid value!");
      }
   }

   private boolean isInteger(String str) {
      Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
      return pattern.matcher(str).matches();
   }
}
