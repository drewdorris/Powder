package com.ruinscraft.powder.command.subcommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class CreateCommand implements SubCommand {

	private List<Player> recentCommandSenders = new ArrayList<>();

	private String[] labels = {"create"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (!player.hasPermission("powder.create")) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}
		// do this before the loading of the powder and such
		if (!player.hasPermission("powder.createany")) {
			// wait time between creating each Powder
			int waitTime = PowderPlugin.get().getConfig().getInt("secondsBetweenPowderUsage");
			// if they sent a command in the given wait time, don't do it
			if (recentCommandSenders.contains(player)) {
				PowderUtil.sendPrefixMessage(player, Message.POWDER_WAIT,
						label, player.getName(), args[0], String.valueOf(waitTime));
				return;
			}
			// if there's a wait time between using each Powder
			if (waitTime > 0) {
				// add user to this list of recent command senders for the given amount of time
				PowderPlugin.get().getServer().getScheduler()
				.scheduleSyncDelayedTask(PowderPlugin.get(), () -> {
					recentCommandSenders.remove(player);
				}, (waitTime * 20));
				recentCommandSenders.add(player);
			}
		}
		String name;
		String powderName;
		Powder newPowder;
		try {
			name = args[1];
			powderName = args[2];
		} catch (Exception e) {
			PowderUtil.sendPrefixMessage(player, Message.CREATE_SYNTAX,
					label, player.getName(), label);

			int page = 1;
			try {
				page = Integer.valueOf(args[1]);
			} catch (Exception ee) {
				page = 1;
			}

			List<PowderTask> created =
					powderHandler.getCreatedPowderTasks(player);
			List<BaseComponent> createdText = new ArrayList<>();

			for (PowderTask powderTask : created) {
				boolean hasPermissionForPowder = false;
				String location = null;
				if (powderTask.getTracker().hasControl(player)) {
					hasPermissionForPowder = true;
					location = powderTask.getTracker().getFormattedLocation();
				}

				BaseComponent baseComponent = PowderUtil.setText(Message.CREATED, powderTask.getName(), location);

				if (player.hasPermission("powder.removeany") || 
						(player.hasPermission("powder.remove") && hasPermissionForPowder)) {
					baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(PowderUtil.setString(
									Message.CREATED_HOVER, powderTask.getName(),
									powderTask.getPowder().getName()))
							.create()));
					baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
							PowderUtil.setString(Message.CREATED_CLICK,
									label, powderTask.getName())));
				}
				createdText.add(baseComponent);
			}
			PowderUtil.paginateAndSend(player, createdText, " create ", page, 7, label);
			return;
		}
		newPowder = powderHandler.getPowder(powderName);
		if (newPowder == null) {
			PowderUtil.sendPrefixMessage(player, Message.CREATE_UNKNOWN, label,
					player.getName(), powderName);
			return;
		}
		if (!(player.hasPermission("powder.createany"))) {
			if (PowderPlugin.get().hasTowny()) {
				if (!PowderPlugin.get().getTownyHandler().checkLocation(newPowder, player)) {
					PowderUtil.sendPrefixMessage(player, Message.CREATE_TOWNY_NO_PLACE, label,
							player.getName(), powderName);
					return;
				}
			}
			if (PowderPlugin.get().hasPlotSquared()) {
				if (!PowderPlugin.get().getPlotSquaredHandler().checkLocation(newPowder, player)) {
					PowderUtil.sendPrefixMessage(player, Message.CREATE_PLOTSQUARED_NO_PLACE, label,
							player.getName(), powderName);
					return;
				}
			}
			if (PowderPlugin.get().getMaxCreatedPowders() < powderHandler.getPowderTasks(player.getUniqueId()).size()) {
				// too many msg
				// CREATE_TOO_MANY_CREATED
				return;
			}
		}
		if (args.length > 3) {
			if (args[3].equalsIgnoreCase("loop")) {
				newPowder = newPowder.loop();
			}
		}
		if (!(powderHandler.getPowderTask(name) == null)) {
			PowderUtil.sendPrefixMessage(player, Message.CREATE_ALREADY_EXISTS,
					label, player.getName(), name, powderName);
			return;
		}
		newPowder.spawn(name, player.getLocation(), player.getUniqueId());
		PowderUtil.sendPrefixMessage(player, Message.CREATE_SUCCESS, label,
				player.getName(), powderName, name);
	}

}
