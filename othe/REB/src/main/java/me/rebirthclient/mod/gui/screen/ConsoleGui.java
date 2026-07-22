//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.screen;

import java.io.IOException;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

public class ConsoleGui extends GuiScreen {
   private GuiTextField command;
   private GuiButton complete;

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.complete = new GuiButton(2, this.width / 2 - 152, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0]));
      this.buttonList.add(this.complete);
      this.buttonList
         .add(new GuiButton(3, this.width / 2 + 2, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
      this.command = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
      this.command.setFocused(true);
      this.command.setMaxStringLength(64);
      this.complete.enabled = false;
   }

   public void drawScreen(int par1, int par2, float par3) {
      this.drawDefaultBackground();
      this.command.drawTextBox();
      super.drawScreen(par1, par2, par3);
   }

   protected void keyTyped(char character, int keyIndex) {
      if (keyIndex == 1) {
         this.escape();
      } else if (keyIndex == 28) {
         if (this.command.isFocused()) {
            this.command.setFocused(false);
         }
      } else if (keyIndex == 15) {
         this.command.setFocused(!this.command.isFocused());
      } else {
         this.command.textboxKeyTyped(character, keyIndex);
      }
   }

   public void updateScreen() {
      this.command.updateCursorCounter();
      this.complete.enabled = true;
   }

   protected void actionPerformed(GuiButton button) {
      if (button.enabled) {
         if (button.id == 2) {
            if (this.getCommand().length() > 1) {
               Managers.COMMANDS.executeCommand(this.getCommand().substring(Command.getCommandPrefix().length() - 1));
            } else {
               Command.sendMessage("Please enter a command.");
            }

            this.escape();
         } else if (button.id == 3) {
            this.escape();
         }
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.command.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   private void escape() {
      this.mc.displayGuiScreen(null);
   }

   public String getCommand() {
      return Command.getCommandPrefix() + this.command.getText();
   }
}
