package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.util.PowderUtil;

public class CategoryCommand implements SubCommand {

	private String[] labels = {"category"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.get().getConfig().getInt("pageLength");

		if (!powderHandler.categoriesEnabled()) {
			PowderUtil.sendPrefixMessage(player,
					Message.CATEGORIES_NOT_ENABLED, label, player.getName());
			PowderUtil.listPowders(player, powderHandler.getPowders(),
					" list ", 1, pageLength, label);
			return;
		}

		String category;
		int page;
		try {
			category = args[1];
		} catch (Exception e) {
			PowderUtil.sendPrefixMessage(player,
					Message.CATEGORY_SYNTAX, label, player.getName(), label);
			return;
		}

		try {
			page = Integer.valueOf(args[2]);
		} catch (Exception e) {
			page = 1;
		}

		// list categories with similar names if given category does not exist
		if (powderHandler.getCategory(category) == null) {
			PowderUtil.sendPrefixMessage(player,
					Message.CATEGORY_UNKNOWN, label, player.getName(), category);
			PowderUtil.listCategories(player, powderHandler.getSimilarCategories(category),
					" category " + category + " ", page, pageLength, label);
			// else, list Powders by category
		} else {
			String correctCategory = powderHandler.getCategory(category);
			PowderUtil.listPowders(player,
					powderHandler.getPowdersFromCategory(correctCategory),
					" category " + category + " ", page, pageLength, label);
		}
	}

}
