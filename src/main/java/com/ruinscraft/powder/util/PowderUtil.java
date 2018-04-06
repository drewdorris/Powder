package com.ruinscraft.powder.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderCommand;
import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.Dust;
import com.ruinscraft.powder.models.Layer;
import com.ruinscraft.powder.models.ParticleMatrix;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderParticle;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.SoundEffect;

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

		/*/
		if (powder.isRepeating()) {

			int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
				public void run() {
					powderTask.addTasks(PowderUtil.spawnParticles(player, powder));
					powderTask.addTasks(PowderUtil.spawnSounds(player, powder));
				}
			}, 0L, powder.getDelay());

			powderTask.addTasks(PowderUtil.spawnDusts(player, powder));

			powderTask.addTask(task);

		} else {
			powderTask.addTasks(PowderUtil.spawnParticles(player, powder));
			powderTask.addTasks(PowderUtil.spawnSounds(player, powder));
			powderTask.addTasks(PowderUtil.spawnDusts(player, powder));
		}
		*/

		plugin.getPowderHandler().addPowderTask(powderTask);

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

	// spawn particle matrices, returns list of taskIDs
	public static Set<Integer> spawnParticles(final Player player, final Powder powder) {
		List<ParticleMatrix> particleMatrices = powder.getMatrices();

		Set<Integer> tasks = new HashSet<Integer>();

		// separate spacing, wait time for each ParticleMatrix
		for (ParticleMatrix particleMatrix : particleMatrices) {
			final float spacing;

			// set particleMatrix spacing to the default if it doesn't exist or is 0
			if (particleMatrix.getSpacing() == null || particleMatrix.getSpacing() == 0) {
				spacing = powder.getDefaultSpacing();
			} else {
				spacing = particleMatrix.getSpacing();
			}

			long waitTime = particleMatrix.getTick();

			int task = plugin.getServer()
					.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override
						public void run() {

							if (!player.isOnline()) {
								return;
							}

							// base player location is the eye location
							Location location = Bukkit.getPlayer(player.getName()).getEyeLocation();

							// get rotation (left/right) and pitch (up/down) of the player
							final double playerRotation = ((player.getLocation().getYaw()) % 360) * (Math.PI / 180);
							final double playerPitch;

							// distance between each row up/down
							final double distanceBetweenRowsY;
							// this would not do anything if the Powder has pitch movement disabled, since it's directly up/down
							final double distanceBetweenRowsXZ;

							// if the Powder moves up/down with the head, set some values that will allow that to happen
							// else, make them do nothing significant
							if (powder.hasPitch()) {
								playerPitch = (player.getLocation().getPitch() * (Math.PI / 180));
								distanceBetweenRowsY = ((Math.sin((Math.PI / 2) - playerPitch)) * spacing);
								distanceBetweenRowsXZ = ((Math.cos((Math.PI / 2) + playerPitch)) * spacing);
							} else {
								playerPitch = 0;
								distanceBetweenRowsY = spacing;
								distanceBetweenRowsXZ = 0;
							}

							// where to start in relation to the player/location
							int left;
							int up;
							if (particleMatrix.getPlayerLeft() == 0 && particleMatrix.getPlayerUp() == 0) {
								left = powder.getDefaultLeft();
								up = powder.getDefaultUp();
							} else {
								left = particleMatrix.getPlayerLeft();
								up = particleMatrix.getPlayerUp();
							}

							// amount to add between each individual particle
							final double amountToAddX = getDirLengthX(playerRotation, spacing);
							final double amountToAddZ = getDirLengthZ(playerRotation, spacing);
							// how far front/back the layer is
							final double startARowX = getDirLengthX(playerRotation + (Math.PI / 2), spacing);
							final double startARowZ = getDirLengthZ(playerRotation + (Math.PI / 2), spacing);
							// if pitch is enabled, this will adjust the x & z for each row, since the Powder isn't top-down
							final double moveWithPitchX = getDirLengthX(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
							final double moveWithPitchZ = getDirLengthZ(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
							// don't remember what this does
							final double moveBackWithPitchX = getDirLengthX(playerRotation, distanceBetweenRowsXZ);
							final double moveBackWithPitchY = (Math.sin(0 - playerPitch) * spacing);
							final double moveBackWithPitchZ = getDirLengthZ(playerRotation, distanceBetweenRowsXZ);

							// starts the Powder in relation to the given left & up
							final double startX = location.getX() - 
									(amountToAddX * left) + (moveWithPitchX * up);
							final double startY = location.getY() + (distanceBetweenRowsY * up);
							final double startZ = location.getZ() - 
									(amountToAddZ * left) + (moveWithPitchZ * up);

							// start off
							double newX = startX;
							double newY = startY;
							double newZ = startZ;

							for (Layer layer : particleMatrix.getLayers()) {

								// sets the position in relation to the layer's front/back position
								float position = layer.getPosition();
								newX = startX + (startARowX * position) + (moveBackWithPitchZ * position);
								newY = startY + (moveBackWithPitchY * position);
								newZ = startZ + (startARowZ * position) + (moveBackWithPitchX * position);

								// rowsDownSoFar is how many rows that have been processed
								int rowsDownSoFar = 0;
								for (List<PowderParticle> row : layer.getRows()) {

									for (PowderParticle powderParticle : row) {

										// add the amount per particle given
										newX = newX + amountToAddX;
										newZ = newZ + amountToAddZ;

										if (powderParticle.getParticle() == null) {
											continue;
										}

										// spawn the particle
										if (powderParticle.getData() == null) {
											if (powderParticle.getXOff() == null) {
												player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
														powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
														powderParticle.getZOff() / 255, 1);
												continue;
											}
											player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 0, 
													powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
													powderParticle.getZOff() / 255, 1);
										} else {
											player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
													powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
													powderParticle.getZOff() / 255, (double) powderParticle.getData());
										}

									}

									// move the row down for the next row
									rowsDownSoFar++;
									newX = startX + (startARowX * position) - (moveWithPitchX * rowsDownSoFar);
									newY = newY - distanceBetweenRowsY;
									newZ = startZ + (startARowZ * position) - (moveWithPitchZ * rowsDownSoFar);
								}
							}
						}
						// begin the task at the tick given
					},waitTime);

			tasks.add(task);
		}

		return tasks;
	}

	// spawns SoundEffects, returns list of taskIDs
	public static Set<Integer> spawnSounds(final Player player, final Powder powder) {
		Set<Integer> tasks = new HashSet<Integer>();

		for (SoundEffect sound : powder.getSoundEffects()) {

			int task = plugin.getServer().getScheduler()
					.scheduleSyncDelayedTask(plugin, new Runnable() {

						@Override
						public void run() {

							player.getWorld().playSound(player.getLocation(), sound.getSound(),
									sound.getVolume(), sound.getPitch());

						}

					},((long) sound.getWaitTime()));

			tasks.add(task);

		}

		return tasks;
	}

	// spawns Dusts, returns list of taskIDs
	public static Set<Integer> spawnDusts(final Player player, final Powder powder) {
		Set<Integer> tasks = new HashSet<Integer>();

		for (Dust dust : powder.getDusts()) {

			// frequency is particles per min
			// translate to ticks if repeating
			long frequency;

			int task;
			if (dust.isSingleOccurrence()) {
				frequency = dust.getFrequency();
				task = plugin.getServer().getScheduler()
						.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								spawnDust(player, dust);
							}
						}, frequency);
			} else {
				if (dust.getFrequency() == 0) {
					frequency = 0;
				} else {
					frequency = 1200 / dust.getFrequency();
				}
				task = plugin.getServer().getScheduler()
						.scheduleSyncRepeatingTask(plugin, new Runnable() {
							public void run() {
								spawnDust(player, dust);
							}
						}, frequency, frequency);
			}
			tasks.add(task);

		}

		return tasks;
	}

	// spawns a given Dust
	public static void spawnDust(final Player player, final Dust dust) {
		double radiusZoneX = (Math.random() - .5) * (2 * dust.getRadius());
		double radiusZoneZ = (Math.random() - .5) * (2 * dust.getRadius());
		double heightZone = (Math.random() - .5) * (2 * dust.getHeight());
		Location particleLocation = player.getLocation().add(radiusZoneX, heightZone + 1, radiusZoneZ);
		// if no block in the way
		if (particleLocation.getBlock().isEmpty()) {
			PowderParticle powderParticle = dust.getPowderParticle();
			if (powderParticle.getData() == null) {
				player.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 0, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 1);
			} else {
				player.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 1, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 
						(double) powderParticle.getData());
			}
		}
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
