package com.ruinscraft.powder.command.subcommand;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.util.ConfigUtil;

public class ArrowCommand implements SubCommand {

	private String[] labels = {"arrow"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			// msg
			player.sendMessage("Please enter a subcommand. Try /powder arrow trail <Powder>");
			return;
		}
		String subCommand = args[1].toLowerCase();

		if (args.length < 3) {
			// msg
			player.sendMessage("Please enter a Powder");
			return;
		}

		Powder powder = PowderPlugin.get().getPowderHandler().getPowder(args[2]);
		if (powder == null) {
			// msg
			player.sendMessage("Unknown Powder");
			return;
		}

		if (args.length >= 4) {
			if (args[3].toLowerCase().equals("loop")) powder = powder.loop();
		}

		if (subCommand.equals("trail")) {
			ConfigUtil.saveArrowTrail(player.getUniqueId(), powder);
			player.sendMessage("Arrow trail Powder saved");
		} else if (subCommand.equals("hit")) {
			ConfigUtil.saveArrowHit(player.getUniqueId(), powder);
			player.sendMessage("Arrow hit Powder saved");
		} else if (subCommand.equals("removetrail")) {
			ConfigUtil.unsetArrowTrail(player.getUniqueId());
			player.sendMessage("Arrow trail removed.");
		} else if (subCommand.equals("removehit")) {
			ConfigUtil.unsetArrowHit(player.getUniqueId());
			player.sendMessage("Arrow hit removed.");
		} else {
			// msg
			player.sendMessage("Unknown command. Try /powder arrow trail <Powder>");
			return;
		}
	}

}
