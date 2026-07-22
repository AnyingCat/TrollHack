package me.catrix.mod.commands.impl;

import me.catrix.Catrix;
import me.catrix.core.Manager;
import me.catrix.core.impl.CommandManager;
import me.catrix.core.impl.ConfigManager;
import me.catrix.mod.commands.Command;

import java.util.List;

public class LoadCommand extends Command {

	public LoadCommand() {
		super("load", "[config]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length == 0) {
			sendUsage();
			return;
		}
		CommandManager.sendChatMessage("§fLoading..");
		ConfigManager.options = Manager.getFile(parameters[0] + ".cfg");
		Catrix.CONFIG = new ConfigManager();
		Catrix.PREFIX = Catrix.CONFIG.getString("prefix", Catrix.PREFIX);
		Catrix.CONFIG.loadSettings();
        ConfigManager.options = Manager.getFile("options.txt");
		Catrix.save();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
