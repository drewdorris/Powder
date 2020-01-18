package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.util.PowderUtil;

public class StarCommand implements SubCommand {

	private String[] labels = {"*"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (args.length < 2) {
			PowderUtil.sendPrefixMessage(player, Message.STAR_USE_CANCEL,
					label, player.getName());
			return;
		} else {
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
	}

}
