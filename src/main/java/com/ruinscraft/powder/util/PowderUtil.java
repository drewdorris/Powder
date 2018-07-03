package com.ruinscraft.powder.util;

import java.io.File;
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
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.ParticleMatrix;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.particle.PowderParticle;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderUtil {

	private static PowderPlugin plugin = PowderPlugin.getInstance();
	public static Random random = new Random();

	private static Set<UUID> recentlyLoadedUUIDs = new HashSet<>();

	public static String color(String string) {
		String newString = "";
		String newestColor = "";
		while (string.indexOf(" ") != -1 || string.length() >= 1) {
			if (string.indexOf(" ") == 0) {
				string = string.substring(string.indexOf(" ") + 1);
				continue;
			}
			String oneWordString;
			if (string.indexOf(" ") != -1) {
				oneWordString = string.substring(0, string.indexOf(" ") + 1);
				string = string.substring(string.indexOf(" ") + 1);
			} else {
				oneWordString = string.substring(0, string.length());
				string = "";
			}
			if (oneWordString.contains("&")) {
				String colorChar = String.valueOf(oneWordString.charAt(
						oneWordString.indexOf("&") + 1));
				if (colorChar != null) {
					newestColor = "&" + colorChar;
				}
			} else {
				oneWordString = newestColor + oneWordString;
			}
			newString = newString + oneWordString;
		}
		return ChatColor.translateAlternateColorCodes('&', newString);
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
		List<TextComponent> texts = new ArrayList<>();
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
		List<String> names = new ArrayList<>(texts.size()); 
		for (TextComponent text : texts) {
			names.add(text.toLegacyText());
		}

		Collections.sort(names, Collator.getInstance());

		List<TextComponent> newList = new ArrayList<>(texts.size());
		for (String name : names) {
			for (TextComponent text : texts) {
				if (text.toLegacyText().equals(name)) {
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
		List<TextComponent> listOfPowders = new ArrayList<>();
		// Powders currently in use by the player
		List<TextComponent> activePowders = new ArrayList<>();
		// Powders the player has permission for
		List<TextComponent> ableToPowders = new ArrayList<>();
		// Powders the player does not have permission for
		List<TextComponent> noPermPowders = new ArrayList<>();
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
		List<TextComponent> listOfCategories = new ArrayList<>();
		// categories containing Powders that are currently active
		List<TextComponent> activeCategories = new ArrayList<>();
		// categories containing Powders the player has permission for
		List<TextComponent> ableToCategories = new ArrayList<>();
		// categories containing Powders the player has no permission for
		// or contains no Powders
		List<TextComponent> noPermCategories = new ArrayList<>();
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
		activeCategories = sortAlphabetically(activeCategories);
		ableToCategories = sortAlphabetically(ableToCategories);
		noPermCategories = sortAlphabetically(noPermCategories);
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

	public static boolean fileExists(String folder, String fileName) {
		return new File(plugin.getDataFolder() + folder, fileName).exists();
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

	public static String cleanPowderTaskName(PowderTask powderTask) {
		return powderTask.getName().replace(".", "-");
	}

	public static boolean recentlyLoaded(UUID uuid) {
		return recentlyLoadedUUIDs.contains(uuid);
	}

	// loads player from database
	public static void loadUUID(UUID uuid) {
		loadPowdersForUUID(uuid);
		plugin.getServer().getScheduler()
		.scheduleSyncDelayedTask(PowderPlugin.getInstance(), () -> {
			recentlyLoadedUUIDs.remove(uuid);
		}, 20L);
		recentlyLoadedUUIDs.add(uuid);
	}

	// loads player from database
	public static void loadUUIDs(Collection<UUID> uuids) {
		loadPowdersForUUIDs(uuids);
		plugin.getServer().getScheduler()
		.scheduleSyncDelayedTask(PowderPlugin.getInstance(), () -> {
			recentlyLoadedUUIDs.removeAll(uuids);
		}, 20L);
		recentlyLoadedUUIDs.addAll(uuids);
	}

	// loads player from database
	public static void loadAllUUIDs() {
		loadUUIDs(PowderUtil.getOnlineUUIDs());
	}

	// cancels all Powders for the given player
	public static int cancelAllPowders(UUID uuid) {
		int amt = 0;
		PowderHandler powderHandler = plugin.getPowderHandler();
		for (PowderTask powderTask : powderHandler.getPowderTasks(uuid)) {
			powderHandler.cancelPowderTask(powderTask);
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
		PowderHandler powderHandler = plugin.getPowderHandler();

		Powder powder = powderHandler.getPowder(powderName);

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
			if (recentlyLoaded(uuid)) {
				return;
			}
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().save(uuid, PowderUtil.getEnabledPowderNames(uuid));
			});
		}
	}

	public static void savePowdersForUUIDAndCancel(UUID uuid) {
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().save(uuid, PowderUtil.getEnabledPowderNames(uuid));
				PowderUtil.cancelAllPowders(uuid);
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

	public static void savePowdersForUUIDs(Collection<UUID> uuids) {
		if (uuids.isEmpty()) {
			return;
		}
		Set<UUID> filteredUUIDs = uuids.stream()
				.filter(uuid -> !PowderUtil.recentlyLoaded(uuid)).collect(Collectors.toSet());
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				plugin.getStorage().saveBatch(filteredUUIDs);
			});
		}
	}

	public static void loadPowdersForUUIDs(Collection<UUID> uuids) {
		if (uuids.isEmpty()) {
			return;
		}
		if (plugin.useStorage()) {
			plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
				Map<UUID, List<String>> enabledPowders = plugin.getStorage()
						.getBatch(uuids);

				for (Map.Entry<UUID, List<String>> entry : enabledPowders.entrySet()) {
					for (String powder : entry.getValue()) {
						createPowderFromName(entry.getKey(), powder);
					}
				}
			});
		}
	}

	private static ParticleMatrix setDefaults(
			ParticleMatrix matrix, ParticleMatrix newMatrix, int newStartTime) {
		newMatrix.setSpacing(matrix.getSpacing());
		newMatrix.setAddedPitch(matrix.getAddedPitch());
		newMatrix.setAddedRotation(matrix.getAddedRotation());
		newMatrix.setAddedTilt(matrix.getAddedTilt());
		newMatrix.setIfPitch(matrix.hasPitch());
		newMatrix.setPlayerLeft(matrix.getPlayerLeft());
		newMatrix.setPlayerUp(matrix.getPlayerUp());
		newMatrix.setStartTime(newStartTime);
		newMatrix.setRepeatTime(matrix.getRepeatTime());
		newMatrix.setLockedIterations(matrix.getLockedIterations());
		return newMatrix;
	}

	public static List<ParticleMatrix> setGradients(ParticleMatrix matrix, 
			int gradient, int tickSpeed, int length) {
		List<ParticleMatrix> newMatrices = new ArrayList<>();
		switch (gradient) {
			// diagram https://i.imgur.com/0uL5i3a.png
			case 1: {
				// 26 25
				int newStartTime = matrix.getStartTime();
				int y = 0;
				boolean started = false;
				while (y <= matrix.getMaxY()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
							for (int x = 0; x <= matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						y++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 2: {
				// 25 26
				int newStartTime = matrix.getStartTime();
				int y = matrix.getMaxY();
				boolean started = false;
				while (y >= 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
							for (int x = 0; x <= matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						y--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 3: {
				// 24 22
				int newStartTime = matrix.getStartTime();
				int x = 0;
				boolean started = false;
				while (x <= matrix.getMaxX()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
							for (int y = 0; y <= matrix.getMaxY(); y++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						x++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 4: {
				// 22 24
				int newStartTime = matrix.getStartTime();
				int x = matrix.getMaxX();
				boolean started = false;
				while (x >= 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
							for (int y = 0; y <= matrix.getMaxY(); y++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						x--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 5: {
				// 21 23
				int newStartTime = matrix.getStartTime();
				int z = matrix.getMinZ();
				boolean started = false;
				while (z <= matrix.getMaxZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; x <= matrix.getMaxX(); x++) {
							for (int y = 0; y <= matrix.getMaxY(); y++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						z++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 6: {
				// 23 21
				int newStartTime = matrix.getStartTime();
				int z = matrix.getMaxZ();
				boolean started = false;
				while (z >= matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; x <= matrix.getMaxX(); x++) {
							for (int y = 0; y <= matrix.getMaxY(); y++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						z--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 7: {
				// 2 12
				int lowest = matrix.getMinZ();
				int longest = matrix.getMaxX();
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = longest; x >= longest - matrix.getMaxDistance() + soFar; x--) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int z = matrix.getMaxZ() - (soFar - y - x);
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 8: {
				// 1 11
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; x <= matrix.getMaxDistance() - soFar; x++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int z = matrix.getMaxZ() + matrix.getMaxX() - 
										soFar + y - x;
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 9: {
				// 11 1
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = 0;
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; x <= matrix.getMaxDistance() - soFar; x++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int z = matrix.getMaxZ() + matrix.getMaxX() - 
										soFar + y - x;
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 10: {
				// 12 2
				int lowest = matrix.getMinZ();
				int longest = matrix.getMaxX();
				int newStartTime = matrix.getStartTime();
				int soFar = 0;
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = longest; x >= longest - matrix.getMaxDistance() + soFar; x--) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int z = matrix.getMaxZ() - (soFar - y - x);
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 11: {
				// 10 3
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = lowest;
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = lowest; z <= soFar; z++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								if (y + z > soFar) {
									break;
								}
								int x = matrix.getMaxX() - (soFar - y - z);
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 12: {
				// 9 4
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = lowest;
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = lowest; z <= soFar; z++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int x = soFar - y - z;
								if (x < 0) {
									break;
								}
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 13: {
				// 4 9
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= lowest) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = lowest; z <= soFar; z++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								int x = soFar - y - z;
								if (x < 0) {
									break;
								}
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 14: {
				// 3 10
				int lowest = matrix.getMinZ();
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= lowest) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = lowest; z <= soFar; z++) {
							for (int y = 0; y <= soFar - lowest; y++) {
								if (y + z > soFar) {
									break;
								}
								int x = matrix.getMaxX() - (soFar - y - z);
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 15: {
				// 13 7
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMaxZ(); 
								z >= matrix.getMaxZ() - soFar; z--) {
							int y = matrix.getMaxY() - 
									(soFar - (matrix.getMaxZ() - z));
							for (int x = 0; x < matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 16: {
				// 5 15
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMaxZ(); 
								z >= matrix.getMaxZ() - soFar; z--) {
							int y = soFar - (matrix.getMaxZ() - z);
							for (int x = 0; x < matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 17: {
				// 7 13
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMinZ();
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMaxZ(); 
								z >= matrix.getMaxZ() - soFar; z--) {
							int y = matrix.getMaxY() - 
									(soFar - (matrix.getMaxZ() - z));
							for (int x = 0; x < matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 18: {
				// 15 5
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMinZ();
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int z = matrix.getMaxZ(); 
								z >= matrix.getMaxZ() - soFar; z--) {
							int y = soFar - (matrix.getMaxZ() - z);
							for (int x = 0; x < matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 19: {
				// 16 6
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMinZ();
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; 
								x <= soFar; x++) {
							int y = soFar - x;
							for (int z = matrix.getMinZ(); 
									z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 20: {
				// 8 14
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMinZ();
				boolean started = false;
				while (soFar <= matrix.getMaxDistance()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; 
								x <= soFar; x++) {
							int y = matrix.getMaxY() - (soFar - x);
							for (int z = matrix.getMinZ(); 
									z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar++;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 21: {
				// 6 16
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; 
								x <= soFar; x++) {
							int y = soFar - x;
							for (int z = matrix.getMinZ(); 
									z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			case 22: {
				// 14 8
				int newStartTime = matrix.getStartTime();
				int soFar = matrix.getMaxDistance();
				boolean started = false;
				while (soFar >= matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int t = 0; t < length; t++) {
						for (int x = 0; 
								x <= soFar; x++) {
							int y = matrix.getMaxY() - (soFar - x);
							for (int z = matrix.getMinZ(); 
									z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						soFar--;
					}

					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, newStartTime));
						started = true;
					}
					if (started) {
						newStartTime = newStartTime + tickSpeed;
					}
				}
				break;
			}
			default: {
				newMatrices.add(matrix);
			}
		}
		return newMatrices;
	}

	public static List<ParticleMatrix> setTwist(ParticleMatrix matrix, 
			int type, int magnitude, int length, int startingPoint) {
		List<ParticleMatrix> newMatrices = new ArrayList<>();
		switch (type) {
		    case 1: {
		    	int y = startingPoint;
				if (y > 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int newy = 0; newy < y; newy++) {
						for (int x = 0; x <= matrix.getMaxX(); x++) {
							for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, newy, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, newy, z);
							}
						}
					}
					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, matrix.getStartTime()));
					}
				}
				int rotation = 0;
				while (y <= matrix.getMaxY()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int i = 0; i < length; i++) {
						for (int x = 0; x <= matrix.getMaxX(); x++) {
							for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						y++;
					}

					if (newMatrix.hasParticles()) {
						newMatrix.setSpacing(matrix.getSpacing());
						newMatrix.setAddedPitch(matrix.getAddedPitch());
						newMatrix.setAddedRotation(matrix.getAddedRotation() + rotation);
						newMatrix.setAddedTilt(matrix.getAddedTilt());
						newMatrix.setIfPitch(matrix.hasPitch());
						newMatrix.setPlayerLeft(matrix.getPlayerLeft());
						newMatrix.setPlayerUp(matrix.getPlayerUp());
						newMatrix.setStartTime(matrix.getStartTime());
						newMatrix.setRepeatTime(matrix.getRepeatTime());
						newMatrix.setLockedIterations(matrix.getLockedIterations());
						newMatrices.add(newMatrix);
					}
					rotation = rotation + magnitude;
				}
				break;
		    }
		    case 2: {
		    	int x = startingPoint;
				if (x > 0) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int newx = 0; newx < x; newx++) {
						for (int y = 0; y <= matrix.getMaxY(); y++) {
							for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(newx, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, newx, y, z);
							}
						}
					}
					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, matrix.getStartTime()));
					}
				}
				int pitch = 0;
				while (x <= matrix.getMaxX()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int i = 0; i < length; i++) {
						for (int y = 0; y <= matrix.getMaxY(); y++) {
							for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						x++;
					}

					if (newMatrix.hasParticles()) {
						newMatrix.setSpacing(matrix.getSpacing());
						newMatrix.setAddedPitch(matrix.getAddedPitch() + pitch);
						newMatrix.setAddedRotation(matrix.getAddedRotation());
						newMatrix.setAddedTilt(matrix.getAddedTilt());
						newMatrix.setIfPitch(matrix.hasPitch());
						newMatrix.setPlayerLeft(matrix.getPlayerLeft());
						newMatrix.setPlayerUp(matrix.getPlayerUp());
						newMatrix.setStartTime(matrix.getStartTime());
						newMatrix.setRepeatTime(matrix.getRepeatTime());
						newMatrix.setLockedIterations(matrix.getLockedIterations());
						newMatrices.add(newMatrix);
					}
					pitch = pitch + magnitude;
				}
				break;
		    }
		    case 3: {
		    	int z = startingPoint;
				if (z > matrix.getMinZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int newz = matrix.getMinZ(); newz < z; newz++) {
						for (int y = 0; y <= matrix.getMaxY(); y++) {
							for (int x = 0; x <= matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, newz);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, newz);
							}
						}
					}
					if (newMatrix.hasParticles()) {
						newMatrices.add(setDefaults(matrix, newMatrix, matrix.getStartTime()));
					}
				}
				int tilt = 0;
				while (z <= matrix.getMaxZ()) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int i = 0; i < length; i++) {
						for (int y = 0; y <= matrix.getMaxY(); y++) {
							for (int x = 0; x <= matrix.getMaxX(); x++) {
								PowderParticle powderParticle = 
										matrix.getParticleAtLocation(x, y, z);
								if (powderParticle == null || powderParticle.getParticle() == null) {
									continue;
								}
								newMatrix.putPowderParticle(powderParticle, x, y, z);
							}
						}
						z++;
					}

					if (newMatrix.hasParticles()) {
						newMatrix.setSpacing(matrix.getSpacing());
						newMatrix.setAddedPitch(matrix.getAddedPitch());
						newMatrix.setAddedRotation(matrix.getAddedRotation());
						newMatrix.setAddedTilt(matrix.getAddedTilt() + tilt);
						newMatrix.setIfPitch(matrix.hasPitch());
						newMatrix.setPlayerLeft(matrix.getPlayerLeft());
						newMatrix.setPlayerUp(matrix.getPlayerUp());
						newMatrix.setStartTime(matrix.getStartTime());
						newMatrix.setRepeatTime(matrix.getRepeatTime());
						newMatrix.setLockedIterations(matrix.getLockedIterations());
						newMatrices.add(newMatrix);
					}
					tilt = tilt + magnitude;
				}
				break;
		    }
		    default: {
		    	newMatrices.add(matrix);
		    }
		}
		return newMatrices;
	}

	public static ParticleMatrix setFlash(ParticleMatrix matrix, int r, int g, int b, int flash) {
		ParticleMatrix newMatrix = new ParticleMatrix();
		for (int x = 0; x <= matrix.getMaxX(); x++) {
			for (int y = 0; y <= matrix.getMaxY(); y++) {
				for (int z = matrix.getMinZ(); z <= matrix.getMaxZ(); z++) {
					PowderParticle powderParticle = 
							matrix.getParticleAtLocation(x, y, z);
					if (powderParticle == null || 
							powderParticle.getParticle() != Particle.REDSTONE) {
						continue;
					}
					PowderParticle newParticle = powderParticle.clone();
					newParticle.setXOff((newParticle.getXOff() + r) % 255);
					newParticle.setYOff((newParticle.getYOff() + g) % 255);
					newParticle.setZOff((newParticle.getZOff() + b) % 255);
					newMatrix.putPowderParticle(newParticle, x, y, z);
				}
			}
		}
		setDefaults(matrix, newMatrix, matrix.getStartTime());
		matrix.setStartTime(matrix.getStartTime() + flash);
		return newMatrix;
	}

}
