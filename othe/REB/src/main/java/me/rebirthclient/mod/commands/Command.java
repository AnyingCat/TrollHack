//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "G:\PortableSoft\JBY\MC_Deobf3000\1.12-MCP-Mappings"!

package me.rebirthclient.mod.commands;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.impl.hud.Notifications;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;

public abstract class Command extends Mod {
   protected final String name;
   protected final String[] commands;

   public Command(String name) {
      super(name);
      this.name = name;
      this.commands = new String[]{""};
   }

   public Command(String name, String[] commands) {
      super(name);
      this.name = name;
      this.commands = commands;
   }

   public static void sendMessage(String message) {
      Notifications.notifyList.add(new Notifications.Notifys(message));
      sendSilentMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message);
   }

   public static void sendSilentMessage(String message) {
      if (!nullCheck()) {
         mc.player.sendMessage(new Command.ChatMessage(message));
      }
   }

   public static String getCommandPrefix() {
      return Managers.COMMANDS.getCommandPrefix();
   }

   public static void sendMessageWithID(String message, int id) {
      if (!nullCheck()) {
         mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new Command.ChatMessage(Managers.TEXT.getPrefix() + ChatFormatting.GRAY + message), id);
      }
   }

   public abstract void execute(String[] var1);

   public String complete(String str) {
      if (this.name.toLowerCase().startsWith(str)) {
         return this.name;
      } else {
         for(String command : this.commands) {
            if (command.toLowerCase().startsWith(str)) {
               return command;
            }
         }

         return null;
      }
   }

   @Override
   public String getName() {
      return this.name;
   }

   public String[] getCommands() {
      return this.commands;
   }

   public static class ChatMessage extends TextComponentBase {
      private final String text;

      public ChatMessage(String text) {
         Pattern pattern = Pattern.compile("&[0123456789abcdefrlosmk]");
         Matcher matcher = pattern.matcher(text);
         StringBuffer stringBuffer = new StringBuffer();

         while(matcher.find()) {
            String replacement = matcher.group().substring(1);
            matcher.appendReplacement(stringBuffer, replacement);
         }

         matcher.appendTail(stringBuffer);
         this.text = stringBuffer.toString();
      }

      public String getUnformattedComponentText() {
         return this.text;
      }

      public ITextComponent createCopy() {
         return null;
      }

      public ITextComponent shallowCopy() {
         return new Command.ChatMessage(this.text);
      }
   }
}
