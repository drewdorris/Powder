package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.ruinscraft.powder.models.powders.Powder;
import com.ruinscraft.powder.models.tasks.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderCommand implements CommandExecutor {

	private List<Player> recentCommandSenders = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// if console
		if (!(sender instanceof Player)) {
			try {
				if (args[0].equals("reload")) {
					// reload
					PowderUtil.notifyOfReload();
					PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							PowderUtil.reloadCommand();
						}
					});
				} else {
					PowderPlugin.getInstance().getLogger().info(
							PowderPlugin.getInstance().getName() + " | " + PowderPlugin.getInstance().getDescription());
				}
			} catch (Exception e) {
				PowderPlugin.getInstance().getLogger().info(
						PowderPlugin.getInstance().getName() + " | " + PowderPlugin.getInstance().getDescription());
			}
			return true;
		}

		Player player = (Player) sender;

		// if no permission for using the command itself
		if (!(player.hasPermission("powder.command"))) {
			PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "You don't have permission to use /" + label + ".", label);
			return false;
		}

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.getInstance().getConfig().getInt("pageLength");

		// if no args, lists categories if categories enabled, else list all Powders
		if (args.length < 1) {
			if (powderHandler.categoriesEnabled()) {
				PowderUtil.listCategories(player, powderHandler.getCategories(), " categories ", 1, pageLength, label);
			} else {
				PowderUtil.listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
			}
			return false;
		}

		Powder powder = powderHandler.getPowder(args[0]);

		// if the first argument given is not a Powder
		if (powder == null) {
			if (args[0].equals("help")) {
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.helpMessage(player, label, page);
				return false;
			} else if (args[0].equals("reload")) {
				if (!(player.hasPermission("powder.reload"))) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "You don't have permission to do this.", label);
					return false;
				}
				// reload if permission
				PowderUtil.notifyOfReload();
				PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), new Runnable() {
					@Override
					public void run() {
						PowderUtil.reloadCommand();
					}
				});
				return true;
			} else if (args[0].equals("*")) {
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO 
							+ "Use '* cancel' to cancel all current active Powders.", label);
					return false;
				} else {
					// cancel all Powders
					if (powderHandler.getPowderTasks(player.getUniqueId()).isEmpty()) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "There are no Powders currently active.", label);
						return false;
					}
					int amount = PowderUtil.cancelAllPowders(player.getUniqueId());
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Successfully cancelled all Powders! (" + 
							amount + " total)", label);
					return true;
				}
				// list Powders, not by category
			} else if (args[0].equals("list")) {
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listPowders(player, powderHandler.getPowders(), " list ", page, pageLength, label);
				return false;
				// list all categories
			} else if (args[0].equals("cancel")) {
				String powderTaskName;
				try {
					powderTaskName = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Please specify a Powder.", label);
					return false;
				}
				if (powderHandler.getPowder(powderTaskName) != null) {
					Set<PowderTask> powderTasks = powderHandler.getPowderTasks(player.getUniqueId(), powderHandler.getPowder(powderTaskName));
					if (powderTasks.isEmpty()) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "There are none of this Powder currently active.", label);
						return false;
					} else {
						powderTaskName = Iterables.get(powderTasks, 0).getName();
					}
				}
				PowderTask powderTask = powderHandler.getPowderTask(powderTaskName);
				if (powderTask == null) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown Powder specified.", label);
					return false;
				}
				if (powderHandler.removePowderTask(powderTask)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Successfully cancelled '" + powderTaskName + "'.", label);
				} else {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Could not cancel this Powder.", label);
				}
				return false;
				// list all categories
			} else if (args[0].equals("active")) {
				PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Active Powders:", label);
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				List<TextComponent> textComponents = new ArrayList<TextComponent>();
				for (PowderTask powderTask : powderHandler.getPowderTasks(player.getUniqueId())) {
					String powderName = "null";
					for (Powder taskPowder : powderTask.getPowders().keySet()) {
						powderName = taskPowder.getName();
						break;
					}
					TextComponent textComponent = new TextComponent(powderName);
					textComponent.setColor(PowderUtil.INFO);
					textComponent.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
							new ComponentBuilder(PowderUtil.INFO + "Powder: " 
									+ powderName + PowderUtil.HIGHLIGHT_TWO + "\nClick to cancel this Powder")
							.color(PowderUtil.HIGHLIGHT_TWO).create() ) );
					textComponent.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
							"/" + label + " cancel " + powderTask.getName() ) );
					textComponents.add(textComponent);
				}
				PowderUtil.paginateAndSend(player, textComponents, " active ", page, 7, label);
				return false;
				// list Powders by category
			} else if (args[0].equals("categories")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Categories are not enabled", label);
					PowderUtil.listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
					return false;
				}
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listCategories(player, powderHandler.getCategories(), " categories ", page, pageLength, label);
				return false;
				// list Powders by category
			} else if (args[0].equals("category")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Categories are not enabled", label);
					PowderUtil.listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
					return false;
				}
				String category;
				int page;
				try {
					category = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/" + label + " category <category> [page]", label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				// list categories with similar names if given category does not exist
				if (powderHandler.getCategory(category) == null) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown category '" + args[0] + "'. Similar categories:", label);
					Map<String, String> similarCategories = powderHandler.getSimilarCategories(args[0]);
					PowderUtil.listCategories(player, similarCategories, " category " + category + " ", page, pageLength, label);
					// else, list Powders by category
				} else {
					String correctCategory = powderHandler.getCategory(category);
					PowderUtil.listPowders(player, powderHandler.getPowdersFromCategory(correctCategory), " category " + category + " ", page, pageLength, label);
				}
				return true;
				// search by Powder name
			} else if (args[0].equals("search")) {
				String search;
				int page;
				try {
					search = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/powder search <term> [page]", label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listPowders(player, powderHandler.getSimilarPowders(search), 
						" search " + search + " ", page, pageLength, label);
				return false;
			} else if (args[0].equals("create")) {
				if (!(player.hasPermission("powder.create"))) {
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/" + label + " create <name> <Powder>", label);
					return false;
				}
				if (!(powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "An active Powder with this name is already active!", label);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown Powder '" + powderName + "'.", label);
					return false;
				}
				newPowder.spawn(player.getLocation(), name);
				PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + 
						"Successfully created '" + name + "'.", label);
				return true;
			} else if (args[0].equals("addto")) {
				if (!(player.hasPermission("powder.addto"))) {
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/" + label + " addto <name> <Powder>", label);
					return false;
				}
				if ((powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "The active Powder '" + name + "' does not exist.", label);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown Powder '" + powderName + "'.", label);
					return false;
				}
				PowderTask powderTask = powderHandler.getPowderTask(name);
				if (powderTask.followsPlayer()) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Cannot edit an active Powder that follows a player!", label);
					return false;
				}
				if (powderTask.addPowder(newPowder, player.getLocation())) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + 
							"Added Powder '" + powderName + "' to '" + name + "'.", label);
				}
				return true;
			} else if (args[0].equals("removefrom")) {
				if (!(player.hasPermission("powder.removefrom"))) {
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/" + label + " removefrom <name> <Powder>", label);
					return false;
				}
				if ((powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "The active Powder '" + name + "' does not exist.", label);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown Powder '" + powderName + "'.", label);
					return false;
				}
				PowderTask powderTask = powderHandler.getPowderTask(name);
				if (powderTask.followsPlayer()) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Cannot edit an active Powder that follows a player!", label);
					return false;
				}
				if (powderTask.removePowder(newPowder)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + 
							"Removed Powder '" + powderName + "' from '" + name + "'.", label);
				} else {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Could not remove this Powder from '" + name + "'.", label);
				}
				return true;
			} else if (args[0].equals("remove")) {
				if (!(player.hasPermission("powder.remove"))) {
					return false;
				}
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/" + label + 
							" remove <name> " + PowderUtil.INFO + "or" + PowderUtil.WARNING + " /" + label + " remove user <username>", label);
					return false;
				}
				if (args[1].equals("user")) {
					Player powderUser;
					try {
						powderUser = Bukkit.getPlayer(args[2]);
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Invalid player.", label);
						return false;
					}
					if (powderUser == null) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Invalid player.", label);
						return false;
					}
					if (powderHandler.getPowderTasks().removeAll(powderHandler.getPowderTasks(powderUser.getUniqueId()))) {
						if (!(powderUser.equals(player))) {
							PowderUtil.sendPrefixMessage(powderUser, PowderUtil.WARNING + "Your Powders were canceled by another user.", label);
						}
						PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + 
								"Successfully removed " + powderUser.getName() + "'s Powders.", label);
					} else {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + 
								"Could not remove " + powderUser.getName() + "'s Powders.", label);
					}
				} else {
					String name;
					try {
						name = args[1];
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "/powder remove <name>", label);
						return false;
					}
					if (powderHandler.removePowderTask(powderHandler.getPowderTask(name))) {
						PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Successfully removed '" + name + "'.", label);
					} else {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Could not find or remove '" + name + "'.", label);
					}
				}
			} else if (args[0].equals("nearby")) {
				if (!(player.hasPermission("powder.nearby"))) {
					return false;
				}
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Nearby active Powders:", label);
				Map<PowderTask, Integer> nearby = powderHandler.getNearbyPowderTasks(player.getLocation(), 200);
				List<TextComponent> nearbyText = new ArrayList<TextComponent>();
				for (PowderTask powderTask : nearby.keySet()) {
					TextComponent text = new TextComponent();
					text.setColor(PowderUtil.INFO);
					String powderTaskName = powderTask.getName();
					String playerName;
					if (powderTask.getPlayerUUID() == null) {
						playerName = null;
					} else {
						playerName = Bukkit.getPlayer(powderTask.getPlayerUUID()).getName();
					}
					text.addExtra(PowderUtil.HIGHLIGHT + powderTaskName + PowderUtil.INFO + " " + nearby.get(powderTask) + "m");
					if (player.hasPermission("powder.remove")) {
						Set<Powder> taskPowders = powderTask.getPowders().keySet();
						StringBuilder stringBuilder = new StringBuilder();
						for (Powder taskPowder : taskPowders) {
							stringBuilder.append(taskPowder.getName());
							if (!(Iterables.get(taskPowders, taskPowders.size() - 1) == taskPowder)) {
								stringBuilder.append(", ");
							}
						}
						if (powderTask.getPlayerUUID() != null) {
							text.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder(PowderUtil.INFO + "Powders: " + stringBuilder.toString() + 
											PowderUtil.HIGHLIGHT_TWO + "\nClick to cancel " + playerName + "'s Powder")
									.color(PowderUtil.HIGHLIGHT_TWO).create() ) );
							text.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
									"/" + label + " remove " + powderTaskName ) );
						} else {
							text.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder(PowderUtil.INFO + "Powders: " + stringBuilder.toString() + 
											PowderUtil.HIGHLIGHT_TWO + "\nClick to cancel this active Powder")
									.color(PowderUtil.HIGHLIGHT_TWO).create() ) );
							text.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
									"/" + label + " remove " + powderTaskName ) );
						}
					}
					nearbyText.add(text);
				}
				PowderUtil.paginateAndSend(player, nearbyText, " nearby ", page, 7, label);
				return true;
			} else {
				if (powderHandler.categoriesEnabled()) {
					if (powderHandler.getCategory(args[0]) != null) {
						PowderUtil.listPowders(player, powderHandler.getPowdersFromCategory(args[0]), " category " + args[0] + " ", 1, pageLength, label);
						return true;
					} else {
						PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown category '" + args[0] + "'. Similar categories:", label);
						PowderUtil.listCategories(player, powderHandler.getSimilarCategories(args[0]), " category " + args[0] + " ", 1, pageLength, label);
					}
				} else {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING + "Unknown Powder '" + args[0] + "'. Similar Powders:", label);
					PowderUtil.listPowders(player, powderHandler.getSimilarPowders(args[0]), " list ", 1, pageLength, label);
				}
				return false;
			}
		}

		// after this, first argument is clearly a Powder

		// if no permission for the specific Powder
		if (!PowderUtil.hasPermission(player, powder)) {
			PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING 
					+ "You don't have permission to use the Powder '" + powder.getName() + "'.", label);
			return false;
		}

		// if another argument after the Powder name
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				int taskAmount = powderHandler.getPowderTasks(player.getUniqueId(), powder).size();
				// cancel if exists
				if (PowderUtil.cancelPowder(player.getUniqueId(), powder)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Powder '" + powder.getName() + "' cancelled! (" + 
							taskAmount + " total)", label);
				} else {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING 
							+ "There are no '" + powder.getName() + "' Powders currently active.", label);
				}
			}
			return false;
		}

		// if this Powder is already running for the player
		if (!(powderHandler.getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
			// if multiple uses of one Powder are not allowed, cancel it
			if (!(PowderPlugin.getInstance().getConfig().getBoolean("allowSamePowdersAtOneTime"))) {
				if (PowderUtil.cancelPowder(player.getUniqueId(), powder)) {
					PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Powder '" + powder.getName() + "' cancelled!", label);
				} else {
					PowderUtil.sendPrefixMessage(player, PowderUtil.WARNING 
							+ "There are no active '" + powder.getName() + "' Powders.", label);
				}
				return false;
			}
		}

		// if player has maxPowdersAtOneTime, don't do it
		int maxSize = PowderPlugin.getInstance().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player.getUniqueId()).size() >= maxSize)) {
			PowderUtil.sendPrefixMessage(player, 
					PowderUtil.WARNING + "You already have " + maxSize + " Powders active!", label);
			for (PowderTask powderTask : powderHandler.getPowderTasks(player.getUniqueId())) {
				String powderName = "null";
				for (Powder taskPowder : powderTask.getPowders().keySet()) {
					powderName = taskPowder.getName();
				}
				TextComponent runningTaskText = new TextComponent(PowderUtil.INFO + "| " 
						+ ChatColor.ITALIC + powderName);
				runningTaskText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderName + "' is currently active. Click to cancel")
						.color(PowderUtil.HIGHLIGHT_THREE).create() ) );
				runningTaskText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderName + " cancel" ) );
				player.spigot().sendMessage(runningTaskText);
			}
			return false;
		}

		// wait time between creating each Powder
		int waitTime = PowderPlugin.getInstance().getConfig().getInt("secondsBetweenPowderUsage");
		// if they sent a command in the given wait time, don't do it
		if (recentCommandSenders.contains(player)) {
			PowderUtil.sendPrefixMessage(player, 
					PowderUtil.WARNING + "Please wait " + waitTime + " seconds between using each Powder.", label);
			return false;
		}
		// if there's a wait time between using each Powder
		if (!(waitTime <= 0)) {
			// add user to this list of recent command senders for the given amount of time
			PowderPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(PowderPlugin.getInstance(), new Runnable() {
				public void run() {
					recentCommandSenders.remove(player);
				}
			}, (waitTime * 20));
			recentCommandSenders.add(player);
		}

		// spawn a Powder with a PowderTask
		powder.spawn(player);
		// if Powder has animation/dusts/sounds

		if (powder.hasMovement()) {
			TextComponent particleSentText = new TextComponent(PowderUtil.INFO 
					+ "Powder '" + powder.getName() + "' created! Click to cancel.");
			particleSentText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Click to cancel '" + powder.getName() + "'.")
					.color(PowderUtil.HIGHLIGHT_THREE).create() ) );
			particleSentText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
					"/" + label + " " + powder.getName() + " cancel" ) );
			PowderUtil.sendPrefixMessage(player, particleSentText, label);
			if (new Random().nextInt(12) == 1) {
				PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + 
						"Tip: Cancel with '/" + label + " <Powder> cancel'.", label);
			}
		} else {
			PowderUtil.sendPrefixMessage(player, PowderUtil.INFO 
					+ "Powder '" + powder.getName() + "' created!", label);
		}

		return true;
	}

}
