package com.ruinscraft.powder.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.Message;
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

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static TextComponent format(String string) {
		return new TextComponent(TextComponent.fromLegacyText(string));
	}

	// check permission for the Powder or for a category that contains the Powder
	public static boolean hasPermission(Player player, Powder powder) {
		if (player.hasPermission("powder.powder.*") || 
				player.hasPermission("powder.powder." + 
						powder.getName().toLowerCase(Locale.US))) {
			return true;
		}
		for (String category : powder.getCategories()) {
			if (player.hasPermission("powder.category." + category.toLowerCase(Locale.US))) {
				return true;
			}
		}
		return false;
	}

	public static TextComponent getMessage(Message message) {
		return new TextComponent(PowderPlugin.getInstance().getMessages().get(message));
	}

	public static TextComponent replace(TextComponent text, String lookFor, String replace) {
		TextComponent textComponent = new TextComponent(text);
		textComponent.setText(textComponent.getText().replaceAll(lookFor, replace));
		return textComponent;
	}

	public static String setString(Message message, String... replacers) {
		TextComponent textComponent = getMessage(message);
		String actualText = textComponent.toLegacyText();
		if (message.name().contains("CLICK")) {
			actualText = textComponent.toPlainText();
		}
		for (int i = 0; i < message.getPlaceholders().length; i++) {
			actualText = actualText.replace(message.getPlaceholders()[i], replacers[i]);
		}
		return actualText;
	}

	public static TextComponent setText(Message message, String... replacers) {
		TextComponent textComponent = getMessage(message);
		String actualText = textComponent.toLegacyText();
		for (int i = 0; i < message.getPlaceholders().length; i++) {
			actualText = actualText.replace(message.getPlaceholders()[i], replacers[i]);
		}
		TextComponent newText = new TextComponent(actualText);
		return newText;
	}

	public static TextComponent setTextAndHover(Message message, 
			Message hover, String... replacers) {
		TextComponent textComponent = setText(message, replacers);
		textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder(setString(hover, replacers)).create()));
		return textComponent;
	}

	public static TextComponent setTextHoverAndClick(Message message, Message hover,
			Message click, String... replacers) {
		TextComponent textComponent = setText(message, replacers);
		textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder(setString(hover, replacers)).create()));
		textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
				setString(click, replacers)));
		return textComponent;
	}

	// sends a message with the given prefix in config.yml
	// label is the base command, i.e. "powder" or "pdr" or "pow"
	public static void sendPrefixMessage(Player player, String message, String label) {
		TextComponent textComponent = format(message);
		sendPrefixMessage(player, textComponent, label);
	}

	public static void sendPrefixMessage(Player player, Message message, 
			String label, String... replacers) {
		TextComponent text = PowderUtil.getMessage(message);
		String actualText = text.toLegacyText();
		for (int i = 0; i < message.getPlaceholders().length; i++) {
			actualText = actualText.replace(message.getPlaceholders()[i], replacers[i]);
		}
		TextComponent newText = new TextComponent(actualText);
		sendPrefixMessage(player, newText, label);
	}

	public static void sendPrefixMessage(Player player, Message message, 
			Message hover, String label, String... replacers) {
		TextComponent text = setTextAndHover(message, hover, replacers);
		sendPrefixMessage(player, text, label);
	}

	public static void sendPrefixMessage(Player player, TextComponent message, String label) {
		BaseComponent fullMessage = new TextComponent();
		fullMessage.addExtra(setTextHoverAndClick(
				Message.PREFIX, Message.PREFIX_HOVER, Message.PREFIX_CLICK, label));
		fullMessage.addExtra(message);
		player.spigot().sendMessage(fullMessage);
	}

	// help message
	public static void helpMessage(Player player, String label, int page) {
		PowderUtil.sendPrefixMessage(player, Message.HELP_PREFIX, label);
		List<TextComponent> texts = new ArrayList<TextComponent>();
		texts.add(getMessage(Message.HELP_POWDER));
		texts.add(getMessage(Message.HELP_POWDER_CANCEL));
		texts.add(getMessage(Message.HELP_POWDER_STAR_CANCEL));
		texts.add(getMessage(Message.HELP_ACTIVE));
		texts.add(getMessage(Message.HELP_LIST));
		if (PowderPlugin.getInstance().getPowderHandler().categoriesEnabled()) {
			texts.add(getMessage(Message.HELP_CATEGORIES));
			texts.add(getMessage(Message.HELP_CATEGORY));
		}
		texts.add(getMessage(Message.HELP_SEARCH));
		if (player.hasPermission("powder.reload")) {
			texts.add(getMessage(Message.HELP_RELOAD));
		}
		if (player.hasPermission("powder.nearby")) {
			texts.add(getMessage(Message.HELP_NEARBY));
		}
		if (player.hasPermission("powder.create")) {
			texts.add(getMessage(Message.HELP_CREATE));
		}
		if (player.hasPermission("powder.remove")) {
			texts.add(getMessage(Message.HELP_REMOVE));
		}
		if (player.hasPermission("powder.attach")) {
			texts.add(getMessage(Message.HELP_ATTACH));
		}
		if (player.hasPermission("powder.addto")) {
			texts.add(getMessage(Message.HELP_ADDTO));
		}
		if (player.hasPermission("powder.removefrom")) {
			texts.add(getMessage(Message.HELP_REMOVEFROM));
		}
		if (player.hasPermission("powder.cancel")) {
			texts.add(getMessage(Message.HELP_CANCEL));
		}
		TextComponent comp1 = getMessage(Message.HELP_EXTRA);
		comp1.setColor(comp1.getColor());
		texts.add(comp1);
		paginateAndSend(player, texts, " help ", page, 7, label);
	}

	public static void sendMainConsoleMessage() {
		PowderPlugin.getInstance().getLogger().info("Powder");
		PowderPlugin.getInstance().getLogger().info("Use '/powder reload' to reload");
	}

	// sorts a list of TextComponents (Powders or categories) alphabetically
	public static List<TextComponent> sortAlphabetically(Collection<TextComponent> texts) {
		List<String> names = new ArrayList<String>(texts.size()); 

		for (TextComponent text : texts) {
			names.add(text.getText());
		}

		Collections.sort(names, Collator.getInstance());
		List<TextComponent> newList = new ArrayList<TextComponent>(texts.size());

		for (String name : names) {
			for (TextComponent text : texts) {
				if (text.getText() == name) {
					newList.add(text);
					break;
				}
			}
		}

		return newList;
	}

	// paginates & sends list of Powders/categories to player
	public static void paginateAndSend(Player player, List<TextComponent> listOfElements, 
			String input, int page, int pageLength, String label) {
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
			TextComponent combinedMessage = getMessage(Message.LIST_GENERAL_ITEM);
			combinedMessage.addExtra(current);
			player.spigot().sendMessage(combinedMessage);
		}

		// create arrows
		TextComponent leftArrow = setTextHoverAndClick(Message.LIST_GENERAL_LEFT, 
				Message.LIST_GENERAL_LEFT_HOVER, 
				Message.LIST_GENERAL_LEFT_CLICK, 
				label, input, String.valueOf(page - 1));

		TextComponent middle = getMessage(Message.LIST_GENERAL_MIDDLE);

		TextComponent rightArrow = setTextHoverAndClick(Message.LIST_GENERAL_RIGHT,
				Message.LIST_GENERAL_RIGHT_HOVER,
				Message.LIST_GENERAL_RIGHT_CLICK,
				label, input, String.valueOf(page + 1));

		// adds the arrows to the message depending on where you are in the list
		TextComponent fullArrows = new TextComponent();
		if (pageList.isEmpty()) {
			player.spigot().sendMessage(getMessage(Message.LIST_NO_ELEMENTS));
			return;
		} else if ((!pageList.contains(listOfElements.get(0)) 
				&& pageList.contains(listOfElements.get(listOfElements.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
		} else if (!(pageList.contains(listOfElements.get(0))) 
				&& !(pageList.contains(listOfElements.get(listOfElements.size() - 1)))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(listOfElements.get(0)) 
				&& !pageList.contains(listOfElements.get(listOfElements.size() - 1))) {
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(listOfElements.get(0)) 
				&& pageList.contains(listOfElements.get(listOfElements.size() - 1))) {
			return;
		}
		player.spigot().sendMessage(fullArrows);
	}

	// organizes given the given List<Powder> by active/allowed/not allowed Powders, 
	// alphabetizes, then paginates
	public static void listPowders(Player player, List<Powder> powders, 
			String input, int page, int pageLength, String label) {
		PowderUtil.sendPrefixMessage(player, setTextHoverAndClick(Message.HELP_TIP, 
				Message.HELP_TIP_HOVER, Message.HELP_TIP_CLICK, label), label);

		// all Powders
		List<TextComponent> listOfPowders = new ArrayList<TextComponent>();
		// Powders currently in use by the player
		List<TextComponent> activePowders = new ArrayList<TextComponent>();
		// Powders the player has permission for
		List<TextComponent> ableToPowders = new ArrayList<TextComponent>();
		// Powders the player does not have permission for
		List<TextComponent> noPermPowders = new ArrayList<TextComponent>();
		for (Powder powder : powders) {
			if (!PowderUtil.hasPermission(player, powder)) {
				if (powder.isHidden()) {
					continue;
				}
				noPermPowders.add(setTextAndHover(Message.LIST_POWDER_NO_PERM, 
						Message.LIST_POWDER_NO_PERM_HOVER, powder.getName()));
			} else if (!(PowderPlugin.getInstance().getPowderHandler()
					.getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
				activePowders.add(setTextHoverAndClick(Message.LIST_POWDER_ACTIVE, 
						Message.LIST_POWDER_ACTIVE_HOVER, Message.LIST_POWDER_ACTIVE_CLICK, 
						label, powder.getName()));
			} else {
				ableToPowders.add(setTextHoverAndClick(Message.LIST_POWDER_REGULAR, 
						Message.LIST_POWDER_REGULAR_HOVER, Message.LIST_POWDER_REGULAR_CLICK, 
						label, powder.getName()));
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
	public static void listCategories(Player player, Map<String, String> categories, 
			String input, int page, int pageLength, String label) {
		PowderUtil.sendPrefixMessage(player, setTextHoverAndClick(Message.HELP_TIP, 
				Message.HELP_TIP_HOVER, Message.HELP_TIP_CLICK, label), label);

		// all categories
		List<TextComponent> listOfCategories = new LinkedList<TextComponent>();
		// categories containing Powders that are currently active
		Set<TextComponent> activeCategories = new HashSet<TextComponent>();
		// categories containing Powders the player has permission for
		Set<TextComponent> ableToCategories = new HashSet<TextComponent>();
		// categories containing Powders the player has no permission for
		// or contains no Powders
		Set<TextComponent> noPermCategories = new HashSet<TextComponent>();
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		for (String category : categories.keySet()) {
			String desc = categories.get(category);
			int i = 0;
			boolean active = false;
			boolean isAllowed = false;
			for (Powder powder : powderHandler.getPowdersFromCategory(category)) {
				i++;
				if (!(PowderPlugin.getInstance().getPowderHandler()
						.getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
					active = true;
					break;
				} else {
					if (PowderUtil.hasPermission(player, powder)) {
						isAllowed = true;
					} else if (!isAllowed) {
					}
				}
			}
			if (i == 0) {
				noPermCategories.add(PowderUtil.setTextHoverAndClick(
						Message.LIST_CATEGORY_NO_PERM, 
						Message.LIST_CATEGORY_NO_PERM_HOVER_EMPTY, 
						Message.LIST_CATEGORY_NO_PERM_CLICK, 
						label, category, desc));
			} else if (active) {
				activeCategories.add(PowderUtil.setTextHoverAndClick(
						Message.LIST_CATEGORY_ACTIVE, 
						Message.LIST_CATEGORY_ACTIVE_HOVER, 
						Message.LIST_CATEGORY_ACTIVE_CLICK, 
						label, category, desc));
			} else if (isAllowed) {
				ableToCategories.add(PowderUtil.setTextHoverAndClick(
						Message.LIST_CATEGORY_REGULAR, 
						Message.LIST_CATEGORY_REGULAR_HOVER, 
						Message.LIST_CATEGORY_REGULAR_CLICK, 
						label, category, desc));
			} else {
				noPermCategories.add(PowderUtil.setTextHoverAndClick(
						Message.LIST_CATEGORY_NO_PERM, 
						Message.LIST_CATEGORY_NO_PERM_HOVER_PERM, 
						Message.LIST_CATEGORY_NO_PERM_CLICK, 
						label, category, desc));
			}
		}
		activeCategories = sortAlphabetically(activeCategories)
				.stream().collect(Collectors.toSet());
		ableToCategories = sortAlphabetically(ableToCategories)
				.stream().collect(Collectors.toSet());
		noPermCategories = sortAlphabetically(noPermCategories)
				.stream().collect(Collectors.toSet());
		listOfCategories.addAll(activeCategories);
		listOfCategories.addAll(ableToCategories);
		listOfCategories.addAll(noPermCategories);

		paginateAndSend(player, listOfCategories, input, page, pageLength, label);
	}

	public static Entity getNearestEntityInSight(Player player, int range) {
		List<Entity> entities = player.getNearbyEntities(range, range, range);
		List<Location> locations = player.getLineOfSight(
				Stream.of(Material.AIR, Material.WATER)
				.collect(Collectors.toSet()), 7).stream()
				.map(Block::getLocation).collect(Collectors.toList());
		Entity nearestEntity = null;
		double closest = range;
		for (Location location : locations) {
			for (Entity entity : entities) {
				double distance = location.distanceSquared(entity.getLocation());
				if (distance < closest) {
					nearestEntity = entity;
					closest = distance;
				}
			}
		}
		return nearestEntity;
	}

	public static String cleanEntityName(Entity entity) {
		String name = entity.getName().replaceAll(" ", "-");
		if (name.contains("entity")) {
			name = name.replace("entity.", "").replace(".name", "");
		}
		if (name == "null") {
			name = "Unknown";
		}
		return name;
	}

	public static Set<UUID> getOnlinePlayerUUIDs() {
		return Bukkit.getOnlinePlayers().stream()
				.map(Player::getUniqueId).collect(Collectors.toSet());
	}

	public static Set<UUID> getOnlineUUIDs() {
		Set<UUID> uuids = getOnlinePlayerUUIDs();
		for (World world : Bukkit.getWorlds()) {
			uuids.addAll(world.getEntities().stream()
					.map(Entity::getUniqueId).collect(Collectors.toSet()));
		}
		return uuids;
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

	public static String getFileNameFromURL(String url) {
		if (url.contains("/")) {
			return url.substring(url.indexOf("/"), url.length() - 1);
		} else {
			return url;
		}
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
			httpConnection.setRequestProperty("User-Agent", 
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 " + 
					"(KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
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
				plugin.getLogger().warning("Error" + 
						httpConnection.getResponseCode() + 
						" while attempting to read URL: " + url.toString());
				return null;
			}

		} catch (IOException io) {
			return null;
		}

		return stream;
	}

	// cancels powders for logout
	public static void unloadUUID(UUID uuid) {
		cancelAllPowders(uuid);
	}

	// loads player from database
	public static void loadUUID(UUID uuid) {
		loadPowdersForUUID(uuid);
	}

	// cancels a given Powder for the given player
	public static boolean cancelPowder(UUID uuid, Powder powder) {
		PowderHandler powderHandler = plugin.getPowderHandler();

		boolean success = false;

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid, powder)) {
			powderHandler.removePowderTask(powderTask);
			success = true;
		}

		if (success) {
			savePowdersForUUID(uuid);
		}

		return success;
	}

	// cancels a given Powder for the given player and saves that to database
	public static boolean cancelPowderAndSave(UUID uuid, Powder powder) {
		if (cancelPowder(uuid, powder)) {
			savePowdersForUUID(uuid);
			return true;
		} else {
			return false;
		}
	}

	// cancels all Powders for the given player
	public static int cancelAllPowders(UUID uuid) {
		int amt = 0;

		PowderHandler powderHandler = plugin.getPowderHandler();

		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid)) {
			powderHandler.removePowderTask(powderTask);
			amt++;
		}

		return amt;
	}

	// cancels all Powders for the given player and saves that to database
	public static int cancelAllPowdersAndSave(UUID uuid) {
		int amt = cancelAllPowders(uuid);

		savePowdersForUUID(uuid);

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
	public static void createPowderFromName(UUID uuid, String powderName) {
		PowderHandler handler = plugin.getPowderHandler();

		Powder powder = handler.getPowder(powderName);

		if (powder == null) {
			return;
		}

		Player player = Bukkit.getPlayer(uuid);
		Entity entity = Bukkit.getEntity(uuid);
		if (player != null) {
			if (!PowderUtil.hasPermission(player, powder)) {
				return;
			}
			powder.spawn(player);
		} else if (entity != null) {
			powder.spawn(entity);
		}
	}

	public static void savePowdersForUUID(UUID uuid) {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().save(uuid, PowderUtil.getEnabledPowderNames(uuid));
			});
		}
	}

	public static void loadPowdersForUUID(UUID uuid) {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				List<String> powders = plugin.getStorage().get(uuid);

				for (String powder : powders) {
					createPowderFromName(uuid, powder);
				}
			});
		}
	}

	public static void savePowdersForOnline() {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().saveBatch(
						plugin.getPowderHandler().getAllPowderTaskUUIDs());
			});
		}
	}

	public static void loadPowdersForOnline() {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				Map<UUID, List<String>> enabledPowders = plugin.getStorage()
						.getBatch(PowderUtil.getOnlineUUIDs());

				for (Map.Entry<UUID, List<String>> entry : enabledPowders.entrySet()) {
					for (String powder : entry.getValue()) {
						createPowderFromName(entry.getKey(), powder);
					}
				}
			});
		}
	}

}
