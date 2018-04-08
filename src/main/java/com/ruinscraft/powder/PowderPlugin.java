package com.ruinscraft.powder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.powder.models.Dust;
import com.ruinscraft.powder.models.Layer;
import com.ruinscraft.powder.models.ParticleMatrix;
import com.ruinscraft.powder.models.ParticleName;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderParticle;
import com.ruinscraft.powder.models.SoundEffect;
import com.ruinscraft.powder.storage.MySqlStorage;
import com.ruinscraft.powder.storage.Storage;
import com.ruinscraft.powder.util.ImageUtil;
import com.ruinscraft.powder.util.PowderUtil;
import com.ruinscraft.powder.util.SoundUtil;

import net.md_5.bungee.api.ChatColor;

public class PowderPlugin extends JavaPlugin {

	private static PowderPlugin instance;
	private static PowderHandler powderHandler;

	public static String PREFIX;

	private FileConfiguration config;
	private List<FileConfiguration> powderConfigs;

	private Storage storage;

	public static PowderPlugin getInstance() {
		return instance;
	}

	public void onEnable() {
		instance = this;

		loadConfig();

		enableStorage();

		// load all powders async & load users' powders if storage is enabled
		PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
			loadPowdersFromSources();

			// load all saved powders from db if enabled
			if (useStorage()) {
				PowderUtil.loadPowdersForOnline();
			}
		});

		getCommand("powder").setExecutor(new PowderCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		// prefix for all messages with prefixes
		PREFIX = PowderUtil.color(config.getString("prefix"));
	}

	public void onDisable() {
		if (useStorage()) {
			// save all current users' powders before disabling
			PowderUtil.savePowdersForOnline();
		}

		// delete all tasks & powders
		powderHandler.clearEverything();

		instance = null;
	}

	// end all current tasks & reinitialize powderHandler
	public void cleanHandlers() {
		if (!(powderHandler == null)) {
			powderHandler.clearEverything();
		}
		powderHandler = new PowderHandler();
	}

	public PowderHandler getPowderHandler() {
		return powderHandler;
	}

	public List<FileConfiguration> getPowderConfigs() {
		return powderConfigs;
	}

	public Storage getStorage() {
		return storage;
	}

	public boolean useStorage() {
		return getStorage() != null;
	}

	public void enableStorage() {
		// enable storage if enabled in configuration
		if (config.getBoolean("storage.mysql.use")) {
			String host = config.getString("storage.mysql.host");
			int port = config.getInt("storage.mysql.port");
			String database = config.getString("storage.mysql.database");
			String username = config.getString("storage.mysql.username");
			String password = config.getString("storage.mysql.password");
			String powdersTable = config.getString("storage.mysql.table");

			storage = new MySqlStorage(host, port, database, username, password, powdersTable);

			getLogger().info("Using MySQL storage");
		}
	}

	public void loadPowderConfigs() {
		// list of configuration files that contain Powders
		powderConfigs = new ArrayList<FileConfiguration>();

		BufferedReader reader;

		for (String urlName : config.getStringList("powderSources")) {

			FileConfiguration powderConfig;
			URL url = PowderUtil.readURL(urlName);
			File file;
			// if a file is from a path, load from within data folder
			if (!urlName.contains("/")) {

				file = new File(getDataFolder(), urlName);
				if (!file.exists()) {
					getLogger().warning("Failed to load config file '" + urlName + "'.");
					continue;
				}
				powderConfig = YamlConfiguration.loadConfiguration(file);

				// else, load from URL
			} else if (url != null) {

				InputStream stream = PowderUtil.getInputStreamFromURL(url);

				if (stream == null) {
					continue;
				}

				reader = new BufferedReader(new InputStreamReader(stream));
				powderConfig = YamlConfiguration.loadConfiguration(reader);

			} else {
				getLogger().warning("Failed to load config file '" + urlName + "'.");
				continue;
			}

			powderConfigs.add(powderConfig);

		}

		// if powders.yml is listed as a source but doesn't exist, create it
		File defaultPowderConfig = new File(getDataFolder(), "powders.yml");
		if (!defaultPowderConfig.exists() && config.getStringList("powderSources").contains("powders.yml")) {
			getLogger().info("powders.yml not found but listed as a source, creating!");
			saveResource("powders.yml", false);
			FileConfiguration powderConfig = YamlConfiguration.loadConfiguration(defaultPowderConfig);
			powderConfigs.add(powderConfig);
		}
	}

	public void loadConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			getLogger().info("config.yml not found, creating!");
			saveDefaultConfig();
		}
		reloadConfig();
		config = getConfig();
	}

	// load all Powders from their source yaml files
	@SuppressWarnings("unchecked")
	public void loadPowdersFromSources() {
		// load source yaml files
		loadPowderConfigs();

		// remove all existing tasks/Powders
		cleanHandlers();

		// handle categories if enabled
		powderHandler.setIfCategoriesEnabled(config.getBoolean("categoriesEnabled", false));

		if (powderHandler.categoriesEnabled()) {
			for (String s : config.getConfigurationSection("categories").getKeys(false)) {
				powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
			}
			if (!powderHandler.getCategories().keySet().contains("Other")) {
				powderHandler.addCategory("Other", "Unsorted Powders");
			}
		}

		List<String> powderNames = new ArrayList<>();

		String powders = "powders.";

		// alert online players of the reload
		getLogger().info("Loading Powders...");
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(player,
						ChatColor.GRAY + "Loading Powders...", "powder");
			}
		}

		for (FileConfiguration powderConfig : powderConfigs) {
			for (String s : powderConfig.getConfigurationSection("powders").getKeys(false)) {
				Powder powder = new Powder();

				// set some given values if they exist, default value if they don't
				powder.setName(powderConfig.getString(powders + s + ".name", null));
				powder.setDefaultSpacing((float) powderConfig.getDouble(powders + s + ".spacing", .5F));
				powder.setHidden(powderConfig.getBoolean(powders + s + ".hidden", false));

				// add categories if enabled
				if (powderHandler.categoriesEnabled()) {
					for (String t : (List<String>) powderConfig.getList(powders + s + ".categories", new ArrayList<String>())) {
						if (!(powderHandler.getCategories().keySet().contains(t))) {
							getLogger().warning("Invalid category '" + t + 
									"' for '" + powder.getName() + "' in " + powderConfig.getName());
							continue;
						}
						powder.addCategory(t);
					}
					if (powder.getCategories().isEmpty()) {
						powder.addCategory("Other");
					}
				}

				String section = powders + s;
				
				// SoundEffect
				// 'BLOCK_NOTE_PLING;4.0;1.50;2;10;200'
				// 'sound;volume;pitch;startTime;repeatTime;iterations'

				// song
				// 'Shrek.nbs;50;2;0;2400;2'
				// 'fileName;volume;multiplier;startTime;repeatTime;iterations'

				if (!(powderConfig.getConfigurationSection(powders + s + ".songs") == null)) {
					for (String ss : powderConfig.getConfigurationSection(powders + s + ".songs").getKeys(false)) {
						String eachSection = section + ".songs." + ss;
						String fileName = powderConfig.getString(eachSection + ".fileName", "unknownfile.nbs");
						double volume = powderConfig.getDouble(eachSection + ".volume", 1);
						double multiplier = powderConfig.getDouble(eachSection + ".multiplier", 1);
						int startTime = powderConfig.getInt(eachSection + ".startTime", 0);
						int repeatTime = powderConfig.getInt(eachSection + ".repeatTime", 20);
						int iterations = powderConfig.getInt(eachSection + ".iterations", 1);
						powder.getSoundEffects().addAll(SoundUtil.getSoundEffectsFromNBS(fileName, volume, 
								multiplier, startTime, repeatTime, iterations));
					}
				}
				if (!(powderConfig.getConfigurationSection(powders + s + ".sounds") == null)) {
					for (String ss : powderConfig.getConfigurationSection(powders + s + ".sounds").getKeys(false)) {
						String eachSection = section + ".sounds." + ss;
						String soundEnum = powderConfig.getString(eachSection + ".soundEnum", "BLOCK_NOTE_CHIME");
						Sound sound = Sound.valueOf(soundEnum);
						double volume = powderConfig.getDouble(eachSection + ".volume", 1);
						float soundPitch = (float) powderConfig.getDouble(eachSection + ".note", 1);
						soundPitch = (float) Math.pow(2.0, ((double)soundPitch - 12.0) / 12.0);
						int startTime = powderConfig.getInt(eachSection + ".startTime", 0);
						int repeatTime = powderConfig.getInt(eachSection + ".repeatTime", 20);
						int iterations = powderConfig.getInt(eachSection + ".iterations", 1);
						powder.addSoundEffect(new SoundEffect(sound, volume, soundPitch, startTime, repeatTime, iterations));
					}
				}
				if (!(powderConfig.getConfigurationSection(powders + s + ".changes") == null)) {
					for (String ss : powderConfig.getConfigurationSection(powders + s + ".changes").getKeys(false)) {
						String eachSection = section + ".changes." + ss;
						String particleChar = powderConfig.getString(eachSection + ".particleChar", "A");
						String particleEnum = powderConfig.getString(eachSection + ".particleEnum", "HEART");
						Particle particle = Particle.valueOf(particleEnum);
						double xOffset = powderConfig.getDouble(eachSection + ".xOffset", 0);
						double yOffset = powderConfig.getDouble(eachSection + ".yOffset", 0);
						double zOffset = powderConfig.getDouble(eachSection + ".zOffset", 0);
						double data = powderConfig.getDouble(eachSection + ".data", 0);
						powder.addPowderParticle(new PowderParticle(particleChar, particle, xOffset, yOffset, zOffset, data));
					}
				}

				// Dust
				// 'A;2;1;3;3;0'
				// 'PowderParticle;radius;height&depth;startTime;repeatTime;iterations'

				if (!(powderConfig.getConfigurationSection(powders + s + ".dusts") == null)) {
					for (String ss : powderConfig.getConfigurationSection(powders + s + ".dusts").getKeys(false)) {
						String eachSection = section + ".dusts." + ss;
						String dustName = powderConfig.getString(eachSection + ".name", "null");
						PowderParticle powderParticle = powder.getPowderParticle(dustName);
						if (powderParticle == null) {
							try {
								Particle particle = Particle.valueOf(ParticleName.valueOf(dustName).getName());
								powderParticle = new PowderParticle(dustName, particle);
							} catch (Exception e) {
								powderParticle = new PowderParticle(null, null);
							}
						}
						double radius = powderConfig.getDouble(eachSection + ".radius", 1);
						double height = powderConfig.getDouble(eachSection + ".height", 1);
						int startTime = powderConfig.getInt(eachSection + ".startTime", 0);
						int repeatTime = powderConfig.getInt(eachSection + ".repeatTime", 20);
						int iterations = powderConfig.getInt(eachSection + ".iterations", 1);
						powder.addDust(new Dust(powderParticle, radius, height, startTime, repeatTime, iterations));
					}
				}

				// [.1;true;2;12;10]
				// [spacing;pitch;startTime;repeatTime;iterations]
				
				int left = 0;
				int up = 0;
				String matrixSection = powders + s + ".matrices";
				
				if (!(powderConfig.getConfigurationSection(matrixSection) == null)) {
					for (String ss : powderConfig.getConfigurationSection(matrixSection).getKeys(false)) {
						String eachSection = section + ".matrices." + ss;
						ParticleMatrix particleMatrix = new ParticleMatrix();
						boolean test = powderConfig.getBoolean(eachSection + ".hasPitch");
						particleMatrix.setSpacing(powderConfig.getDouble(eachSection + ".spacing", .1));
						particleMatrix.setIfPitch(powderConfig.getBoolean(eachSection + ".hasPitch", false));
						particleMatrix.setStartTime(powderConfig.getInt(eachSection + ".startTime", 0));
						particleMatrix.setRepeatTime(powderConfig.getInt(eachSection + ".repeatTime", 20));
						particleMatrix.setLockedIterations(powderConfig.getInt(eachSection + ".iterations", 1));
						for (String sss : powderConfig.getConfigurationSection(eachSection + ".layers").getKeys(false)) {
							String eachEachSection = eachSection + ".layers." + sss;
							Layer layer = new Layer();
							layer.setPosition(powderConfig.getDouble(eachEachSection + ".position", 0));
							for (String t : (List<String>) powderConfig.getList(eachEachSection + ".layerMatrix", new ArrayList<String>())) {
								if (t.contains(":")) {
									if (t.contains("img:")) {
										String urlName;
										int width;
										int height;
										t = t.replace("img:", "");
										urlName = t.substring(0, t.indexOf(";"));
										t = t.substring(t.indexOf(";") + 1, t.length());
										width = Integer.valueOf(t.substring(0, t.indexOf(";")));
										t = t.substring(t.indexOf(";") + 1, t.length());
										height = Integer.valueOf(t);
										try {
											ImageUtil.getRows(layer.getRows(), urlName, width, height);
										} catch (IOException io) {
											getLogger().warning("Failed to load image: '" + urlName + "'");
											continue;
										}
										// add height to compensate for dist. from location (might not necessarily correspond w/ actual image)
										up = up + height;
									}
									continue;
								}
								// if the Layer is in the same position as where the location/player is
								if (layer.getPosition() == 0) {
									up++;
									// if the string contains location/player
									if (t.contains("?")) {
										// set the left & up of the Layer so createPowders() knows where to start
										left = (t.indexOf("?")) + 1;
										// set default if it's the matrix spawned immediately 
										if (particleMatrix.getStartTime() == 0) {
											powder.setDefaultLeft(left);
											powder.setDefaultUp(up);
										}
										particleMatrix.setPlayerLeft(left);
										particleMatrix.setPlayerUp(up);
									}
								}
								// add a row to the Layer if it has gone through everything
								// rows contain PowderParticles
								List<PowderParticle> row = new ArrayList<PowderParticle>();
								for (char character : t.toCharArray()) {
									String string = String.valueOf(character);
									PowderParticle powderParticle;
									powderParticle = powder.getPowderParticle(string);
									if (powderParticle == null) {
										try {
											Particle particle = Particle.valueOf(ParticleName.valueOf(string).getName());
											powderParticle = new PowderParticle(string, particle);
										} catch (Exception e) {
											powderParticle = new PowderParticle(null, null);
										}
									}
									row.add(powderParticle);
								}
								layer.addRow(row);
							}
							particleMatrix.addLayer(layer);
						}
						powder.addMatrix(particleMatrix);
					}
				}
				if (powder.getMatrices().isEmpty() && powder.getSoundEffects().isEmpty() && powder.getDusts().isEmpty()) {
					getLogger().warning("Powder '" + powder.getName() + "' appears empty and was not loaded.");
					continue;
				}
				powderNames.add(powder.getName());
				getPowderHandler().addPowder(powder);
			}
		}

		// do this again to load {total} parameter
		if (powderHandler.categoriesEnabled()) {
			for (String s : config.getConfigurationSection("categories").getKeys(false)) {
				powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
			}
			if (!powderHandler.getCategories().keySet().contains("Other")) {
				powderHandler.addCategory("Other", "Unsorted Powders");
			}
		}

		String powderAmount = String.valueOf(powderNames.size());

		// alert console of the Powders loaded
		StringBuilder msg = new StringBuilder();
		msg.append("Loaded Powders: ");
		for (String effect : powderNames) {
			if (powderNames.get(powderNames.size() - 1).equals(effect)) {
				msg.append(effect + ". " + powderAmount + " total!");
			} else {
				msg.append(effect + ", ");
			}
		}
		getLogger().info(msg.toString());

		// alert online players with permission of the Powders loaded
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(player,
						ChatColor.GRAY + "All Powders loaded! (" + powderAmount + " total)", "powder");
			}
		}
	}

}
