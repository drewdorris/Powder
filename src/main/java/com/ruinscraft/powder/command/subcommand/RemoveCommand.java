package com.ruinscraft.powder.command.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

public class RemoveCommand implements SubCommand {

	private String[] labels = {"remove"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (!(player.hasPermission("powder.remove"))) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}
		if (args.length < 2) {
			PowderUtil.sendPrefixMessage(player, Message.REMOVE_SYNTAX,
					label, player.getName(), label);
			return;
		}
		if (args[1].equalsIgnoreCase("user")) {
			Player powderUser;
			try {
				powderUser = Bukkit.getPlayer(args[2]);
			} catch (Exception e) {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_INVALID_PLAYER,
						label, player.getName());
				return;
			}
			if (powderUser == null) {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_INVALID_PLAYER,
						label, player.getName());
				return;
			}
			if (!(player.hasPermission("powder.removeany") && !powderUser.equals(player))) {
				PowderUtil.sendPrefixMessage(player,
						Message.GENERAL_NO_PERMISSION, label, player.getName());
				return;
			}
			if (powderHandler.cancelPowderTasks(
					powderHandler.getPowderTasks(powderUser.getUniqueId()))) {
				if (!(powderUser.equals(player))) {
					PowderUtil.sendPrefixMessage(powderUser, Message.REMOVE_USER_REMOVED_BY,
							label, player.getName(), powderUser.getName());
				}
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_USER_REMOVE_SUCCESS,
						label, player.getName(), powderUser.getName());
			} else {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_USER_REMOVE_FAILURE,
						label, player.getName(), powderUser.getName());
				return;
			}
			return;
		} else {
			String name;
			try {
				name = args[1];
			} catch (Exception e) {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_SYNTAX,
						label, player.getName(), label);
				return;
			}

			PowderTask powderTask = powderHandler.getPowderTask(name);
			if (powderTask == null) {
				PowderUtil.sendPrefixMessage(player,
						Message.REMOVE_NO_USER_DOES_NOT_EXIST,
						label, player.getName(), name);
				return;
			}
			if (!(player.hasPermission("powder.removeany"))) {
				if (!powderTask.getTracker().hasControl(player)) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return;
				}
			}
			if (powderHandler.cancelPowderTask(powderTask)) {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_SUCCESS,
						label, player.getName(), name);
			} else {
				PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_FAILURE,
						label, player.getName(), name);
				return;
			}
			return;
		}
	}

}
