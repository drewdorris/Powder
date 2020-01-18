package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.util.PowderUtil;

public class HelpCommand implements SubCommand {

	private String[] labels = {"help"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		int page;
		try {
			page = Integer.valueOf(args[1]);
		} catch (Exception e) {
			page = 1;
		}

		PowderUtil.helpMessage(player, label, page);
	}

}
