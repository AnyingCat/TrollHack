//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import me.rebirthclient.mod.commands.Command;

public class CoordsCommand extends Command {
   String coords;

   public CoordsCommand() {
      super("coords");
   }

   @Override
   public void execute(String[] commands) {
      int posX = (int)mc.player.posX;
      int posY = (int)mc.player.posY;
      int posZ = (int)mc.player.posZ;
      this.coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;
      String myString = this.coords;
      StringSelection stringSelection = new StringSelection(myString);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
      Command.sendMessage(ChatFormatting.GRAY + "Coords copied.");
   }
}
