package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.util.PowderUtil;

public class AddtoCommand implements SubCommand {

	private String[] labels = {"addto"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (!(player.hasPermission("powder.addto"))) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}
		String name;
		String powderName;
		Powder newPowder;
		try {
			name = args[1];
			powderName = args[2];
			newPowder = powderHandler.getPowder(powderName);
		} catch (Exception e) {
			PowderUtil.sendPrefixMessage(player, Message.ADDTO_SYNTAX, label,
					player.getName(), label);
			return;
		}
		if ((powderHandler.getPowderTask(name) == null)) {
			PowderUtil.sendPrefixMessage(player, Message.ADDTO_DOES_NOT_EXIST,
					label, player.getName(), name);
			return;
		}
		if (newPowder == null) {
			PowderUtil.sendPrefixMessage(player, Message.ADDTO_UNKNOWN,
					label, player.getName(), powderName);
			return;
		}
		PowderTask powderTask = powderHandler.getPowderTask(name);
		if (powderTask.addPowder(newPowder,
				new StationaryTracker(player.getLocation(), player.getUniqueId()))) {
			PowderUtil.sendPrefixMessage(player, Message.ADDTO_SUCCESS,
					label, player.getName(), powderName, name);
		} else {
			PowderUtil.sendPrefixMessage(player, Message.ADDTO_FAILURE,
					label, player.getName(), powderName, name);
			return;
		}
	}

}
