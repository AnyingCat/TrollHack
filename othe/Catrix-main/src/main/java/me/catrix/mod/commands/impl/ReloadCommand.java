package me.catrix.mod.commands.impl;

import me.catrix.Catrix;
import me.catrix.core.impl.CommandManager;
import me.catrix.core.impl.ConfigManager;
import me.catrix.mod.commands.Command;

import java.util.List;

public class ReloadCommand extends Command {

	public ReloadCommand() {
		super("reload", "");
	}

	@Override
	public void runCommand(String[] parameters) {
		CommandManager.sendChatMessage("§fReloading..");
		Catrix.CONFIG = new ConfigManager();
		Catrix.PREFIX = Catrix.CONFIG.getString("prefix", Catrix.PREFIX);
		Catrix.CONFIG.loadSettings();
		Catrix.XRAY.read();
		Catrix.TRADE.read();
		Catrix.FRIEND.read();
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return null;
	}
}
