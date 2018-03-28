package com.ruinscraft.powder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.tasks.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderCommand implements CommandExecutor {

	private List<Player> recentCommandSenders = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			try {
				if (args[0].equals("reload")) {
					notifyOfReload();
					PowderPlugin.getInstance().handleConfig();
					PowderPlugin.getInstance().getLogger().info("Powder config.yml reloading...");
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
		if (!(player.hasPermission("powder.command"))) {
			sendPrefixMessage(player, ChatColor.RED + "You don't have permission to use /" + label + ".", label);
			return false;
		}

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		int pageLength = PowderPlugin.getInstance().getConfig().getInt("pageLength");

		if (args.length < 1) {
			if (powderHandler.categoriesEnabled()) {
				listCategories(player, powderHandler.getCategories(), " categories ", 1, pageLength, label);
			} else {
				listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
			}
			return false;
		}

		Powder powder = powderHandler.getPowder(args[0]);

		if (powder == null) {

			if (args[0].equals("help")) {

				helpMessage(player, label);
				return false;

			} else if (args[0].equals("reload")) {

				if (!(player.hasPermission("powder.reload"))) {
					sendPrefixMessage(player, ChatColor.RED + "You don't have permission to do this.", label);
					return false;
				}
				notifyOfReload();
				PowderPlugin.getInstance().handleConfig();
				sendPrefixMessage(player, ChatColor.GRAY + "Powder config.yml reloaded.", label);

				return true;

			} else if (args[0].equals("*")) {

				if (args.length < 2) {
					sendPrefixMessage(player, ChatColor.GRAY 
							+ "Use '* cancel' to cancel all current active Powders.", label);
					return false;
				} else {
					if (powderHandler.getPowderTasks(player).isEmpty()) {
						sendPrefixMessage(player, ChatColor.RED + "There are no Powders currently active.", label);
						return false;
					}
					int amount = powderHandler.getPowderTasks(player).size();
					for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
						powderHandler.removePowderTask(powderTask);
					}
					sendPrefixMessage(player, ChatColor.GRAY + "Successfully cancelled all Powders! (" + 
							amount + " total)", label);
					return true;
				}

			} else if (args[0].equals("list")) {

				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				listPowders(player, powderHandler.getPowders(), " list ", page, pageLength, label);
				return false;

			} else if (args[0].equals("categories")) {

				if (!powderHandler.categoriesEnabled()) {
					sendPrefixMessage(player, ChatColor.RED + "Categories are not enabled", label);
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

			} else if (args[0].equals("category")) {

				if (!powderHandler.categoriesEnabled()) {
					sendPrefixMessage(player, ChatColor.RED + "Categories are not enabled", label);
					listPowders(player, powderHandler.getPowders(), " list ", 1, pageLength, label);
					return false;
				}
				String category;
				int page;
				try {
					category = args[1];
				} catch (Exception e) {
					sendPrefixMessage(player, ChatColor.RED + "/" + label + " category <category> [page]", label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				if (powderHandler.getCategory(category) == null) {
					sendPrefixMessage(player, ChatColor.RED + "Unknown category '" + args[0] + "'. Similar categories:", label);
					Map<String, String> similarCategories = powderHandler.getSimilarCategories(args[0]);
					listCategories(player, similarCategories, " category " + category + " ", page, pageLength, label);
				} else {
					String correctCategory = powderHandler.getCategory(category);
					listPowders(player, powderHandler.getPowdersFromCategory(correctCategory), " category " + category + " ", page, pageLength, label);
				}
				return true;

			} else if (args[0].equals("search")) {

				String search;
				int page;
				try {
					search = String.valueOf(args[1]);
				} catch (Exception e) {
					sendPrefixMessage(player, ChatColor.RED + "/powder search <term> [page]", label);
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

			} else {

				if (powderHandler.categoriesEnabled()) {
					if (powderHandler.getCategory(args[0]) != null) {
						listPowders(player, powderHandler.getPowdersFromCategory(args[0]), " category " + args[0] + " ", 1, pageLength, label);
						return true;
					} else {
						sendPrefixMessage(player, ChatColor.RED + "Unknown category '" + args[0] + "'. Similar categories:", label);
						listCategories(player, powderHandler.getSimilarCategories(args[0]), " category " + args[0] + " ", 1, pageLength, label);
					}
				} else {
					sendPrefixMessage(player, ChatColor.RED + "Unknown Powder '" + args[0] + "'. Similar Powders:", label);
					listPowders(player, powderHandler.getSimilarPowders(args[0]), " list ", 1, pageLength, label);
				}
				return false;

			}

		}

		if (!hasPermission(player, powder)) {
			sendPrefixMessage(player, ChatColor.RED 
					+ "You don't have permission to use the Powder '" + powder.getName() + "'.", label);
			return false;
		}

		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				int taskAmount = powderHandler.getPowderTasks(player, powder).size();
				if (PowderUtil.cancelPowder(player, powder)) {
					sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + powder.getName() + "' cancelled! (" + 
							taskAmount + " total)", label);
				} else {
					sendPrefixMessage(player, ChatColor.RED 
							+ "There are no '" + powder.getName() + "' Powders currently active.", label);
				}
			}
			return false;
		}

		if (!(powderHandler.getPowderTasks(player, powder).isEmpty())) {
			if (!(PowderPlugin.getInstance().getConfig().getBoolean("allowSamePowdersAtOneTime"))) {
				boolean success = false;
				for (PowderTask powderTask : powderHandler.getPowderTasks(player, powder)) {
					powderHandler.removePowderTask(powderTask);
					success = true;
				}
				if (success) {
					sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + powder.getName() + "' cancelled!", label);
				} else {
					sendPrefixMessage(player, ChatColor.RED 
							+ "There are no active '" + powder.getName() + "' Powders.", label);
				}
				return false;
			}
		}

		int maxSize = PowderPlugin.getInstance().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player).size() >= maxSize)) {
			sendPrefixMessage(player, 
					ChatColor.RED + "You already have " + maxSize + " Powders active!", label);
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				TextComponent runningTaskText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "| " 
						+ net.md_5.bungee.api.ChatColor.ITALIC + powderTask.getMap().getName());
				runningTaskText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderTask.getMap().getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
				runningTaskText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderTask.getMap().getName() + " cancel" ) );
				player.spigot().sendMessage(runningTaskText);
			}
			return false;
		}
		if (!(PowderPlugin.getInstance().getConfig().getBoolean("allowSamePowdersAtOneTime"))) {
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				if (powderTask.getMap().equals(powder)) {
					sendPrefixMessage(player, 
							ChatColor.RED + "'" + powder.getName() + "' is already active!", label);
					return false;
				}
			}
		}

		// wait time between each command
		int waitTime = PowderPlugin.getInstance().getConfig().getInt("secondsBetweenPowderUsage");
		if (recentCommandSenders.contains(player)) {
			sendPrefixMessage(player, 
					ChatColor.RED + "Please wait " + waitTime + " seconds between using each Powder.", label);
			return false;
		}
		if (!(waitTime <= 0)) {
			PowderPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(PowderPlugin.getInstance(), new Runnable() {
				public void run() {
					recentCommandSenders.remove(player);
				}
			}, (waitTime * 20));
			recentCommandSenders.add(player);
		}

		List<Integer> tasks = new ArrayList<Integer>();

		tasks.addAll(PowderUtil.createPowder(player, powder));

		if (powder.isRepeating() || powder.getMatrices().size() > 1 || !(powder.getDusts().isEmpty())) {
			TextComponent particleSentText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + powder.getName() + "' created! Click to cancel.");
			particleSentText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
					new ComponentBuilder("Click to cancel '" + powder.getName() + "'.")
					.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
			particleSentText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
					"/" + label + " " + powder.getName() + " cancel" ) );
			sendPrefixMessage(player, particleSentText, label);
			if (new Random().nextInt(12) == 1) {
				sendPrefixMessage(player, ChatColor.GRAY + 
						"Tip: Cancel with '/" + label + " <Powder> cancel'.", label);
			}
		} else {
			sendPrefixMessage(player, net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + powder.getName() + "' created!", label);
		}

		PowderTask powderTask = new PowderTask(player, tasks, powder);
		powderHandler.addPowderTask(powderTask);

		return true;

	}

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

	public static void notifyOfReload() {

		List<Player> playersDoneAlready = new ArrayList<Player>();
		for (PowderTask powderTask : PowderPlugin.getInstance().getPowderHandler().getPowderTasks()) {
			if (playersDoneAlready.contains(powderTask.getPlayer())) {
				continue;
			}
			playersDoneAlready.add(powderTask.getPlayer());
			sendPrefixMessage(powderTask.getPlayer(), ChatColor.GRAY 
					+ "Your Powders were cancelled due to " + "a reload.", "powder");
		}
		playersDoneAlready = null;

	}

	public static void helpMessage(Player player, String label) {

		sendPrefixMessage(player, ChatColor.GRAY + "Powder Help", label);
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder <powder> " + ChatColor.GRAY + "- Use a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder <powder> cancel " + ChatColor.GRAY + "- Cancel a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder * cancel " + ChatColor.GRAY + "- Cancel all Powders"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder list [page] " + ChatColor.GRAY + "- List Powders by page");
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder search <term> [page] " + ChatColor.GRAY + "- Search for a Powder");
		if (player.hasPermission("powder.reload")) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder reload " + ChatColor.GRAY + "- Reload Powder"); 
		}
		player.sendMessage(ChatColor.GRAY + "It's also possible to " + ChatColor.RED + "click things in /" 
				+ label + ChatColor.GRAY + " to enable or cancel Powders. Click the prefix in a message to return to the menu."); 

	}

	public static void sendPrefixMessage(Player player, Object message, String label) {

		if (!(message instanceof String) && !(message instanceof BaseComponent)) {
			return;
		}
		if (message instanceof String) {
			String messageText = (String) message;
			message = new TextComponent(messageText);
		}

		BaseComponent fullMessage = new TextComponent();
		TextComponent prefix = new TextComponent(PowderPlugin.PREFIX);
		prefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label).color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		prefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/" + label ) );
		fullMessage.addExtra(prefix);
		fullMessage.addExtra((TextComponent) message);

		player.spigot().sendMessage(fullMessage);

	}

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

	public static void paginate(Player player, List<TextComponent> list, String input, int page, int pageLength, String label) {
		List<TextComponent> pageList = new ArrayList<TextComponent>();
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

	public static void listPowders(Player player, List<Powder> powders, String input, int page, int pageLength, String label) {

		TextComponent helpPrefix = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "Use " +
				net.md_5.bungee.api.ChatColor.RED + "/" + label +  " help" + net.md_5.bungee.api.ChatColor.GRAY + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		helpPrefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " help" ) );
		sendPrefixMessage(player, helpPrefix, label);

		List<TextComponent> listOfPowders = new ArrayList<TextComponent>();
		List<TextComponent> activePowders = new ArrayList<TextComponent>();
		List<TextComponent> ableToPowders = new ArrayList<TextComponent>();
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
			} else if (!(PowderPlugin.getInstance().getPowderHandler().getPowderTasks(player, powder).isEmpty())) {
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

	public static void listCategories(Player player, Map<String, String> categories, String input, int page, int pageLength, String label) {

		TextComponent helpPrefix = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "Use " +
				net.md_5.bungee.api.ChatColor.RED + "/" + label +  " help" + net.md_5.bungee.api.ChatColor.GRAY + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		helpPrefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " help" ) );
		sendPrefixMessage(player, helpPrefix, label);

		List<TextComponent> listOfCategories = new LinkedList<TextComponent>();
		List<TextComponent> activeCategories = new LinkedList<TextComponent>();
		List<TextComponent> ableToCategories = new LinkedList<TextComponent>();
		List<TextComponent> noPermCategories = new LinkedList<TextComponent>();
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		for (String category : categories.keySet()) {
			TextComponent categoryText = new TextComponent(category);
			String desc = categories.get(category);
			for (Powder powder : powderHandler.getPowdersFromCategory(category)) {
				if (!(PowderPlugin.getInstance().getPowderHandler().getPowderTasks(player, powder).isEmpty())) {
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
