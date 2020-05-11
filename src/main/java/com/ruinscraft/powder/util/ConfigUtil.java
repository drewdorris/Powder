package com.ruinscraft.powder.util;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.Sound;
import com.ruinscraft.powder.model.*;
import com.ruinscraft.powder.model.particle.ModelPowderParticle;
import com.ruinscraft.powder.model.particle.ParticleName;
import com.ruinscraft.powder.model.particle.PositionedPowderParticle;
import com.ruinscraft.powder.model.particle.PowderParticle;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import org.bukkit.*;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ConfigUtil {

	private static final String PLAYER_DATA_FILE = "playerdata.yml";

	public static FileConfiguration loadConfig() {
		FileConfiguration config = null;
		PowderPlugin instance = PowderPlugin.get();
		File configFile = new File(instance.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			PowderPlugin.info("config.yml not found, creating!");
			instance.saveDefaultConfig();
		}
		instance.reloadConfig();
		config = instance.getConfig();
		instance.setConfigVersion(config.getInt("configVersion", 0));
		instance.setFastMode(config.getBoolean("fastMode", false));
		instance.setAsyncMode(config.getBoolean("asyncMode", false));
		instance.setMaxCreatedPowders(config.getInt("maxCreated", 100));
		checkConfigVersion();
		return config;
	}

	public static boolean checkConfigVersion() {
		int configVersion = PowderPlugin.get().getConfigVersion();
		int currentConfigVersion = 2;
		int versionsBehind = currentConfigVersion - configVersion;
		if (versionsBehind == 1) {
			PowderPlugin.warning("Your config version is out of date! You are " +
					"1 version behind. Please obtain a new version of the config" +
					" to get the latest functionality.");
			return false;
		} else if (versionsBehind > 1) {
			PowderPlugin.warning("Your config version is out of date! You are " +
					versionsBehind + " versions behind. Please obtain a " +
					"new version of the config to get the latest functionality.");
			return false;
		} else {
			return true;
		}
	}

	public static List<FileConfiguration> loadPowderConfigs() {
		// list of configuration files that contain Powders
		List<FileConfiguration> powderConfigs = new ArrayList<>();

		FileConfiguration config = PowderPlugin.get().getConfig();
		File dataFolder = PowderPlugin.get().getDataFolder();

		for (String urlName : config.getStringList("powderSources")) {
			FileConfiguration powderConfig;
			URL url = PowderUtil.readURL(urlName);
			File file;
			// if a file is from a path, load from within data folder
			if (!urlName.contains("/")) {
				file = new File(dataFolder, urlName);
				if (!file.exists()) {
					if (urlName.equals("powders.yml")) {
						continue;
					}
					PowderPlugin.warning("Failed to load config file '" + urlName + "'.");
					continue;
				}
				powderConfig = YamlConfiguration.loadConfiguration(file);
				// else, load from URL
			} else if (url != null) {
				InputStream stream = PowderUtil.getInputStreamFromURL(url);

				if (stream == null) {
					continue;
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				powderConfig = YamlConfiguration.loadConfiguration(reader);

			} else {
				PowderPlugin.warning("Failed to load config file '" + urlName + "'.");
				continue;
			}

			powderConfigs.add(powderConfig);
		}

		// if powders.yml is listed as a source but doesn't exist, create it
		File defaultPowderConfig = new File(dataFolder, "powders.yml");
		if (!defaultPowderConfig.exists() &&
				config.getStringList("powderSources").contains("powders.yml")) {
			PowderPlugin.info("powders.yml not found and listed as a source, creating!");
			PowderPlugin.get().saveResource("powders.yml", false);
			FileConfiguration powderConfig =
					YamlConfiguration.loadConfiguration(defaultPowderConfig);
			powderConfigs.add(powderConfig);
		}
		return powderConfigs;
	}

	public static void reloadCategories() {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();
		FileConfiguration config = PowderPlugin.get().getConfig();
		if (powderHandler.categoriesEnabled()) {
			for (String s : config.getConfigurationSection("categories").getKeys(false)) {
				powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
			}
			if (!powderHandler.getCategories().keySet().contains("Other")) {
				powderHandler.addCategory("Other", "Unsorted Powders");
			}
		}
	}

	public static boolean configContainsPowder(FileConfiguration config, String path) {
		if (config.getString("powders." + path + ".name") != null) {
			return true;
		}
		return false;
	}

	public static Powder loadPowderFromConfig(String path) {
		for (FileConfiguration config : PowderPlugin.get().getPowderConfigs()) {
			if (configContainsPowder(config, path)) {
				return loadPowderFromConfig(config, path);
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Powder loadPowderShellFromConfig(FileConfiguration powderConfig, String path) {
		Powder powder = new Powder(path);

		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		String section = "powders." + path;

		// set some given values if they exist, default value if they don't
		powder.setName(powderConfig.getString(section + ".name", null));
		powder.setHidden(
				powderConfig.getBoolean(section + ".hidden", false));

		// add categories if enabled
		if (powderHandler.categoriesEnabled()) {
			for (String t : (List<String>) powderConfig
					.getList(section + ".categories", new ArrayList<String>())) {
				if (!(powderHandler.getCategories().keySet().contains(t))) {
					PowderPlugin.warning("Invalid category '" + t +
							"' for '" + powder.getName() + "' in " + powderConfig.getName());
					continue;
				}
				powder.addCategory(t);
			}
			if (powder.getCategories().isEmpty()) {
				powder.addCategory("Other");
			}
		}

		return powder;
	}

	@SuppressWarnings("unchecked")
	public static Powder loadPowderFromConfig(FileConfiguration powderConfig, String path) {
		Powder powder = new Powder(path);

		String section = "powders." + path;

		// set some given values if they exist, default value if they don't
		powder.setName(powderConfig.getString(section + ".name", null));
		powder.setDefaultSpacing(
				powderConfig.getDouble(section + ".defaultSpacing", .1));
		powder.setDefaultStartTime(
				powderConfig.getInt(section + ".defaultStartTime", 0));
		powder.setDefaultRepeatTime(
				powderConfig.getInt(section + ".defaultRepeatTime", 0));
		powder.setDefaultLockedIterations(
				powderConfig.getInt(section + ".defaultIterations", 1));
		powder.setDefaultAddedPitch(
				powderConfig.getDouble(section + ".defaultAddedPitch", 0));
		powder.setDefaultAddedRotation(
				powderConfig.getDouble(section + ".defaultAddedRotation", 0));
		powder.setDefaultAddedTilt(
				powderConfig.getDouble(section + ".defaultAddedTilt", 0));
		powder.setHidden(
				powderConfig.getBoolean(section + ".hidden", false));

		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();
		// add categories if enabled
		if (powderHandler.categoriesEnabled()) {
			for (String t : (List<String>) powderConfig
					.getList(section + ".categories", new ArrayList<String>())) {
				if (!(powderHandler.getCategories().keySet().contains(t))) {
					PowderPlugin.warning("Invalid category '" + t +
							"' for '" + powder.getName() + "' in " + powderConfig.getName());
					continue;
				}
				powder.addCategory(t);
			}
			if (powder.getCategories().isEmpty()) {
				powder.addCategory("Other");
			}
		}

		if (powderConfig.getConfigurationSection(section + ".songs") != null) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".songs").getKeys(false)) {
				String eachSection = section + ".songs." + ss;
				String fileName = powderConfig
						.getString(eachSection + ".fileName", "unknownfile.nbs");
				double volume = powderConfig
						.getDouble(eachSection + ".volume", 1);
				double multiplier = powderConfig
						.getDouble(eachSection + ".multiplier", 1);
				boolean surroundSound = powderConfig
						.getBoolean(eachSection + ".surroundSound", true);
				int transpose = powderConfig.getInt(eachSection + ".transpose", 0);
				boolean limitNotes = powderConfig.getBoolean(eachSection + ".limitNotes", true);
				int volumeMultiplier = powderConfig.getInt(eachSection + ".volumeMultiplier", 1);
				List<SoundEffect> songSoundEffects =
						SoundUtil.getSoundEffectsFromNBS(fileName, volume,
								multiplier, surroundSound, transpose, limitNotes, volumeMultiplier,
								getStart(powderConfig, powder, eachSection),
								getRepeat(powderConfig, powder, eachSection),
								getIterations(powderConfig, powder, eachSection));
				powder.addPowderElements(songSoundEffects);
			}
		}

		if (powderConfig.getConfigurationSection(section + ".sounds") != null) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".sounds").getKeys(false)) {
				String eachSection = section + ".sounds." + ss;
				String soundEnum = powderConfig.getString(eachSection + ".soundEnum",
						"BLOCK_NOTE_CHIME");
				org.bukkit.Sound bukkitSound = Sound.getFromBukkitName(soundEnum);
				double volume = powderConfig.getDouble(eachSection + ".volume", 1);
				float soundPitch = (float) powderConfig.getDouble(eachSection + ".note", 1);
				soundPitch = (float) Math.pow(2.0, ((double) soundPitch - 12.0) / 12.0);
				boolean surroundSound = powderConfig.getBoolean(
						eachSection + ".surroundSound", true);
				powder.addPowderElement(new SoundEffect(bukkitSound, volume, soundPitch, surroundSound,
						getStart(powderConfig, powder, eachSection),
						getRepeat(powderConfig, powder, eachSection),
						getIterations(powderConfig, powder, eachSection)));
			}
		}

		if (powderConfig.getConfigurationSection(section + ".changes") != null) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".changes").getKeys(false)) {
				String eachSection = section + ".changes." + ss;
				String particleChar = powderConfig.getString(
						eachSection + ".particleChar", "A");
				char character = particleChar.charAt(0);
				String particleEnum = powderConfig.getString(
						eachSection + ".particleEnum", "HEART");
				Particle particle = Particle.valueOf(particleEnum);
				int amount = powderConfig.getInt(eachSection + ".amount", 1);
				double xOffset = powderConfig.getDouble(eachSection + ".xOffset", 0);
				double yOffset = powderConfig.getDouble(eachSection + ".yOffset", 0);
				double zOffset = powderConfig.getDouble(eachSection + ".zOffset", 0);
				Object data = null;

				int note = powderConfig.getInt(eachSection + ".note");
				if (powderConfig.getString(eachSection + ".note") != null) {
					note = note * 255 / 24;
					if (note > 255) note -= (255 - 10.625);
					xOffset = note;
					yOffset = 0;
					zOffset = 0;
					data = 1D;
					amount = 0;
					particle = Particle.NOTE;
				}

				if (particle == Particle.REDSTONE && PowderPlugin.is1_13()) {
					data = new DustOptions(Color.fromRGB(
							(int) xOffset,
							(int) yOffset,
							(int) zOffset), 1F);
				} else if (particle == Particle.NOTE && PowderPlugin.is1_13()) {
					xOffset = xOffset * 255 / 10.625 / 24;
					if (xOffset > (255)) {
						xOffset -= (255);
					}
					data = 1D;
				} else {
					data = (Void) null;
				}

				powder.addPowderParticle(new ModelPowderParticle(character, particle,
						amount, xOffset, yOffset, zOffset, data));
			}
		}

		if (powderConfig.getConfigurationSection(section + ".dusts") != null) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".dusts").getKeys(false)) {
				String eachSection = section + ".dusts." + ss;
				String dustName = powderConfig.getString(eachSection + ".particleChar", "null");
				char character = dustName.charAt(0);
				PowderParticle powderParticle = powder.getPowderParticle(character);
				if (powderParticle == null) {
					try {
						Particle particle = Particle.valueOf(
								ParticleName.valueOf(dustName).getName());
						powderParticle = new ModelPowderParticle(character, particle);
					} catch (Exception e) {
						continue;
					}
				}
				double radius = powderConfig.getDouble(eachSection + ".radius", 1);
				double height = powderConfig.getDouble(eachSection + ".height", 1);
				double span = powderConfig.getDouble(eachSection + ".span", 1);
				List<PowderElement> addedPowderElements = new ArrayList<>();
				if (powderConfig.getBoolean(eachSection + ".attachToNote")) {
					String noteName = powderConfig.getString(eachSection + ".attachedToNote",
							"BLOCK_NOTE_HARP");
					noteName = com.xxmicloxx.NoteBlockAPI.model.Sound.getFromBukkitName(noteName).name();
					for (PowderElement powderElement : powder.getPowderElements()) {
						if (powderElement instanceof SoundEffect) {
							SoundEffect soundEffect = (SoundEffect) powderElement;
							if (soundEffect.getSound().name().equals(noteName)) {
								addedPowderElements.add(new Dust(powderParticle, radius, height, span,
										soundEffect.getStartTime(), soundEffect.getRepeatTime(),
										soundEffect.getLockedIterations()));
							}
						}
					}
					powder.addPowderElements(addedPowderElements);
					continue;
				}
				powder.addPowderElement(new Dust(powderParticle, radius, height, span,
						getStart(powderConfig, powder, eachSection),
						getRepeat(powderConfig, powder, eachSection),
						getIterations(powderConfig, powder, eachSection)));
			}
		}

		if (powderConfig.getConfigurationSection(section + ".matrices") != null) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".matrices").getKeys(false)) {
				String eachSection = section + ".matrices." + ss;
				boolean containsPlayer = false;
				ParticleMatrix particleMatrix = new ParticleMatrix();
				particleMatrix.setSpacing(powderConfig.getDouble(
						eachSection + ".spacing", powder.getDefaultSpacing()));
				particleMatrix.setIfPitch(powderConfig.getBoolean(
						eachSection + ".hasPitch", false));
				particleMatrix.setAddedPitch(powderConfig.getDouble(
						eachSection + ".addedPitch", 0));
				particleMatrix.setAddedRotation(powderConfig.getDouble(
						eachSection + ".addedRotation", 0));
				particleMatrix.setAddedTilt(powderConfig.getDouble(
						eachSection + ".addedTilt", 0));
				particleMatrix.setStartTime(
						getStart(powderConfig, powder, eachSection));
				particleMatrix.setRepeatTime(
						getRepeat(powderConfig, powder, eachSection));
				particleMatrix.setLockedIterations(
						getIterations(powderConfig, powder, eachSection));
				int left = 0;
				int up = 0;
				for (String sss : powderConfig
						.getConfigurationSection(eachSection + ".layers").getKeys(false)) {
					String eachEachSection = eachSection + ".layers." + sss;
					int z = powderConfig.getInt(eachEachSection + ".position", 0);
					List<String> layerMatrix = (List<String>) powderConfig
							.getList(eachEachSection + ".layerMatrix", new ArrayList<String>());
					for (int index = 0; index < layerMatrix.size(); index++) {
						String ssss = layerMatrix.get(index);
						if (ssss.contains(":")) {
							if (ssss.contains("img:")) {
								String urlName;
								int width;
								int height;
								int xAdd;
								ssss = ssss.replace("img:", "");
								urlName = ssss.substring(0, ssss.indexOf(";"));
								ssss = ssss.substring(ssss.indexOf(";") + 1, ssss.length());
								width = Integer.valueOf(ssss.substring(0, ssss.indexOf(";")));
								ssss = ssss.substring(ssss.indexOf(";") + 1, ssss.length());
								if (ssss.contains(";")) {
									height = Integer.valueOf(ssss.substring(0, ssss.indexOf(";")));
									ssss = ssss.substring(ssss.indexOf(";") + 1, ssss.length());
									xAdd = Integer.valueOf(ssss);
								} else {
									height = Integer.valueOf(ssss);
									xAdd = 0;
								}
								try {
									particleMatrix.addParticles(
											ImageUtil.getRows(urlName, z, index, xAdd, width, height));
								} catch (IOException io) {
									PowderPlugin.warning("Failed to load image: '" + urlName + "'");
									continue;
								}
								// add height to compensate for dist. from location
								// (might not necessarily correspond w/ actual image)
								up = up + height;
							}
							continue;
						}
						// if the Layer is in the same position as where the location/player is
						if (z == 0) {
							up++;
							// if the string contains location/player
							if (ssss.contains("?")) {
								containsPlayer = true;
								// set the left & up of the Layer
								// so that createPowders() knows where to start
								left = (ssss.indexOf("?")) + 1;
								// set default if it's the matrix spawned immediately
								if (particleMatrix.getStartTime() == 0) {
									powder.setDefaultLeft(left - 1);
									powder.setDefaultUp(up + 1);
								}
								particleMatrix.setPlayerLeft(left - 1);
								particleMatrix.setPlayerUp(up + 1);
							}
						}
						for (int x = 0; x < ssss.toCharArray().length; x++) {
							char character = ssss.toCharArray()[x];
							PositionedPowderParticle powderParticle;
							PowderParticle model = powder.getPowderParticle(character);
							if (model == null) {
								try {
									String string = String.valueOf(character);
									Particle particle = Particle.valueOf(
											ParticleName.valueOf(string).getName());
									Object data = null;
									if (particle == Particle.REDSTONE
											&& PowderPlugin.is1_13()) {
										data = new DustOptions(Color.fromRGB(0, 0, 0), 1F);
									} else {
										data = (Void) data;
									}
									powderParticle = new PositionedPowderParticle(
											character, particle, x, index, z);
								} catch (Exception e) {
									continue;
								}
							} else {
								Object data = null;
								if (model.getParticle() == Particle.REDSTONE
										&& PowderPlugin.is1_13()) {
									data = new DustOptions(Color.fromRGB(
											(int) model.getXOff(),
											(int) model.getYOff(),
											(int) model.getZOff()), 1F);
								} else {
									data = (Void) data;
								}
								powderParticle = new PositionedPowderParticle(
										model, x, index, z);
							}
							particleMatrix.addParticle(powderParticle);
						}
					}
				}
				if (!containsPlayer) {
					particleMatrix.setPlayerLeft(powder.getDefaultLeft());
					particleMatrix.setPlayerUp(powder.getDefaultUp());
				}

				List<ParticleMatrix> matrices = new ArrayList<>();
				if (powderConfig.getInt(eachSection + ".settings.gradient.type", 0) > 0) {
					int gradient = powderConfig.getInt(eachSection + ".settings.gradient.type");
					int tickSpeed = powderConfig.getInt(eachSection + ".settings.gradient.speed", 1);
					int length = powderConfig.getInt(eachSection + ".settings.gradient.length", 1);
					matrices.addAll(
							PowderUtil.setGradients(particleMatrix, gradient, tickSpeed, length));
				} else if (powderConfig.getInt(eachSection + ".settings.twist.type", 0) > 0) {
					int type = powderConfig.getInt(eachSection + ".settings.twist.type", 0);
					int magnitude = powderConfig.getInt(eachSection + ".settings.twist.magnitude", 2);
					int length = powderConfig.getInt(eachSection + ".settings.twist.length", 1);
					int startingPoint = powderConfig.getInt(
							eachSection + ".settings.twist.startingPoint", 0);
					matrices.addAll(
							PowderUtil.setTwist(particleMatrix,
									type, magnitude, length, startingPoint));
				} else {
					matrices.add(particleMatrix);
				}
				int rgbr = powderConfig.getInt(eachSection + ".settings.rgb.r", 0);
				int rgbg = powderConfig.getInt(eachSection + ".settings.rgb.g", 0);
				int rgbb = powderConfig.getInt(eachSection + ".settings.rgb.b", 0);
				if (rgbr + rgbg + rgbb != 0) {
					int size = matrices.size();
					for (int i = 0; i < size; i++) {
						ParticleMatrix matrix = matrices.get(0);
						matrices.add(PowderUtil.setNewRGB(matrix, rgbr, rgbg, rgbb));
						matrices.remove(matrix);
					}
				}
				if (powderConfig.getInt(eachSection + ".settings.flash.flash", 0) != 0) {
					int r = powderConfig.getInt(eachSection + ".settings.flash.r", 0);
					int g = powderConfig.getInt(eachSection + ".settings.flash.g", 0);
					int b = powderConfig.getInt(eachSection + ".settings.flash.b", 0);
					int flash = powderConfig.getInt(eachSection + ".settings.flash.flash", 1);
					int size = matrices.size();
					for (int i = 0; i < size; i++) {
						ParticleMatrix matrix = matrices.get(i);
						matrices.add(PowderUtil.setFlash(matrix, r, g, b, flash));
					}
				}
				powder.addPowderElements(matrices);
			}
		}

		if (powder.getPowderElements().isEmpty()) {
			PowderPlugin.warning("Powder '" +
					powder.getName() + "' appears empty and/or incorrectly formatted.");
			return null;
		}

		return powder;
	}

	private static int getStart(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".startTime", powder.getDefaultStartTime());
	}

	private static int getRepeat(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".repeatTime", powder.getDefaultRepeatTime());
	}

	private static int getIterations(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".iterations", powder.getDefaultLockedIterations());
	}

	public static void saveFile(FileConfiguration config, String fileName) {
		try {
			File file = new File(PowderPlugin.get()
					.getDataFolder(), fileName);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean containsTask(PowderTask powderTask) {
		FileConfiguration config = PowderPlugin.get().getPlayerDataFile();
		if (config == null) {
			return false;
		}
		if (config.getConfigurationSection(powderTask.getTracker().getCreator() + ".stationary."
				+ PowderUtil.cleanPowderTaskName(powderTask)) == null) {
			return false;
		}
		return true;
	}

	public static FileConfiguration loadPlayerDataFile() {
		FileConfiguration config = null;
		PowderPlugin instance = PowderPlugin.get();
		File configFile = new File(instance.getDataFolder(), PLAYER_DATA_FILE);
		if (configFile.exists()) {
			config = YamlConfiguration.loadConfiguration(configFile);
		} else {
			return null;
		}
		if (config == null) {
			return null;
		}
		PowderPlugin.get().setPlayerDataFile(config);
		Set<PowderTask> powderTasks = loadStationaryPowders();
		for (PowderTask powderTask : powderTasks) {
			instance.getPowderHandler().runPowderTask(powderTask);
		}
		return config;
	}

	public static void saveStationaryPowder(
			FileConfiguration playerDataFile, PowderTask powderTask) {
		if (powderTask.getPowder() == null) {
			ConfigUtil.removeStationaryPowder(powderTask);
			return;
		}
		UUID uuid = powderTask.getTracker().getCreator();

		if (powderTask.getTracker().getType() == Tracker.Type.STATIONARY) {
			PowderPlugin instance = PowderPlugin.get();
			if (playerDataFile == null) {
				File file = new File(instance.getDataFolder(), PLAYER_DATA_FILE);
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				playerDataFile = YamlConfiguration.loadConfiguration(file);
				instance.setPlayerDataFile(playerDataFile);
			}
			String path = uuid + ".stationary." + PowderUtil.cleanPowderTaskName(powderTask);
			playerDataFile.set(path + ".name", powderTask.getName());

			Powder powder = powderTask.getPowder();
			Tracker tracker = powderTask.getTracker();
			StationaryTracker stationaryTracker = (StationaryTracker) tracker;

			playerDataFile.set(path + ".powder", powder.getName());
			playerDataFile.set(path + ".creator", stationaryTracker.getCreator().toString());
			playerDataFile.set(path + ".loop", powder.isLooping());
			path = path + ".location";
			Location location = stationaryTracker.getCurrentLocation().clone();
			playerDataFile.set(path + ".world", location.getWorld().getName());
			playerDataFile.set(path + ".x", location.getX());
			playerDataFile.set(path + ".y", location.getY());
			playerDataFile.set(path + ".z", location.getZ());
			playerDataFile.set(path + ".pitch", location.getPitch());
			playerDataFile.set(path + ".yaw", location.getYaw());

			saveFile(playerDataFile, PLAYER_DATA_FILE);
		}
	}

	public static Set<PowderTask> loadStationaryPowders() {
		Set<PowderTask> powderTasks = new HashSet<>();
		FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();
		if (playerDataFile == null) {
			return powderTasks;
		}
		for (String uuid : playerDataFile.getKeys(false)) {
			if (playerDataFile.getConfigurationSection(uuid + ".stationary") == null) continue;
			for (String task : playerDataFile.getConfigurationSection(uuid + ".stationary").getKeys(false)) {
				PowderTask powderTask = loadStationaryPowder(playerDataFile, uuid + ".stationary." + task);
				if (powderTask == null) continue;
				powderTasks.add(powderTask);
			}
		}
		return powderTasks;
	}

	// fix this eventually!!
	public static PowderTask loadStationaryPowder(FileConfiguration config, String section) {
		String newSection = section;
		if (newSection.contains(".name")) {
			return null;
		}
		String powderName = config.getString(newSection + ".powder");
		String uuidString = config.getString(newSection + ".creator");
		System.out.println(powderName + " " + uuidString);
		UUID uuid = UUID.fromString(uuidString);
		if (uuid == null) {
			PowderPlugin.warning("Unknown creator of Powder '" +
					powderName + "' in playerdata.yml");
			return null;
		}
		Powder powder = PowderPlugin.get().getPowderHandler().getPowder(powderName);
		if (powder == null) {
			PowderPlugin.warning("Unknown Powder '" +
					powderName + "' in playerdata.yml");
			return null;
		}
		boolean loop = config.getBoolean(newSection + ".loop", true);
		if (loop) {
			powder = powder.loop();
		}
		newSection = newSection + ".location";
		String worldName = config.getString(newSection + ".world");
		World world = Bukkit.getWorld(worldName);
		if (world == null) {
			PowderPlugin.warning("Unknown World '" +
					worldName + "' in playerdata.yml");
			return null;
		}
		double x = config.getDouble(newSection + ".x");
		double y = config.getDouble(newSection + ".y");
		double z = config.getDouble(newSection + ".z");
		float yaw = (float) config.getDouble(newSection + ".yaw");
		float pitch = (float) config.getDouble(newSection + ".pitch");
		Location location = new Location(world, x, y, z, yaw, pitch);

		PowderTask powderTask = new PowderTask(config.getString(section + ".name"), 
				powder, new StationaryTracker(location, uuid));

		return powderTask;
	}

	public static void removeStationaryPowder(PowderTask powderTask) {
		if (powderTask.getTracker().getType() == Tracker.Type.STATIONARY) {
			FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();
			PowderPlugin.get().getPlayerDataFile()
			.set(powderTask.getTracker().getCreator() + ".stationary." + PowderUtil.cleanPowderTaskName(powderTask), null);
			saveFile(playerDataFile, PLAYER_DATA_FILE);
			powderTask.cancel();
		}
	}

	public static int getAmountCreated(UUID creator) {
		return getAmountAttached(creator) + getAmountStationed(creator);
	}

	public static int getAmountAttached(UUID creator) {
		FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();
		for (String uuid : playerDataFile.getKeys(false)) {
			if (!uuid.equals(creator.toString())) continue;
			if (playerDataFile.getConfigurationSection(uuid + ".attached") == null) return 0;
			return playerDataFile.getConfigurationSection(uuid + ".attached").getKeys(false).size();
		}
		return 0;
	}

	public static int getAmountStationed(UUID creator) {
		FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();
		for (String uuid : playerDataFile.getKeys(false)) {
			if (!uuid.equals(creator.toString())) continue;
			if (playerDataFile.getConfigurationSection(uuid + ".stationary") == null) return 0;
			return playerDataFile.getConfigurationSection(uuid + ".stationary").getKeys(false).size();
		}
		return 0;
	}

	public static void unsetArrowTrail(UUID creator) {
		FileConfiguration playerdata = PowderPlugin.get().getPlayerDataFile();
		if (playerdata == null) return;
		playerdata.set(creator + ".arrowTrail", null);

		saveFile(playerdata, PLAYER_DATA_FILE);
	}

	public static void saveArrowTrail(UUID creator, Powder powder) {
		FileConfiguration playerdata = PowderPlugin.get().getPlayerDataFile();
		if (playerdata == null) return;
		playerdata.set(creator + ".arrowTrail.powder", powder.getName());
		playerdata.set(creator + ".arrowTrail.loop", powder.isLooping());

		saveFile(playerdata, PLAYER_DATA_FILE);
	}

	public static PowderTask loadArrowTrail(Projectile projectile, UUID creator) {
		String powderName = PowderPlugin.get().getPlayerDataFile().getString("" + creator + ".arrowTrail.powder");
		if (powderName == null) return null;
		boolean loop = PowderPlugin.get().getPlayerDataFile().getBoolean("" + creator + ".arrowTrail.loop");

		PowderHandler handler = PowderPlugin.get().getPowderHandler();
		Powder powder = handler.getPowder(powderName);
		if (powder == null) return null;
		if (loop) powder = powder.loop();
		powder = powder.arrowFadeout(20 * 12);

		return new PowderTask("arrow-" + PowderUtil.generateID(5), powder, 
				new EntityTracker(projectile, creator));
	}

	public static void unsetArrowHit(UUID creator) {
		FileConfiguration playerdata = PowderPlugin.get().getPlayerDataFile();
		if (playerdata == null) return;
		playerdata.set(creator + ".arrowHit", null);

		saveFile(playerdata, PLAYER_DATA_FILE);
	}

	public static void saveArrowHit(UUID creator, Powder powder) {
		FileConfiguration playerdata = PowderPlugin.get().getPlayerDataFile();
		if (playerdata == null) return;
		playerdata.set(creator + ".arrowHit.powder", powder.getName());
		playerdata.set(creator + ".arrowHit.loop", powder.isLooping());

		saveFile(playerdata, PLAYER_DATA_FILE);
	}

	public static PowderTask loadArrowHit(Entity hitEntity, UUID creator) {
		String powderName = PowderPlugin.get().getPlayerDataFile().getString("" + creator + ".arrowHit.powder");
		if (powderName == null) return null;
		boolean loop = PowderPlugin.get().getPlayerDataFile().getBoolean("" + creator + ".arrowHit.loop");

		PowderHandler handler = PowderPlugin.get().getPowderHandler();
		Powder powder = handler.getPowder(powderName);
		if (powder == null) return null;
		if (loop) powder = powder.loop();
		powder = powder.arrowFadeout(20 * 5);

		return new PowderTask("hit-" + PowderUtil.generateID(5), powder, 
				new EntityTracker(hitEntity, creator));
	}

	public static void saveAttached(Entity entity, PowderTask powderTask) {
		saveAttached(entity, powderTask.getPowder(), powderTask.getTracker().getCreator());
	}

	public static void saveAttached(Entity entity, Powder powder, UUID creator) {
		FileConfiguration playerdata = PowderPlugin.get().getPlayerDataFile();
		if (entity == null || entity instanceof Player) return;
		if (playerdata == null) return;
		playerdata.set(creator + ".attached." + entity.getUniqueId() + ".powder", powder.getName());
		playerdata.set(creator + ".attached." + entity.getUniqueId() + ".loop", powder.isLooping());

		saveFile(playerdata, PLAYER_DATA_FILE);
	}

	public static void removeAttached(UUID entityUUID) {
		FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();

		for (String uuid : playerDataFile.getKeys(false)) {
			if (playerDataFile.getConfigurationSection(uuid + ".attached") == null) continue;
			for (String otherEntityUUID : playerDataFile.getConfigurationSection(uuid + ".attached").getKeys(false)) {
				if (!otherEntityUUID.equals(entityUUID.toString())) continue;
				playerDataFile.set(uuid + ".attached." + otherEntityUUID, null);
			}
		}

		saveFile(playerDataFile, PLAYER_DATA_FILE);
	}

	public static void loadAllAttached(Collection<UUID> uuids) {
		FileConfiguration playerDataFile = PowderPlugin.get().getPlayerDataFile();

		for (String uuid : playerDataFile.getKeys(false)) {
			if (playerDataFile.getConfigurationSection(uuid + ".attached") == null) continue;
			for (String entityUUID : playerDataFile.getConfigurationSection(uuid + ".attached").getKeys(false)) {
				boolean toLoad = false;
				for (UUID otherEntityUUID : uuids) {
					if (entityUUID.equals(otherEntityUUID.toString())) {
						toLoad = true;
						break;
					}
				}
				if (!toLoad) continue;

				Entity entity = Bukkit.getEntity(UUID.fromString(entityUUID));
				if (entity == null) continue;
				UUID creator = UUID.fromString(uuid);
				if (creator == null) continue;

				String powderName = playerDataFile.getString(uuid + ".attached." + entityUUID + ".powder");
				boolean loop = playerDataFile.getBoolean(uuid + ".attached." + entityUUID + ".loop");

				Powder powder = PowderPlugin.get().getPowderHandler().getPowder(powderName);
				if (powder == null) continue;
				if (loop) powder = powder.loop();
				powder.spawn(entity, creator);
			}
		}
	}

}