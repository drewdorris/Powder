package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;

public class ArrowCommand implements SubCommand {

	private String[] labels = {"arrow"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		if (!player.hasPermission("powder.arrow")) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}

		if (args.length < 2) {
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_SYNTAX, label, player.getName(), label);
			return;
		}
		String subCommand = args[1].toLowerCase();

		if (subCommand.equals("removetrail")) {
			ConfigUtil.unsetArrowTrail(player.getUniqueId());
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_TRAIL_REMOVED, label, player.getName());
			return;
		} else if (subCommand.equals("removehit")) {
			ConfigUtil.unsetArrowHit(player.getUniqueId());
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_HIT_REMOVED, label, player.getName());
			return;
		}

		if (args.length < 3) {
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_SYNTAX_POWDER, label, player.getName());
			return;
		}

		Powder powder = PowderPlugin.get().getPowderHandler().getPowder(args[2]);
		if (powder == null) {
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_UNKNOWN_POWDER, label, player.getName(), args[2]);
			return;
		}

		if (args.length >= 4) {
			if (args[3].toLowerCase().equals("loop")) powder = powder.loop();
		}

		// if no permission for the specific Powder
		if (!PowderUtil.hasPermission(player, powder)) {
			PowderUtil.sendPrefixMessage(player, Message.POWDER_NO_PERMISSION,
					label, args[0]);
			return;
		}

		if (subCommand.equals("trail")) {
			ConfigUtil.saveArrowTrail(player.getUniqueId(), powder);
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_TRAIL_SAVED, label, player.getName(), powder.getName());
		} else if (subCommand.equals("hit")) {
			ConfigUtil.saveArrowHit(player.getUniqueId(), powder);
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_HIT_SAVED, label, player.getName(), powder.getName());
		} else {
			PowderUtil.sendPrefixMessage(player,
					Message.ARROW_SYNTAX, label, player.getName(), label);
			return;
		}
	}

}
