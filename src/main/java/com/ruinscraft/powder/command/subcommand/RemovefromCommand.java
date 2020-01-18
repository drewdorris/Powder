package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

public class RemovefromCommand implements SubCommand {

	private String[] labels = {"removefrom"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (!(player.hasPermission("powder.removefrom"))) {
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
			PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_SYNTAX,
					label, player.getName(), label);
			return;
		}
		if ((powderHandler.getPowderTask(name) == null)) {
			PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_DOES_NOT_EXIST,
					label, player.getName(), name);
			return;
		}
		if (newPowder == null) {
			PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_UNKNOWN,
					label, player.getName(), powderName);
			return;
		}
		PowderTask powderTask = powderHandler.getPowderTask(name);
		if (powderTask.removePowder(newPowder)) {
			PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_SUCCESS,
					label, player.getName(), powderName, name);
		} else {
			PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_FAILURE,
					label, player.getName(), powderName, name);
			return;
		}
	}

}
