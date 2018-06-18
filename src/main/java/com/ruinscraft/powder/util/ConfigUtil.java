package com.ruinscraft.powder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.Dust;
import com.ruinscraft.powder.models.Layer;
import com.ruinscraft.powder.models.ParticleMatrix;
import com.ruinscraft.powder.models.ParticleName;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderParticle;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.SoundEffect;
import com.ruinscraft.powder.models.trackers.StationaryTracker;
import com.ruinscraft.powder.models.trackers.Tracker;
import com.ruinscraft.powder.models.trackers.TrackerType;

public class ConfigUtil {

	public static FileConfiguration loadConfig() {
		FileConfiguration config = null;
		PowderPlugin instance = PowderPlugin.getInstance();
		File configFile = new File(instance.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			instance.getLogger().info("config.yml not found, creating!");
			instance.saveDefaultConfig();
		}
		instance.reloadConfig();
		config = instance.getConfig();
		PowderPlugin.getInstance().setConfigVersion(config.getInt("configVersion", 0));
		checkConfigVersion();
		return config;
	}

	public static boolean checkConfigVersion() {
		int configVersion = PowderPlugin.getInstance().getConfigVersion();
		int currentConfigVersion = 1;
		int versionsBehind = currentConfigVersion - configVersion;
		if (versionsBehind == 1) {
			Bukkit.getLogger().warning("Your config version is out of date! You are " + 
					"1 version behind. Please update to get the latest functionality.");
			return false;
		} else if (versionsBehind > 1) {
			Bukkit.getLogger().warning("Your config version is out of date! You are " + 
					versionsBehind + " versions behind. Please " + 
					"update to get the latest functionality.");
			return false;
		} else {
			return true;
		}
	}

	public static List<FileConfiguration> loadPowderConfigs() {
		// list of configuration files that contain Powders
		List<FileConfiguration> powderConfigs = new ArrayList<>();

		Logger logger = PowderPlugin.getInstance().getLogger();
		FileConfiguration config = PowderPlugin.getInstance().getConfig();
		File dataFolder = PowderPlugin.getInstance().getDataFolder();

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
					logger.warning("Failed to load config file '" + urlName + "'.");
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
				logger.warning("Failed to load config file '" + urlName + "'.");
				continue;
			}

			powderConfigs.add(powderConfig);
		}

		// if powders.yml is listed as a source but doesn't exist, create it
		File defaultPowderConfig = new File(dataFolder, "powders.yml");
		if (!defaultPowderConfig.exists() && 
				config.getStringList("powderSources").contains("powders.yml")) {
			logger.info("powders.yml not found and listed as a source, creating!");
			PowderPlugin.getInstance().saveResource("powders.yml", false);
			FileConfiguration powderConfig = 
					YamlConfiguration.loadConfiguration(defaultPowderConfig);
			powderConfigs.add(powderConfig);
		}
		return powderConfigs;
	}

	public static void reloadCategories() {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		FileConfiguration config = PowderPlugin.getInstance().getConfig();
		if (powderHandler.categoriesEnabled()) {
			for (String s : config.getConfigurationSection("categories").getKeys(false)) {
				powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
			}
			if (!powderHandler.getCategories().keySet().contains("Other")) {
				powderHandler.addCategory("Other", "Unsorted Powders");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static Powder loadPowderFromConfig(FileConfiguration powderConfig, String s) {
		Powder powder = new Powder();

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		Logger logger = PowderPlugin.getInstance().getLogger();

		String section = "powders." + s;

		// set some given values if they exist, default value if they don't
		powder.setName(powderConfig.getString(section + ".name", null));
		powder.setDefaultSpacing(
				powderConfig.getDouble(section + ".defaultSpacing", .1));
		powder.setDefaultStartTime(
				powderConfig.getInt(section + ".defaultStartTime", 0));
		powder.setDefaultRepeatTime(
				powderConfig.getInt(section + ".defaultRepeatTime", 20));
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

		// add categories if enabled
		if (powderHandler.categoriesEnabled()) {
			for (String t : (List<String>) powderConfig
					.getList(section + ".categories", new ArrayList<String>())) {
				if (!(powderHandler.getCategories().keySet().contains(t))) {
					logger.warning("Invalid category '" + t + 
							"' for '" + powder.getName() + "' in " + powderConfig.getName());
					continue;
				}
				powder.addCategory(t);
			}
			if (powder.getCategories().isEmpty()) {
				powder.addCategory("Other");
			}
		}
		if (!(powderConfig.getConfigurationSection(section + ".songs") == null)) {
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
				for (SoundEffect soundEffect : songSoundEffects) {
					powder.addPowderElement(soundEffect);
				}
			}
		}
		if (!(powderConfig.getConfigurationSection(section + ".sounds") == null)) {
			for (String ss : powderConfig
					.getConfigurationSection(section + ".sounds").getKeys(false)) {
				String eachSection = section + ".sounds." + ss;
				String soundEnum = powderConfig.getString(eachSection + ".soundEnum", 
						"BLOCK_NOTE_CHIME");
				Sound sound = Sound.valueOf(soundEnum);
				double volume = powderConfig.getDouble(eachSection + ".volume", 1);
				float soundPitch = (float) powderConfig.getDouble(eachSection + ".note", 1);
				soundPitch = (float) Math.pow(2.0, ((double)soundPitch - 12.0) / 12.0);
				boolean surroundSound = powderConfig.getBoolean(
						eachSection + ".surroundSound", true);
				powder.addPowderElement(new SoundEffect(sound, volume, soundPitch, surroundSound,
						getStart(powderConfig, powder, eachSection), 
						getRepeat(powderConfig, powder, eachSection), 
						getIterations(powderConfig, powder, eachSection)));
			}
		}
		if (!(powderConfig.getConfigurationSection(section + ".changes") == null)) {
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
				double data = powderConfig.getDouble(eachSection + ".data", 0);
				powder.addPowderParticle(new PowderParticle(character, particle, 
						amount, xOffset, yOffset, zOffset, data));
			}
		}
		if (!(powderConfig.getConfigurationSection(section + ".dusts") == null)) {
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
						powderParticle = new PowderParticle(character, particle);
					} catch (Exception e) {
						powderParticle = new PowderParticle();
					}
				}
				double radius = powderConfig.getDouble(eachSection + ".radius", 1);
				double height = powderConfig.getDouble(eachSection + ".height", 1);
				double span = powderConfig.getDouble(eachSection + ".span", 1);
				List<PowderElement> addedPowderElements = new ArrayList<>();
				if (powderConfig.getBoolean(eachSection + ".attachToNote")) {
					String noteName = powderConfig.getString(eachSection + ".attachedToNote", 
							"BLOCK_NOTE_HARP");
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
		if (!(powderConfig.getConfigurationSection(section + ".matrices") == null)) {
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
					Layer layer = new Layer();
					layer.setPosition(powderConfig.getDouble(eachEachSection + ".position", 0));
					for (String ssss : (List<String>) powderConfig
							.getList(eachEachSection + ".layerMatrix", new ArrayList<String>())) {
						if (ssss.contains(":")) {
							if (ssss.contains("img:")) {
								String urlName;
								int width;
								int height;
								ssss = ssss.replace("img:", "");
								urlName = ssss.substring(0, ssss.indexOf(";"));
								ssss = ssss.substring(ssss.indexOf(";") + 1, ssss.length());
								width = Integer.valueOf(ssss.substring(0, ssss.indexOf(";")));
								ssss = ssss.substring(ssss.indexOf(";") + 1, ssss.length());
								height = Integer.valueOf(ssss);
								try {
									ImageUtil.getRows(layer.getRows(), urlName, width, height);
								} catch (IOException io) {
									logger.warning("Failed to load image: '" + urlName + "'");
									continue;
								}
								// add height to compensate for dist. from location 
								// (might not necessarily correspond w/ actual image)
								up = up + height;
							}
							continue;
						}
						// if the Layer is in the same position as where the location/player is
						if (layer.getPosition() == 0) {
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
						// add a row to the Layer if it has gone through everything
						// rows contain PowderParticles
						List<PowderParticle> row = new ArrayList<>();
						for (char character : ssss.toCharArray()) {
							PowderParticle powderParticle;
							powderParticle = powder.getPowderParticle(character);
							if (powderParticle == null) {
								try {
									String string = String.valueOf(character);
									Particle particle = Particle.valueOf(
											ParticleName.valueOf(string).getName());
									powderParticle = new PowderParticle(character, particle);
								} catch (Exception e) {
									powderParticle = new PowderParticle();
								}
							}
							row.add(powderParticle);
						}
						layer.addRow(row);
					}
					particleMatrix.addLayer(layer);
				}
				if (!containsPlayer) {
					particleMatrix.setPlayerLeft(powder.getDefaultLeft());
					particleMatrix.setPlayerUp(powder.getDefaultUp());
				}
				if (powderConfig.getInt(eachSection + ".settings.gradient.type", 0) > 0) {
					int gradient = powderConfig.getInt(eachSection + ".settings.gradient.type");
					int tickSpeed = powderConfig.getInt(eachSection + ".settings.gradient.speed", 1);
					int length = powderConfig.getInt(eachSection + ".settings.gradient.length", 1);
					powder.addPowderElements(setGradients(particleMatrix, gradient, tickSpeed, length));
				} else {
					powder.addPowderElement(particleMatrix);
				}
			}
		}
		if (powder.getPowderElements().isEmpty()) {
			PowderPlugin.getInstance().getLogger().warning("Powder '" + 
					powder.getName() + "' appears empty and/or incorrectly formatted.");
			return null;
		}
		return powder;
	}

	public static int getStart(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".startTime", powder.getDefaultStartTime());
	}

	public static int getRepeat(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".repeatTime", powder.getDefaultRepeatTime());
	}

	public static int getIterations(FileConfiguration powderConfig, Powder powder, String section) {
		return powderConfig.getInt(section + ".iterations", powder.getDefaultLockedIterations());
	}

	public static List<ParticleMatrix> setGradients(ParticleMatrix matrix, 
			int gradient, int tickSpeed, int length) {
		List<ParticleMatrix> newMatrices = new ArrayList<>();
		switch (gradient) {
			// diagram https://i.imgur.com/0uL5i3a.png
			case 1: {
				// 25 26
				int highest = -1;
				for (Layer layer : matrix.getLayers()) {
					int rows = layer.getRows().size();
					if (rows > highest) {
						highest = rows;
					}
				}
				if (length > highest) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				int newPlayerUp = matrix.getPlayerUp() + length + 1;
				for (int i = highest + length; i >= 0; i = i - length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int j = 0; j < matrix.getLayers().size(); j++) {
						Layer layer = new Layer();
						Layer otherLayer = matrix.getLayers().get(j);
						layer.setPosition(otherLayer.getPosition());
						List<List<PowderParticle>> reversedList = 
								new ArrayList<>(otherLayer.getRows());
						Collections.reverse(reversedList);
						for (int k = i; k > i - length; k--) {
							try {
								layer.addRow(reversedList.get(k));
							} catch (Exception e) {
								layer.addRow(new ArrayList<PowderParticle>());
							}
						}
						newMatrix.addLayer(layer);
					}
					newMatrix.setSpacing(matrix.getSpacing());
					newMatrix.setAddedPitch(matrix.getAddedPitch());
					newMatrix.setAddedRotation(matrix.getAddedRotation());
					newMatrix.setAddedTilt(matrix.getAddedTilt());
					newMatrix.setIfPitch(matrix.hasPitch());
					newMatrix.setPlayerLeft(matrix.getPlayerLeft());
					newMatrix.setPlayerUp(newPlayerUp);
					newMatrix.setStartTime(newStartTime);
					newMatrix.setRepeatTime(matrix.getRepeatTime());
					newMatrix.setLockedIterations(matrix.getLockedIterations());
					newMatrices.add(newMatrix);

					newPlayerUp = newPlayerUp - length;
					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 2: {
				// 26 25
				int highest = -1;
				for (Layer layer : matrix.getLayers()) {
					int rows = layer.getRows().size();
					if (rows > highest) {
						highest = rows;
					}
				}
				if (length > highest) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				int newPlayerUp = matrix.getPlayerUp() - highest;
				for (int i = length; i <= highest + length; i = i + length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int j = 0; j < matrix.getLayers().size(); j++) {
						Layer layer = new Layer();
						Layer otherLayer = matrix.getLayers().get(j);
						layer.setPosition(otherLayer.getPosition());
						List<List<PowderParticle>> reversedList = 
								new ArrayList<>(otherLayer.getRows());
						Collections.reverse(reversedList);
						for (int k = i - 1; k >= i - length; k--) {
							try {
								layer.addRow(reversedList.get(k));
							} catch (Exception e) {
								layer.addRow(new ArrayList<PowderParticle>());
							}
						}
						newMatrix.addLayer(layer);
					}

					newPlayerUp = newPlayerUp + length;

					newMatrix.setSpacing(matrix.getSpacing());
					newMatrix.setAddedPitch(matrix.getAddedPitch());
					newMatrix.setAddedRotation(matrix.getAddedRotation());
					newMatrix.setAddedTilt(matrix.getAddedTilt());
					newMatrix.setIfPitch(matrix.hasPitch());
					newMatrix.setPlayerLeft(matrix.getPlayerLeft());
					newMatrix.setPlayerUp(newPlayerUp);
					newMatrix.setStartTime(newStartTime);
					newMatrix.setRepeatTime(matrix.getRepeatTime());
					newMatrix.setLockedIterations(matrix.getLockedIterations());
					newMatrices.add(newMatrix);

					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 3: {
				// 24 22
				int highest = -1;
				for (Layer layer : matrix.getLayers()) {
					for (List<PowderParticle> list : layer.getRows()) {
						int rows = list.size();
						if (rows > highest) {
							highest = rows;
						}
					}
				}
				if (length > highest) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				for (int i = 0; i <= highest + length; i = i + length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (Layer layer : matrix.getLayers()) {
						Layer newLayer = new Layer();
						newLayer.setPosition(layer.getPosition());
						for (List<PowderParticle> list : layer.getRows()) {
							List<PowderParticle> row = new ArrayList<>();
							for (int l = 0; l < highest; l++) {
								row.add(new PowderParticle());
							}
							for (int k = i; k < i + length; k++) {
								try {
									row.add(k, list.get(k));
								} catch (Exception e) {
									row.add(new PowderParticle());
								}
							}
							newLayer.addRow(row);
						}
						newMatrix.addLayer(newLayer);
					}

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
					newMatrices.add(newMatrix);

					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 4: {
				// 22 24
				int highest = -1;
				for (Layer layer : matrix.getLayers()) {
					for (List<PowderParticle> list : layer.getRows()) {
						int rows = list.size();
						if (rows > highest) {
							highest = rows;
						}
					}
				}
				if (length > highest) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				for (int i = highest + length; i >= 0; i = i - length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (Layer layer : matrix.getLayers()) {
						Layer newLayer = new Layer();
						newLayer.setPosition(layer.getPosition());
						for (List<PowderParticle> list : layer.getRows()) {
							List<PowderParticle> row = new ArrayList<>();
							for (int l = 0; l < highest; l++) {
								row.add(new PowderParticle());
							}
							for (int k = i; k > i - length; k--) {
								try {
									row.add(k, list.get(k));
								} catch (Exception e) {
									row.add(new PowderParticle());
								}
							}
							newLayer.addRow(row);
						}
						newMatrix.addLayer(newLayer);
					}

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
					newMatrices.add(newMatrix);

					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 5: {
				// 21 23
				int farthestPos = 0;
				int farthestNeg = 0;
				for (Layer layer : matrix.getLayers()) {
					int position = (int) layer.getPosition();
					if (position > farthestPos) {
						farthestPos = position;
					}
					if (position < farthestNeg) {
						farthestNeg = position;
					}
				}
				int distance = farthestPos - farthestNeg;
				if (length > distance) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				for (int i = farthestPos; i > farthestNeg - length; i = i - length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int j = i; j > i - length; j--) {
						boolean done = false;
						for (Layer layer : matrix.getLayers()) {
							if (layer.getPosition() == j) {
								newMatrix.addLayer(layer);
								done = true;
							}
						}
						if (!done) {
							newMatrix.addLayer(new Layer());
						}
					}
					if (newMatrix.getLayers().isEmpty()) {
						continue;
					}

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
					newMatrices.add(newMatrix);

					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 6: {
				// 23 21
				int farthestPos = 0;
				int farthestNeg = 0;
				for (Layer layer : matrix.getLayers()) {
					int position = (int) layer.getPosition();
					if (position > farthestPos) {
						farthestPos = position;
					}
					if (position < farthestNeg) {
						farthestNeg = position;
					}
				}
				int distance = farthestPos - farthestNeg;
				if (length > distance) {
					newMatrices.add(matrix);
					break;
				}
				int newStartTime = 0;
				for (int i = farthestNeg; i < farthestPos + length; i = i + length) {
					ParticleMatrix newMatrix = new ParticleMatrix();
					for (int j = i; j < i + length; j++) {
						boolean done = false;
						for (Layer layer : matrix.getLayers()) {
							if (layer.getPosition() == j) {
								newMatrix.addLayer(layer);
								done = true;
							}
						}
						if (!done) {
							newMatrix.addLayer(new Layer());
						}
					}
					if (newMatrix.getLayers().isEmpty()) {
						continue;
					}

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
					newMatrices.add(newMatrix);

					newStartTime = newStartTime + tickSpeed;
				}
				break;
			}
			case 7: {
				// 2 12
			}
			case 8: {
				// 1 11
			}
			case 9: {
				// 11 1
			}
			case 10: {
				// 12 2
			}
			case 11: {
				// 10 3
			}
			case 12: {
				// 9 4
			}
			case 13: {
				// 4 9
			}
			case 14: {
				// 3 10
			}
			case 15: {
				// 13 7
			}
			case 16: {
				// 5 15
			}
			case 17: {
				// 7 13
			}
			case 18: {
				// 15 5
			}
			case 19: {
				// 16 6
			}
			case 20: {
				// 8 14
			}
			case 21: {
				// 6 16
			}
			case 22: {
				// 14 8
			}
			default: {
				newMatrices.add(matrix);
			}
		}
		return newMatrices;
	}

	public static void saveFile(FileConfiguration config, String fileName) {
		try {
			File file = new File(PowderPlugin.getInstance()
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
		FileConfiguration config = PowderPlugin.getInstance().getCreatedPowdersFile();
		if (config == null) {
			return false;
		}
		if (config.getConfigurationSection("created." 
				+ PowderUtil.cleanPowderTaskName(powderTask)) == null) {
			return false;
		}
		return true;
	}

	public static FileConfiguration loadCreatedPowders() {
		FileConfiguration config = null;
		PowderPlugin instance = PowderPlugin.getInstance();
		File configFile = new File(instance.getDataFolder(), "createdpowders.yml");
		if (configFile.exists()) {
			config = YamlConfiguration.loadConfiguration(configFile);
		} else {
			return null;
		}
		if (config == null) {
			return null;
		}
		PowderPlugin.getInstance().setCreatedPowdersFile(config);
		Set<PowderTask> powderTasks = loadStationaryPowders();
		for (PowderTask powderTask : powderTasks) {
			instance.getPowderHandler().runPowderTask(powderTask);
		}
		return config;
	}

	public static void saveStationaryPowder(
			FileConfiguration createdPowders, PowderTask powderTask) {
		if (powderTask.getTrackerType() == TrackerType.STATIONARY) {
			PowderPlugin instance = PowderPlugin.getInstance();
			if (createdPowders == null) {
				File file = new File(instance.getDataFolder(), "createdpowders.yml");
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				createdPowders = YamlConfiguration.loadConfiguration(file);
				instance.setCreatedPowdersFile(createdPowders);
			}
			String path = "created." + PowderUtil.cleanPowderTaskName(powderTask);
			createdPowders.set(path + ".name", powderTask.getName());
			int i = 0;
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				i++;
				Powder powder = entry.getKey();
				Tracker tracker = entry.getValue();
				if (tracker.getType() != TrackerType.STATIONARY) {
					continue;
				}
				StationaryTracker stationaryTracker = (StationaryTracker) tracker;
				String powderPath = path + ".powder" + String.valueOf(i);
				createdPowders.set(powderPath + ".powder", powder.getName());
				powderPath = powderPath + ".location";
				Location location = stationaryTracker.getCurrentLocation().clone();
				createdPowders.set(powderPath + ".world", location.getWorld().getName());
				createdPowders.set(powderPath + ".x", location.getX());
				createdPowders.set(powderPath + ".y", location.getY());
				createdPowders.set(powderPath + ".z", location.getZ());
				createdPowders.set(powderPath + ".pitch", location.getPitch());
				createdPowders.set(powderPath + ".yaw", location.getYaw());
			}
			saveFile(createdPowders, "createdpowders.yml");
		}
	}

	public static Set<PowderTask> loadStationaryPowders() {
		Set<PowderTask> powderTasks = new HashSet<>();
		FileConfiguration createdPowders = PowderPlugin.getInstance().getCreatedPowdersFile();
		if (createdPowders == null) {
			return powderTasks;
		}
		for (String task : createdPowders.getConfigurationSection("created").getKeys(false)) {
			PowderTask powderTask = loadStationaryPowder(createdPowders, "created." + task);
			if (powderTask == null) {
				continue;
			}
			powderTasks.add(powderTask);
		}
		return powderTasks;
	}

	public static PowderTask loadStationaryPowder(FileConfiguration config, String section) {
		PowderTask powderTask = new PowderTask(config.getString(section + ".name"));
		for (String powderSection : config.getConfigurationSection(section).getKeys(false)) {
			String newSection = section + "." + powderSection;
			if (powderSection.equals("name")) {
				continue;
			}
			String powderName = config.getString(newSection + ".powder");
			Powder powder = PowderPlugin.getInstance().getPowderHandler().getPowder(powderName);
			if (powder == null) {
				Bukkit.getLogger().warning("Unknown Powder '" + 
						powderName + "' in createdpowders.yml");
				return null;
			}
			newSection = newSection + ".location";
			String worldName = config.getString(newSection + ".world");
			World world = Bukkit.getWorld(worldName);
			if (world == null) {
				Bukkit.getLogger().warning("Unknown World '" + 
						worldName + "' in createdpowders.yml");
				return null;
			}
			double x = config.getDouble(newSection + ".x");
			double y = config.getDouble(newSection + ".y");
			double z = config.getDouble(newSection + ".z");
			float yaw = (float) config.getDouble(newSection + ".yaw");
			float pitch = (float) config.getDouble(newSection + ".pitch");
			Location location = new Location(world, x, y, z, yaw, pitch);
			powderTask.addPowder(powder, new StationaryTracker(location));
		}
		return powderTask;
	}

	public static void removeStationaryPowder(PowderTask powderTask) {
		if (powderTask.getTrackerType() == TrackerType.STATIONARY) {
			FileConfiguration createdPowders = PowderPlugin.getInstance().getCreatedPowdersFile();
			PowderPlugin.getInstance().getCreatedPowdersFile()
			.set("created." + PowderUtil.cleanPowderTaskName(powderTask), null);
			saveFile(createdPowders, "createdpowders.yml");
		}
	}

}