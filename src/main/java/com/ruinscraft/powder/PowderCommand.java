package com.ruinscraft.powder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

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
					notifyOfReload();
					PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), new Runnable() {
						@Override
						public void run() {
							reload();
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
			PowderUtil.sendPrefixMessage(player, ChatColor.RED + "You don't have permission to use /" + label + ".", label);
			return false;
		}

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.getInstance().getConfig().getInt("pageLength");

		// if no args, lists categories if categories enabled, else list all Powders
		if (args.length < 1) {
			if (powderHandler.categoriesEnabled()) {
				listCategories(player, powderHandler.getCategories(), " categories ", 1, pageLength, label);
			} else {
				listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
			}
			return false;
		}

		Powder powder = powderHandler.getPowder(args[0]);

		// if the first argument given is not a Powder
		if (powder == null) {
			if (args[0].equals("help")) {
				helpMessage(player, label);
				return false;
			} else if (args[0].equals("reload")) {
				if (!(player.hasPermission("powder.reload"))) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "You don't have permission to do this.", label);
					return false;
				}
				// reload if permission
				notifyOfReload();
				PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), new Runnable() {
					@Override
					public void run() {
						reload();
					}
				});
				return true;
			} else if (args[0].equals("*")) {
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, ChatColor.GRAY 
							+ "Use '* cancel' to cancel all current active Powders.", label);
					return false;
				} else {
					// cancel all Powders
					if (powderHandler.getPowderTasks(player.getUniqueId()).isEmpty()) {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "There are no Powders currently active.", label);
						return false;
					}
					int amount = PowderUtil.cancelAllPowders(player.getUniqueId());
					PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Successfully cancelled all Powders! (" + 
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
				listPowders(player, powderHandler.getPowders(), " list ", page, pageLength, label);
				return false;
				// list all categories
			} else if (args[0].equals("categories")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Categories are not enabled", label);
					listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
					return false;
				}
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				listCategories(player, powderHandler.getCategories(), " categories ", page, pageLength, label);
				return false;
				// list Powders by category
			} else if (args[0].equals("category")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Categories are not enabled", label);
					listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
					return false;
				}
				String category;
				int page;
				try {
					category = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "/" + label + " category <category> [page]", label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				// list categories with similar names if given category does not exist
				if (powderHandler.getCategory(category) == null) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Unknown category '" + args[0] + "'. Similar categories:", label);
					Map<String, String> similarCategories = powderHandler.getSimilarCategories(args[0]);
					listCategories(player, similarCategories, " category " + category + " ", page, pageLength, label);
					// else, list Powders by category
				} else {
					String correctCategory = powderHandler.getCategory(category);
					listPowders(player, powderHandler.getPowdersFromCategory(correctCategory), " category " + category + " ", page, pageLength, label);
				}
				return true;
				// search by Powder name
			} else if (args[0].equals("search")) {
				String search;
				int page;
				try {
					search = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "/powder search <term> [page]", label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				listPowders(player, powderHandler.getSimilarPowders(search), 
						" search " + search + " ", page, pageLength, label);
				return false;
				// list by category/Powder if other criteria not met
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
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "/" + label + " create <name> <Powder>", label);
					return false;
				}
				if (!(powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "An active Powder with this name is already active!", label);
					return false;
				}
				newPowder.spawn(player.getLocation(), name);
			} else if (args[0].equals("remove")) {
				if (!(player.hasPermission("powder.remove"))) {
					return false;
				}
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "/" + label + 
							" remove <name> " + ChatColor.GRAY + "or" + ChatColor.RED + " /" + label + " remove user <username>", label);
					return false;
				}
				if (args[1].equals("user")) {
					Player powderUser;
					try {
						powderUser = Bukkit.getPlayer(args[2]);
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Invalid player.", label);
						return false;
					}
					if (powderUser == null) {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Invalid player.", label);
						return false;
					}
					if (powderHandler.getPowderTasks().removeAll(powderHandler.getPowderTasks(powderUser.getUniqueId()))) {
						if (!(powderUser.equals(player))) {
							PowderUtil.sendPrefixMessage(powderUser, ChatColor.RED + "Your Powders were canceled by another user.", label);
						}
						PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + 
								"Successfully removed " + powderUser.getName() + "'s Powders.", label);
					} else {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + 
								"Could not remove " + powderUser.getName() + "'s Powders.", label);
					}
				} else {
					String name;
					try {
						name = args[1];
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "/powder remove <name>", label);
						return false;
					}
					if (powderHandler.removePowderTask(powderHandler.getPowderTask(name))) {
						PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Successfully removed '" + name + "'.", label);
					} else {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Could not find or remove '" + name + "'.", label);
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
				PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Nearby active Powders:", label);
				Map<PowderTask, Integer> nearby = powderHandler.getNearbyPowderTasks(player.getLocation(), 200);
				List<TextComponent> nearbyText = new ArrayList<TextComponent>();
				for (PowderTask powderTask : nearby.keySet()) {
					TextComponent text = new TextComponent();
					text.setColor(net.md_5.bungee.api.ChatColor.GRAY);
					String powderTaskName = powderTask.getName();
					boolean usingName = false;
					String playerName = null;
					if (powderTaskName == null) {
						playerName = Bukkit.getPlayer(powderTask.getPlayerUUID()).getName();
						powderTaskName = ChatColor.ITALIC + playerName + "'s Powder";
						usingName = true;
					}
					text.addExtra(ChatColor.RED + powderTaskName + ChatColor.GRAY + " - " + nearby.get(powderTask) + "m");
					if (player.hasPermission("powder.remove")) {
						if (usingName) {
							text.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder("Click to cancel all of " + playerName + "'s active Powders")
									.color(net.md_5.bungee.api.ChatColor.GREEN).create() ) );
							text.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
									"/" + label + " remove user " + playerName ) );
						} else {
							text.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
									new ComponentBuilder("Click to cancel this active Powder")
									.color(net.md_5.bungee.api.ChatColor.GREEN).create() ) );
							text.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
									"/" + label + " remove " + powderTaskName ) );
						}
					}
					nearbyText.add(text);
				}
				paginate(player, nearbyText, " nearby " + String.valueOf(page), page, 7, label);
				return true;
			} else {
				if (powderHandler.categoriesEnabled()) {
					if (powderHandler.getCategory(args[0]) != null) {
						listPowders(player, powderHandler.getPowdersFromCategory(args[0]), " category " + args[0] + " ", 1, pageLength, label);
						return true;
					} else {
						PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Unknown category '" + args[0] + "'. Similar categories:", label);
						listCategories(player, powderHandler.getSimilarCategories(args[0]), " category " + args[0] + " ", 1, pageLength, label);
					}
				} else {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED + "Unknown Powder '" + args[0] + "'. Similar Powders:", label);
					listPowders(player, powderHandler.getSimilarPowders(args[0]), " list ", 1, pageLength, label);
				}
				return false;
			}
		}

		// after this, first argument is clearly a Powder

		// if no permission for the specific Powder
		if (!hasPermission(player, powder)) {
			PowderUtil.sendPrefixMessage(player, ChatColor.RED 
					+ "You don't have permission to use the Powder '" + powder.getName() + "'.", label);
			return false;
		}

		// if another argument after the Powder name
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				int taskAmount = powderHandler.getPowderTasks(player.getUniqueId(), powder).size();
				// cancel if exists
				if (PowderUtil.cancelPowder(player.getUniqueId(), powder)) {
					PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + powder.getName() + "' cancelled! (" + 
							taskAmount + " total)", label);
				} else {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED 
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
					PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + powder.getName() + "' cancelled!", label);
				} else {
					PowderUtil.sendPrefixMessage(player, ChatColor.RED 
							+ "There are no active '" + powder.getName() + "' Powders.", label);
				}
				return false;
			}
		}

		// if player has maxPowdersAtOneTime, don't do it
		int maxSize = PowderPlugin.getInstance().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player.getUniqueId()).size() >= maxSize)) {
			PowderUtil.sendPrefixMessage(player, 
					ChatColor.RED + "You already have " + maxSize + " Powders active!", label);
			for (PowderTask powderTask : powderHandler.getPowderTasks(player.getUniqueId())) {
				TextComponent runningTaskText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "| " 
						+ net.md_5.bungee.api.ChatColor.ITALIC + powderTask.getPowder().getName());
				runningTaskText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderTask.getPowder().getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
				runningTaskText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderTask.getPowder().getName() + " cancel" ) );
				player.spigot().sendMessage(runningTaskText);
			}
			return false;
		}

		// wait time between creating each Powder
		int waitTime = PowderPlugin.getInstance().getConfig().getInt("secondsBetweenPowderUsage");
		// if they sent a command in the given wait time, don't do it
		if (recentCommandSenders.contains(player)) {
			PowderUtil.sendPrefixMessage(player, 
					ChatColor.RED + "Please wait " + waitTime + " seconds between using each Powder.", label);
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
			TextComponent particleSentText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + powder.getName() + "' created! Click to cancel.");
			particleSentText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Click to cancel '" + powder.getName() + "'.")
					.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
			particleSentText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
					"/" + label + " " + powder.getName() + " cancel" ) );
			PowderUtil.sendPrefixMessage(player, particleSentText, label);
			if (new Random().nextInt(12) == 1) {
				PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + 
						"Tip: Cancel with '/" + label + " <Powder> cancel'.", label);
			}
		} else {
			PowderUtil.sendPrefixMessage(player, net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + powder.getName() + "' created!", label);
		}

		return true;
	}

	// reload config and all Powders, while saving database
	public void reload() {
		PowderPlugin.getInstance().loadConfig();

		if (PowderPlugin.getInstance().useStorage()) {
			PowderUtil.savePowdersForOnline();
		}

		PowderPlugin.getInstance().enableStorage();
		PowderPlugin.getInstance().loadPowdersFromSources();

		if (PowderPlugin.getInstance().useStorage()) {
			PowderUtil.loadPowdersForOnline();
		}
	}

	// check permission for the Powder or for a category that contains the Powder
	public static boolean hasPermission(Player player, Powder powder) {
		if (player.hasPermission("powder.powder.*")) {
			return true;
		}
		boolean success = false;
		for (String category : powder.getCategories()) {
			if (player.hasPermission("powder.category." + category.toLowerCase(Locale.US))) {
				success = true;
				break;
			}
		}
		if (success == false) {
			if (!(player.hasPermission("powder.powder." + powder.getName().toLowerCase(Locale.US)))) {
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	// notify players who have a running PowderTask of the reload
	public static void notifyOfReload() {
		if (!(PowderPlugin.getInstance().useStorage())) {
			for (Player player : PowderPlugin.getInstance().getPowderHandler().getAllPowderTaskUsers()) {
				PowderUtil.sendPrefixMessage(player, ChatColor.GRAY 
						+ "Your Powders were cancelled due to a reload.", "powder");
			}
		}
	}

	// help message
	public static void helpMessage(Player player, String label) {
		PowderUtil.sendPrefixMessage(player, ChatColor.GRAY + "Powder Help", label);
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder <powder> " + ChatColor.GRAY + "- Use a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder <powder> cancel " + ChatColor.GRAY + "- Cancel a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder * cancel " + ChatColor.GRAY + "- Cancel all Powders"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder list [page] " + ChatColor.GRAY + "- List Powders by page");
		if (PowderPlugin.getInstance().getPowderHandler().categoriesEnabled()) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder categories [page] " + ChatColor.GRAY + "- List all categories");
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder category <category> [page] " + 
					ChatColor.GRAY + "- List all Powders in a category");
		}
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder search <term> [page] " + ChatColor.GRAY + "- Search for a Powder");
		if (player.hasPermission("powder.reload")) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder reload " + ChatColor.GRAY + "- Reload Powder"); 
		}
		player.sendMessage(ChatColor.GRAY + "It's also possible to " + ChatColor.RED + "click things in /" 
				+ label + ChatColor.GRAY + " to enable or cancel Powders. Click the prefix in a message to return to the menu."); 
	}

	// sorts a list of TextComponents (Powders or categories) alphabetically
	public static List<TextComponent> sortAlphabetically(List<TextComponent> powders) {
		List<String> names = new ArrayList<String>(powders.size()); 

		for (TextComponent powderName : powders) {
			names.add(powderName.getText());
		}

		Collections.sort(names, Collator.getInstance());
		List<TextComponent> newList = new ArrayList<TextComponent>(powders.size());

		for (String name : names) {
			for (TextComponent powderName : powders) {
				if (powderName.getText() == name) {
					newList.add(powderName);
					break;
				}
			}
		}

		return newList;
	}

	// paginates & sends list of Powders/categories to player
	public static void paginate(Player player, List<TextComponent> list, String input, int page, int pageLength, String label) {
		List<TextComponent> pageList = new ArrayList<TextComponent>();
		// create list of Powders based on current page & given amnt per page
		for (int i = 1; i <= pageLength; i++) {
			TextComponent current;
			try {
				current = list.get((page * pageLength) + i - pageLength - 1);
			} catch (Exception e) {
				break;
			}
			pageList.add(current);
			TextComponent combinedMessage = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + 
					"| " + net.md_5.bungee.api.ChatColor.RESET);;
					combinedMessage.addExtra(current);
					player.spigot().sendMessage(combinedMessage);
		}

		// create arrows
		TextComponent leftArrow = new TextComponent("<<  ");
		leftArrow.setColor(net.md_5.bungee.api.ChatColor.RED);
		leftArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + input + (page - 1) ) );
		leftArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Previous Page")
				.color(net.md_5.bungee.api.ChatColor.RED).create() ) );

		TextComponent middle = new TextComponent("Page (click)");
		middle.setColor(net.md_5.bungee.api.ChatColor.RED);

		TextComponent rightArrow = new TextComponent("  >>");
		rightArrow.setColor(net.md_5.bungee.api.ChatColor.RED);
		rightArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + input + (page + 1) ) );
		rightArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Next Page")
				.color(net.md_5.bungee.api.ChatColor.RED).create() ) );

		// adds the arrows to the message depending on where you are in the list
		TextComponent fullArrows = new TextComponent();
		if (pageList.isEmpty()) {
			player.sendMessage(ChatColor.RED + "None found.");
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
		} else if ((!pageList.contains(list.get(0)) && pageList.contains(list.get(list.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
		} else if (!(pageList.contains(list.get(0))) && !(pageList.contains(list.get(list.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(list.get(0)) && !pageList.contains(list.get(list.size() - 1))) {
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(list.get(0)) && pageList.contains(list.get(list.size() - 1))) {
			return;
		}
		player.spigot().sendMessage(fullArrows);
	}

	// organizes given the given List<Powder> by active/allowed/not allowed Powders, alphabetizes, then paginates
	public static void listPowders(Player player, List<Powder> powders, String input, int page, int pageLength, String label) {
		TextComponent helpPrefix = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "Use " +
				net.md_5.bungee.api.ChatColor.RED + "/" + label +  " help" + net.md_5.bungee.api.ChatColor.GRAY + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		helpPrefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " help" ) );
		PowderUtil.sendPrefixMessage(player, helpPrefix, label);

		// all Powders
		List<TextComponent> listOfPowders = new ArrayList<TextComponent>();
		// Powders currently in use by the player
		List<TextComponent> activePowders = new ArrayList<TextComponent>();
		// Powders the player has permission for
		List<TextComponent> ableToPowders = new ArrayList<TextComponent>();
		// Powders the player does not have permission for
		List<TextComponent> noPermPowders = new ArrayList<TextComponent>();
		for (Powder powder : powders) {
			TextComponent powderMapText = new TextComponent(powder.getName());
			if (!hasPermission(player, powder)) {
				if (powder.isHidden()) {
					continue;
				}
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("You don't have permission to use '" + powder.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.RED).create() ) );
				noPermPowders.add(powderMapText);
			} else if (!(PowderPlugin.getInstance().getPowderHandler().getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powder.getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.GREEN).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powder.getName() + " cancel" ) );
				activePowders.add(powderMapText);
			} else {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click to use '" + powder.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powder.getName()) );
				ableToPowders.add(powderMapText);
			}
		}
		activePowders = sortAlphabetically(activePowders);
		ableToPowders = sortAlphabetically(ableToPowders);
		noPermPowders = sortAlphabetically(noPermPowders);
		listOfPowders.addAll(activePowders);
		listOfPowders.addAll(ableToPowders);
		listOfPowders.addAll(noPermPowders);
		paginate(player, listOfPowders, input, page, pageLength, label);
	}

	// similar to listPowders but lists categories instead
	public static void listCategories(Player player, Map<String, String> categories, String input, int page, int pageLength, String label) {
		TextComponent helpPrefix = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "Use " +
				net.md_5.bungee.api.ChatColor.RED + "/" + label +  " help" + net.md_5.bungee.api.ChatColor.GRAY + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		helpPrefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " help" ) );
		PowderUtil.sendPrefixMessage(player, helpPrefix, label);

		// all categories
		List<TextComponent> listOfCategories = new LinkedList<TextComponent>();
		// categories containing Powders that are currently active
		List<TextComponent> activeCategories = new LinkedList<TextComponent>();
		// categories containing Powders the player has permission for
		List<TextComponent> ableToCategories = new LinkedList<TextComponent>();
		// categories containing Powders the player has no permission for, or contains no Powders
		List<TextComponent> noPermCategories = new LinkedList<TextComponent>();
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		for (String category : categories.keySet()) {
			TextComponent categoryText = new TextComponent(category);
			String desc = categories.get(category);
			for (Powder powder : powderHandler.getPowdersFromCategory(category)) {
				if (!(PowderPlugin.getInstance().getPowderHandler().getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
					categoryText.setColor(net.md_5.bungee.api.ChatColor.GREEN);
					categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
							new ComponentBuilder(desc + " - " + "'" + category + "' contains currently active Powders.")
							.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
					categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
							"/" + label + " " + category + " 1" ) );
					if (!activeCategories.contains(categoryText)) {
						activeCategories.add(categoryText);
					}
					break;
				} else {
					if (hasPermission(player, powder)) {
						categoryText.setColor(net.md_5.bungee.api.ChatColor.GRAY);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc)
								.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
						categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
								"/" + label + " " + category + " 1" ) );
						if (!ableToCategories.contains(categoryText)) {
							ableToCategories.add(categoryText);
						}
					}
				}
			}
			if (!activeCategories.contains(categoryText)) {
				if (!ableToCategories.contains(categoryText)) {
					if (powderHandler.getPowdersFromCategory(category).isEmpty()) {
						categoryText.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc + " - This category is empty.")
								.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
						categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
								"/" + label + " " + category + " 1" ) );
					} else {
						categoryText.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc + " - " + "You don't have permission to use any Powders in '" + category + "'.")
								.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
						categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
								"/" + label + " " + category + " 1" ) );
					}
					noPermCategories.add(categoryText);
				}
			} else {
				ableToCategories.remove(categoryText);
			}

		}
		activeCategories = sortAlphabetically(activeCategories);
		ableToCategories = sortAlphabetically(ableToCategories);
		noPermCategories = sortAlphabetically(noPermCategories);
		listOfCategories.addAll(activeCategories);
		listOfCategories.addAll(ableToCategories);
		listOfCategories.addAll(noPermCategories);

		paginate(player, listOfCategories, input, page, pageLength, label);
	}

}
