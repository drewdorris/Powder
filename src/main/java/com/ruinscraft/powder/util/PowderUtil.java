package com.ruinscraft.powder.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderCommand;
import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderUtil {

	private static PowderPlugin plugin = PowderPlugin.getInstance();

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static Set<UUID> getOnlineUUIDs() {
		return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
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
		TextComponent prefix = new TextComponent(PowderPlugin.PREFIX);
		prefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label).color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		prefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/" + label ) );
		fullMessage.addExtra(prefix);
		fullMessage.addExtra((TextComponent) message);

		player.spigot().sendMessage(fullMessage);
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

	// cosine of the given rotation, and multiplies it by the given spacing
	public static double getDirLengthX(double rot, double spacing) {
		return (spacing * Math.cos(rot));
	}

	// sine of the given rotation, and multiplies it by the given spacing
	public static double getDirLengthZ(double rot, double spacing) {
		return (spacing * Math.sin(rot));
	}

	// spawns a given Powder for the given user
	public static void spawnPowder(final Player player, final Powder powder) {
		// create a PowderTask, add taskIDs to it
		List<PowderElement> elements = new ArrayList<PowderElement>();
		elements.addAll(powder.getMatrices());
		elements.addAll(powder.getDusts());
		elements.addAll(powder.getSoundEffects());
		PowderTask powderTask = new PowderTask(player.getUniqueId(), powder);
		for (PowderElement element : elements) {
			powderTask.addElement(element);
		}
		if (plugin.getPowderHandler().getPowderTasks().isEmpty()) {
			plugin.getPowderHandler().addPowderTask(powderTask);
			new PowdersCreationTask().runTaskTimer(plugin, 0L, 1L);
		} else {
			plugin.getPowderHandler().addPowderTask(powderTask);
		}

		savePowdersForPlayer(player.getUniqueId());
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
			if (powderTask.getPowder().isRepeating() || !powderTask.getPowder().getDusts().isEmpty()) {
				enabledPowders.add(powderTask.getPowder().getName());
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

		if (!PowderCommand.hasPermission(player, powder)) {
			return;
		}

		PowderUtil.spawnPowder(player, powder);
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
