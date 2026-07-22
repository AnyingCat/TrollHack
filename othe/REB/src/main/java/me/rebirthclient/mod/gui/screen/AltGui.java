//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.gui.screen;

import java.io.IOException;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

public class AltGui extends GuiScreen {
   private GuiTextField username;
   private GuiButton complete;

   public void initGui() {
      Keyboard.enableRepeatEvents(true);
      this.buttonList.clear();
      this.complete = new GuiButton(2, this.width / 2 - 152, this.height - 28, 150, 20, I18n.format("gui.done", new Object[0]));
      this.buttonList.add(this.complete);
      this.buttonList
         .add(new GuiButton(3, this.width / 2 + 2, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));
      this.username = new GuiTextField(0, this.fontRenderer, this.width / 2 - 100, 60, 200, 20);
      this.username.setFocused(true);
      this.username.setMaxStringLength(64);
      this.complete.enabled = false;
   }

   public void drawScreen(int par1, int par2, float par3) {
      this.drawDefaultBackground();
      this.drawCenteredString(this.fontRenderer, "Username", this.width / 2 - 130, 66, -1);
      this.username.drawTextBox();
      super.drawScreen(par1, par2, par3);
   }

   protected void keyTyped(char character, int keyIndex) {
      if (keyIndex == 1) {
         this.escape();
      } else if (keyIndex == 28) {
         if (this.username.isFocused()) {
            this.username.setFocused(false);
         }
      } else if (keyIndex == 15) {
         this.username.setFocused(!this.username.isFocused());
      } else {
         this.username.textboxKeyTyped(character, keyIndex);
      }
   }

   public void updateScreen() {
      this.username.updateCursorCounter();
      this.complete.enabled = true;
   }

   protected void actionPerformed(GuiButton button) {
      if (button.enabled) {
         if (button.id == 2) {
            Session session = new Session(this.getUsername(), this.getUsername(), "0", "legacy");

            try {
               setSession(session);
            } catch (Exception var4) {
               var4.printStackTrace();
            }

            this.escape();
         } else if (button.id == 3) {
            this.escape();
         }
      }
   }

   public static void setSession(Session s) throws Exception {
      Class<?> mc = Minecraft.getMinecraft().getClass();

      try {
         Field session = null;

         for(Field f : mc.getDeclaredFields()) {
            if (f.getType().isInstance(s)) {
               session = f;
               System.out.println("Found field " + f + ", injecting...");
            }
         }

         if (session == null) {
            throw new IllegalStateException("No field of type " + Session.class.getCanonicalName() + " declared.");
         } else {
            session.setAccessible(true);
            session.set(Minecraft.getMinecraft(), s);
            session.setAccessible(false);
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         throw var7;
      }
   }

   protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      this.username.mouseClicked(mouseX, mouseY, mouseButton);
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   private void escape() {
      this.mc.displayGuiScreen(null);
   }

   public String getUsername() {
      return this.username.getText();
   }
}
