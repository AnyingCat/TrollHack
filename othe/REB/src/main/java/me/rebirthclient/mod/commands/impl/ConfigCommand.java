package me.rebirthclient.mod.commands.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;

public class ConfigCommand extends Command {
   public ConfigCommand() {
      super("config", new String[]{"<save/load>"});
   }

   @Override
   public void execute(String[] commands) {
      if (commands.length == 1) {
         sendMessage("You`ll find the config files in your gameProfile directory under Rebirth/config");
      } else {
         if (commands.length == 2) {
            if ("list".equals(commands[0])) {
               String configs = "Configs: ";
               File file = new File("Rebirth/");
               List<File> directories = Arrays.stream(file.listFiles())
                  .filter(File::isDirectory)
                  .filter(f -> !f.getName().equals("util"))
                  .collect(Collectors.toList());
               StringBuilder builder = new StringBuilder(configs);

               for(File file1 : directories) {
                  builder.append(file1.getName() + ", ");
               }

               configs = builder.toString();
               sendMessage(configs);
            } else {
               sendMessage("Not a valid command... Possible usage: <list>");
            }
         }

         if (commands.length >= 3) {
            String var9 = commands[0];
            switch(var9) {
               case "save":
                  Managers.CONFIGS.saveConfig(commands[1]);
                  sendMessage(ChatFormatting.GREEN + "Config '" + commands[1] + "' has been saved.");
                  return;
               case "load":
                  if (Managers.CONFIGS.configExists(commands[1])) {
                     Managers.CONFIGS.loadConfig(commands[1]);
                     sendMessage(ChatFormatting.GREEN + "Config '" + commands[1] + "' has been loaded.");
                  } else {
                     sendMessage(ChatFormatting.RED + "Config '" + commands[1] + "' does not exist.");
                  }

                  return;
               default:
                  sendMessage("Not a valid command... Possible usage: <save/load>");
            }
         }
      }
   }
}
