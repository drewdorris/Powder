package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.util.PowderUtil;

public class SearchCommand implements SubCommand {

	private String[] labels = {"search"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.get().getConfig().getInt("pageLength");

		String search;
		int page;
		try {
			search = args[1];
		} catch (Exception e) {
			PowderUtil.sendPrefixMessage(player,
					Message.SEARCH_SYNTAX, label, player.getName(), label);
			return;
		}
		try {
			page = Integer.valueOf(args[2]);
		} catch (Exception e) {
			page = 1;
		}
		PowderUtil.listPowders(player, powderHandler.getSimilarPowders(search),
				" search " + search + " ", page, pageLength, label);
	}

}
