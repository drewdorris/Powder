package com.ruinscraft.powder.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderTask;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderUtil {

	private static PowderPlugin plugin = PowderPlugin.getInstance();
	public static Random random = new Random();

	public static String PREFIX;
	public static ChatColor WARNING;
	public static ChatColor HIGHLIGHT;
	public static ChatColor HIGHLIGHT_TWO;
	public static ChatColor HIGHLIGHT_THREE;
	public static ChatColor INFO;
	public static ChatColor NO_PERM;

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static TextComponent format(String string) {
		return new TextComponent(TextComponent.fromLegacyText(string));
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
			for (UUID uuid : PowderPlugin.getInstance().getPowderHandler().getAllPowderTaskUsers()) {
				PowderUtil.sendPrefixMessage(Bukkit.getPlayer(uuid), PowderUtil.INFO 
						+ "Your Powders were cancelled due to a reload.", "powder");
			}
		}
	}
	
	// sends a message with the given prefix in config.yml
	// label is the base command, i.e. "powder" or "pdr" or "pow"
	public static void sendPrefixMessage(Player player, Object message, String label) {
		if (!(message instanceof String) && !(message instanceof BaseComponent)) {
			return;
		}
		if (message instanceof String) {
			String messageText = (String) message;
			message = new TextComponent(messageText);
		}

		BaseComponent fullMessage = new TextComponent();
		fullMessage.setColor(PowderUtil.INFO);
		TextComponent prefix = new TextComponent(PowderUtil.PREFIX);
		prefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label).color(PowderUtil.INFO).create() ) );
		prefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/" + label ) );
		fullMessage.addExtra(prefix);
		fullMessage.addExtra((TextComponent) message);

		player.spigot().sendMessage(fullMessage);
	}
	
	// reload config and all Powders, while saving database
	public static void reloadCommand() {
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

	// help message
	public static void helpMessage(Player player, String label, int page) {
		PowderUtil.sendPrefixMessage(player, PowderUtil.INFO + "Powder Help", label);
		List<TextComponent> texts = new ArrayList<TextComponent>();
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder <Powder> " + PowderUtil.INFO + "- Use a Powder"));
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder <Powder> cancel " + PowderUtil.INFO + "- Cancel a Powder"));
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder * cancel " + PowderUtil.INFO + "- Cancel all Powders"));
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder active " + PowderUtil.INFO + "- Show or stop active Powders"));
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder list [page] " + PowderUtil.INFO + "- List Powders by page"));
		if (PowderPlugin.getInstance().getPowderHandler().categoriesEnabled()) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder categories [page] " + PowderUtil.INFO + "- List all categories"));
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder category <category> [page] " + 
					PowderUtil.INFO + "- List all Powders in a category"));
		}
		texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
				"/powder search <term> [page] " + PowderUtil.INFO + "- Search for a Powder"));
		if (player.hasPermission("powder.reload")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder reload " + PowderUtil.INFO + "- Reload Powder"));
		}
		if (player.hasPermission("powder.nearby")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + "/powder nearby " + PowderUtil.INFO + "- Show nearby Powders"));
		}
		if (player.hasPermission("powder.create")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder create <name> <Powder> " + PowderUtil.INFO + "- Creates a named Powder at your location"));
		}
		if (player.hasPermission("powder.remove")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder remove <name> " + PowderUtil.INFO + "- Removes a named Powder"));
		}
		if (player.hasPermission("powder.addto")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder addto <name> <Powder> " + PowderUtil.INFO + "- Adds a Powder to an existing named Powder"));
		}
		if (player.hasPermission("powder.removefrom")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder removefrom <name> <Powder> " + PowderUtil.INFO + "- Removes a Powder from an existing named Powder"));
		}
		if (player.hasPermission("powder.cancel")) {
			texts.add(PowderUtil.format(PowderUtil.HIGHLIGHT + 
					"/powder cancel <name> " + PowderUtil.INFO + "- Cancels any currently active Powder"));
		}
		TextComponent comp1 = PowderUtil.format(PowderUtil.INFO + "It's also possible to " + PowderUtil.HIGHLIGHT + "click things in /" 
				+ label + PowderUtil.INFO + " to enable or cancel Powders." + 
				" Click the prefix in a message to return to the menu.");
		comp1.setColor(PowderUtil.INFO);
		texts.add(comp1);
		paginateAndSend(player, texts, " help ", page, 7, label);
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
	public static void paginateAndSend(Player player, List<TextComponent> listOfElements, String input, int page, int pageLength, String label) {
		List<TextComponent> pageList = new ArrayList<TextComponent>();
		// create list of Powders based on current page & given amnt per page
		for (int i = 1; i <= pageLength; i++) {
			TextComponent current;
			try {
				current = listOfElements.get((page * pageLength) + i - pageLength - 1);
			} catch (Exception e) {
				break;
			}
			pageList.add(current);
			TextComponent combinedMessage = new TextComponent(PowderUtil.INFO + 
					"| " + ChatColor.RESET);
			combinedMessage.addExtra(current);
			combinedMessage.setColor(PowderUtil.INFO);
			player.spigot().sendMessage(combinedMessage);
		}

		// create arrows
		TextComponent leftArrow = new TextComponent("<<  ");
		leftArrow.setColor(PowderUtil.HIGHLIGHT);
		leftArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + input + (page - 1) ) );
		leftArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Previous Page")
				.color(PowderUtil.HIGHLIGHT).create() ) );

		TextComponent middle = new TextComponent("Page (click)");
		middle.setColor(PowderUtil.HIGHLIGHT);

		TextComponent rightArrow = new TextComponent("  >>");
		rightArrow.setColor(PowderUtil.HIGHLIGHT);
		rightArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + input + (page + 1) ) );
		rightArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Next Page")
				.color(PowderUtil.HIGHLIGHT).create() ) );

		// adds the arrows to the message depending on where you are in the list
		TextComponent fullArrows = new TextComponent();
		if (pageList.isEmpty()) {
			player.sendMessage(PowderUtil.WARNING + "None found.");
			return;
		} else if ((!pageList.contains(listOfElements.get(0)) && pageList.contains(listOfElements.get(listOfElements.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
		} else if (!(pageList.contains(listOfElements.get(0))) && !(pageList.contains(listOfElements.get(listOfElements.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(listOfElements.get(0)) && !pageList.contains(listOfElements.get(listOfElements.size() - 1))) {
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(listOfElements.get(0)) && pageList.contains(listOfElements.get(listOfElements.size() - 1))) {
			return;
		}
		player.spigot().sendMessage(fullArrows);
	}

	// organizes given the given List<Powder> by active/allowed/not allowed Powders, alphabetizes, then paginates
	public static void listPowders(Player player, List<Powder> powders, String input, int page, int pageLength, String label) {
		TextComponent helpPrefix = new TextComponent(PowderUtil.INFO + "Use " +
				PowderUtil.HIGHLIGHT + "/" + label +  " help" + PowderUtil.INFO + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(PowderUtil.INFO).create() ) );
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
			if (!PowderUtil.hasPermission(player, powder)) {
				if (powder.isHidden()) {
					continue;
				}
				powderMapText.setColor(PowderUtil.NO_PERM);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("You don't have permission to use '" + powder.getName() + "'.")
						.color(PowderUtil.WARNING).create() ) );
				noPermPowders.add(powderMapText);
			} else if (!(PowderPlugin.getInstance().getPowderHandler().getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
				powderMapText.setColor(PowderUtil.HIGHLIGHT_TWO);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powder.getName() + "' is currently active. Click to cancel")
						.color(PowderUtil.HIGHLIGHT_TWO).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powder.getName() + " cancel" ) );
				activePowders.add(powderMapText);
			} else {
				powderMapText.setColor(PowderUtil.INFO);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click to use '" + powder.getName() + "'.")
						.color(PowderUtil.INFO).create() ) );
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
		paginateAndSend(player, listOfPowders, input, page, pageLength, label);
	}

	// similar to listPowders but lists categories instead
	public static void listCategories(Player player, Map<String, String> categories, String input, int page, int pageLength, String label) {
		TextComponent helpPrefix = new TextComponent(PowderUtil.INFO + "Use " +
				PowderUtil.HIGHLIGHT + "/" + label +  " help" + PowderUtil.INFO + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(PowderUtil.INFO).create() ) );
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
					categoryText.setColor(PowderUtil.HIGHLIGHT_TWO);
					categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
							new ComponentBuilder(desc + " - " + "'" + category + "' contains currently active Powders.")
							.color(PowderUtil.INFO).create() ) );
					categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
							"/" + label + " " + category + " 1" ) );
					if (!activeCategories.contains(categoryText)) {
						activeCategories.add(categoryText);
					}
					break;
				} else {
					if (PowderUtil.hasPermission(player, powder)) {
						categoryText.setColor(PowderUtil.INFO);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc)
								.color(PowderUtil.INFO).create() ) );
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
						categoryText.setColor(PowderUtil.NO_PERM);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc + " - This category is empty.")
								.color(PowderUtil.INFO).create() ) );
						categoryText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
								"/" + label + " " + category + " 1" ) );
					} else {
						categoryText.setColor(PowderUtil.NO_PERM);
						categoryText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
								new ComponentBuilder(desc + " - " + "You don't have permission to use any Powders in '" + category + "'.")
								.color(PowderUtil.INFO).create() ) );
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

		paginateAndSend(player, listOfCategories, input, page, pageLength, label);
	}

	public static Set<UUID> getOnlineUUIDs() {
		return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
	}

	public static Random getRandom() {
		return random;
	}

	public static String generateID(int length) {
		StringBuilder id = new StringBuilder("");
		for (int i = 0; i < length; i++) {
			byte randomValue = (byte) ((random.nextInt(26) + 65) + (32 * random.nextInt(2)));
			id.append(Character.toString((char) randomValue));
		}
		return id.toString();
	}

	// returns a URL from a string, adds http:// if appended
	public static URL readURL(String urlName) {
		URL url;
		try {
			url = new URL(urlName);
		} catch (MalformedURLException mal) {
			String urlString = urlName;
			if (!(urlString.contains("http"))) {
				try {
					url = new URL("http://" + urlString);
				} catch (Exception mal2) {
					plugin.getLogger().warning("Invalid URL: '" + urlName + "'");
					mal2.printStackTrace();
					return null;
				}
			} else {
				plugin.getLogger().warning("Invalid URL: '" + urlName + "'");
				mal.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			plugin.getLogger().warning("Invalid URL: '" + urlName + "'");
			return null;
		}
		return url;
	}

	// returns an InputStream from the given URL
	public static InputStream getInputStreamFromURL(URL url) {
		HttpURLConnection httpConnection;
		InputStream stream;
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			httpConnection.connect();

			stream = httpConnection.getInputStream();

			if (httpConnection.getResponseCode() == 301) {

				String urlString = url.toString();
				if (urlString.contains("https")) {
					plugin.getLogger().warning("Failed to load URL '" + urlString + "'.");
					return null;
				}
				// try again to see if the site requires https
				urlString = urlString.replaceAll("http", "https");
				url = new URL(urlString);
				return getInputStreamFromURL(url);

			} else if (!(httpConnection.getResponseCode() == 200)) {
				plugin.getLogger().warning("Error" + httpConnection.getResponseCode() + " while attempting to read URL: " + url.toString());
				return null;
			}

		} catch (IOException io) {
			return null;
		}

		return stream;
	}

	// cancels powders for logout
	public static void unloadPlayer(Player player) {
		cancelAllPowders(player.getUniqueId());
	}

	// loads player from database
	public static void loadPlayer(Player player) {
		loadPowdersForPlayer(player.getUniqueId());
	}

	public static void unloadServerPowders() {

	}

	public static void loadServerPowders() {

	}

	// cosine of the given rotation, and multiplies it by the given spacing
	public static double getDirLengthX(double rot, double spacing) {
		return (spacing * Math.cos(rot));
	}

	// sine of the given rotation, and multiplies it by the given spacing
	public static double getDirLengthZ(double rot, double spacing) {
		return (spacing * Math.sin(rot));
	}

	// cancels a given Powder for the given player
	public static boolean cancelPowder(UUID uuid, Powder powder) {
		PowderHandler powderHandler = plugin.getPowderHandler();

		boolean success = false;

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid, powder)) {
			powderHandler.removePowderTask(powderTask);
			success = true;
		}

		if (success && plugin.useStorage()) {
			savePowdersForPlayer(uuid);
		}

		return success;
	}

	public static int cancelAllPowders(UUID uuid) {
		int amt = 0;

		PowderHandler powderHandler = plugin.getPowderHandler();

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid)) {
			powderHandler.removePowderTask(powderTask);
			amt++;
		}

		if (plugin.useStorage()) {
			savePowdersForPlayer(uuid);
		}

		return amt;
	}

	// get names of enabled Powders for a user
	public static List<String> getEnabledPowderNames(UUID uuid) {
		PowderHandler powderHandler = plugin.getPowderHandler();

		List<String> enabledPowders = new ArrayList<>();

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid)) {
			for (Powder powder : powderTask.getPowders().keySet()) {
				if (powder.hasMovement()) {
					enabledPowders.add(powder.getName());
				}
			}
		}

		return enabledPowders;
	}

	// loads and creates a Powder (used for storage loading)
	public static void createPowderFromName(Player player, String powderName) {
		PowderHandler handler = plugin.getPowderHandler();

		Powder powder = handler.getPowder(powderName);

		if (powder == null) {
			return;
		}

		if (!PowderUtil.hasPermission(player, powder)) {
			return;
		}

		powder.spawn(player);
	}

	public static void savePowdersForPlayer(UUID uuid) {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().save(uuid, PowderUtil.getEnabledPowderNames(uuid));
			});
		}
	}

	public static void loadPowdersForPlayer(UUID uuid) {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				List<String> powders = plugin.getStorage().get(uuid);

				for (String powder : powders) {
					createPowderFromName(Bukkit.getPlayer(uuid), powder);
				}
			});
		}
	}

	public static void savePowdersForOnline() {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().saveBatch(PowderUtil.getOnlineUUIDs());
			});
		}
	}

	public static void loadPowdersForOnline() {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				Map<UUID, List<String>> enabledPowders = plugin.getStorage().getBatch(PowderUtil.getOnlineUUIDs());

				for (Map.Entry<UUID, List<String>> entry : enabledPowders.entrySet()) {
					for (String powder : entry.getValue()) {
						createPowderFromName(Bukkit.getPlayer(entry.getKey()), powder);
					}
				}
			});
		}
	}

}
