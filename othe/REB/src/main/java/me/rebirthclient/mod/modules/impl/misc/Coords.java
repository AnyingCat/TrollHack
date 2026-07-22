//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.modules.impl.misc;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;

public class Coords extends Module {
   public Coords() {
      super("Coords", "copies your current position to the clipboard", Category.MISC);
   }

   @Override
   public void onEnable() {
      int posX = (int)mc.player.posX;
      int posY = (int)mc.player.posY;
      int posZ = (int)mc.player.posZ;
      String coords = "X: " + posX + " Y: " + posY + " Z: " + posZ;
      StringSelection stringSelection = new StringSelection(coords);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, null);
      this.disable();
   }
}
