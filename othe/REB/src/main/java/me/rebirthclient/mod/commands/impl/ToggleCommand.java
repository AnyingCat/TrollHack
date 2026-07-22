package me.rebirthclient.mod.commands.impl;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Module;

public class ToggleCommand extends Command {
   public ToggleCommand() {
      super("toggle", new String[]{"<module>"});
   }

   @Override
   public void execute(String[] commands) {
      if (commands.length == 2) {
         String name = commands[0].replaceAll("_", " ");
         Module module = Managers.MODULES.getModuleByName(name);
         if (module != null) {
            module.toggle();
         } else {
            Command.sendMessage("Unable to find a module with that name!");
         }
      } else {
         Command.sendMessage("Please provide a valid module name!");
      }
   }
}
