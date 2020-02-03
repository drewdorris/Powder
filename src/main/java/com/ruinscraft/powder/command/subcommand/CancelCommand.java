package com.ruinscraft.powder.command.subcommand;

import java.util.Set;

import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

public class CancelCommand implements SubCommand {

	private String[] labels = {"cancel"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		String powderTaskName;
		try {
			powderTaskName = args[1];
		} catch (Exception e) {
			// cancel all Powders
			if (powderHandler.getPowderTasks(player.getUniqueId()).isEmpty()) {
				PowderUtil.sendPrefixMessage(player,
						Message.STAR_NO_ACTIVE, label, player.getName());
				return;
			}

			int amount = PowderUtil.cancelAllPowders(player.getUniqueId());
			PowderUtil.sendPrefixMessage(player, Message.STAR_CANCEL_SUCCESS,
					label, player.getName(), String.valueOf(amount));
			return;
		}
		if (powderHandler.getPowder(powderTaskName) != null) {
			Set<PowderTask> powderTasks = powderHandler.getPowderTasks(
					player.getUniqueId(),
					powderHandler.getPowder(powderTaskName));
			if (powderTasks.isEmpty()) {
				PowderUtil.sendPrefixMessage(player,
						Message.CANCEL_NO_ACTIVE, label, player.getName(),
						powderTaskName);
				return;
			} else {
				powderTaskName = Iterables.get(powderTasks, 0).getName();
			}
		}
		PowderTask powderTask = powderHandler.getPowderTask(powderTaskName);
		if (!player.hasPermission("powder.cancel")) {
			if (!powderTask.getTracker().hasControl(player)) {
				PowderUtil.sendPrefixMessage(player, Message.CANCEL_FAILURE,
						label, player.getName(), powderTaskName);
				return;
			}
		}
		if (powderTask == null) {
			PowderUtil.sendPrefixMessage(player, Message.CANCEL_UNKNOWN_SPECIFY,
					label, player.getName(), powderTaskName);
			return;
		}
		if (powderHandler.cancelPowderTask(powderTask)) {
			PowderUtil.sendPrefixMessage(player, Message.CANCEL_SUCCESS,
					label, player.getName(), powderTaskName);
		} else {
			PowderUtil.sendPrefixMessage(player, Message.CANCEL_FAILURE,
					label, player.getName(), powderTaskName);
		}
	}

}
