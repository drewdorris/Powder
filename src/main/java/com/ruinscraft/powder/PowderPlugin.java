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
import org.bukkit.scheduler.BukkitScheduler;

import com.ruinscraft.powder.models.Dust;
import com.ruinscraft.powder.models.Layer;
import com.ruinscraft.powder.models.ParticleMatrix;
import com.ruinscraft.powder.models.ParticleName;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderParticle;
import com.ruinscraft.powder.models.PowderTask;
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

		// every 100 ticks, remove a PowderTask if it's shown to be finished
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				if (powderHandler == null) {
					return;
				}
				List<PowderTask> toBeRemoved = new ArrayList<PowderTask>();
				for (PowderTask powderTask : powderHandler.getPowderTasks()) {
					boolean active = false;
					for (Integer task : powderTask.getTaskIds()) {
						if (scheduler.isQueued(task) || scheduler.isCurrentlyRunning(task)) {
							active = true;
						}
					}
					if (!(active)) {
						toBeRemoved.add(powderTask);
					}
				}
				powderHandler.getPowderTasks().removeAll(toBeRemoved);
			}
		}, 0L, 100L);
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

		// run every tick to update each PowderTask
		new PowdersCreationTask().runTaskTimer(this, 0L, 1L);

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
				powder.setPitch(powderConfig.getBoolean(powders + s + ".pitch", false));
				powder.setRepeating(powderConfig.getBoolean(powders + s + ".repeating", false));
				powder.setHidden(powderConfig.getBoolean(powders + s + ".hidden", false));
				powder.setDelay(powderConfig.getLong(powders + s + ".delay", Long.MAX_VALUE));

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

				// add sounds; read from string list
				for (String t : (List<String>) powderConfig.getList(powders + s + ".sounds", new ArrayList<String>())) {
					if (t.contains("song:")) {
						t = t.replaceFirst("song:", "");
						String fileName;
						try {
							fileName = t.substring(0, t.indexOf(";"));
							t = t.substring(t.indexOf(";") + 1, t.length());
							double volume = Double.valueOf(t.substring(0, t.indexOf(";")));
							t = t.substring(t.indexOf(";") + 1, t.length());
							double multiplier = Double.valueOf(t.substring(0, t.indexOf(";")));
							t = t.substring(t.indexOf(";") + 1, t.length());
							int addTime = Integer.valueOf(t);
							powder.getSoundEffects().addAll(SoundUtil.getSoundEffectsFromNBS(fileName, volume, multiplier, addTime));
						} catch (Exception e) {
							getLogger().warning("Invalid fileName/multiplier/start time in song '" + t + "'.");
							fileName = t;
							powder.getSoundEffects().addAll(SoundUtil.getSoundEffectsFromNBS(fileName, 1, 1, 0));
						}
						continue;
					}
					Sound sound;
					String soundName;
					soundName = t.substring(0, t.indexOf(";"));
					sound = Sound.valueOf(soundName);
					if ((Sound.valueOf(soundName) == null)) {
						getLogger().warning("Invalid sound name '" + soundName + 
								"' for '" + powder.getName() + "' in " + powderConfig.getName());
						continue;
					}
					t = t.replaceFirst(soundName + ";", "");
					float volume = Float.valueOf(t.substring(0, t.indexOf(";")));
					t = t.substring(t.indexOf(";") + 1, t.length());
					float soundPitch = Float.valueOf(t.substring(0, t.indexOf(";")));
					soundPitch = (float) Math.pow(2.0, ((double)soundPitch - 12.0) / 12.0);
					t = t.substring(t.indexOf(";") + 1, t.length());
					int waitTime = Integer.valueOf(t);
					powder.addSoundEffect(new SoundEffect(sound, volume, soundPitch, waitTime));
				}

				// add changed particles; read from string list
				for (String t : (List<String>) powderConfig.getList(powders + s + ".changes", new ArrayList<String>())) {
					String enumName = t.substring(0, t.indexOf(";"));
					t = t.replaceFirst(enumName + ";", "");
					String particleName = t.substring(0, t.indexOf(";"));
					Particle particle = Particle.valueOf(particleName);
					if (particle == null) {
						getLogger().warning("Invalid particle name '" + particleName + 
								"' for " + powder.getName() + " in " + powderConfig.getName());
						continue;
					}
					t = t.substring(t.indexOf(";") + 1, t.length());
					double xOff;
					double yOff;
					double zOff;
					try {
						xOff = Double.valueOf(t.substring(0, t.indexOf(";")));
					} catch (Exception e) {
						powder.addPowderParticle(new PowderParticle(enumName, particle, 0, 0, 0));
						continue;
					}
					t = t.substring(t.indexOf(";") + 1, t.length());
					yOff = Double.valueOf(t.substring(0, t.indexOf(";")));
					t = t.substring(t.indexOf(";") + 1, t.length());
					// might / might not have data
					try {
						zOff = Double.valueOf(t);
						powder.addPowderParticle(new PowderParticle(enumName, particle, xOff, yOff, zOff));
					} catch (Exception e) {
						zOff = Double.valueOf(t.substring(0, t.indexOf(";")));
						t = t.substring(t.indexOf(";") + 1, t.length());
						Object data;
						try {
							data = Double.valueOf(t);
						} catch (Exception ex) {
							data = t;
						}
						powder.addPowderParticle(new PowderParticle(enumName, particle, xOff, yOff, zOff, data));
					}
				}

				// add dusts; read from string list
				for (String t : (List<String>) powderConfig.getList(powders + s + ".dusts", new ArrayList<String>())) {
					String dustName = t.substring(0, t.indexOf(";"));
					PowderParticle powderParticle;
					powderParticle = powder.getPowderParticle(dustName);
					// can be null if it is nothing
					if (powderParticle == null) {
						try {
							Particle particle = Particle.valueOf(ParticleName.valueOf(dustName).getName());
							powderParticle = new PowderParticle(dustName, particle);
						} catch (Exception e) {
							powderParticle = new PowderParticle(null, null);
						}
					}
					t = t.replaceFirst(dustName + ";", "");
					double radius = Double.valueOf(t.substring(0, t.indexOf(";")));
					t = t.substring(t.indexOf(";") + 1, t.length());
					double height = Float.valueOf(t.substring(0, t.indexOf(";")));
					t = t.substring(t.indexOf(";") + 1, t.length());
					// can be a single dust with an "s" or not
					long frequency;
					try {
						frequency = Long.valueOf(t);
					} catch (Exception e) {
						if (t.contains("s")) {
							t = t.replace("s", "");
							frequency = Long.valueOf(t);
							powder.addDust(new Dust(powderParticle, radius, height, frequency, true));
							continue;
						}
						frequency = 20;
					}
					powder.addDust(new Dust(powderParticle, radius, height, frequency, false));
				}

				int tick = 0;
				int left = 0;
				int up = 0;
				ParticleMatrix particleMatrix = new ParticleMatrix();
				Layer layer = new Layer();

				// read matrices/maps
				for (String t : (List<String>) powderConfig.getList(powders + s + ".map", new ArrayList<String>())) {
					// read animation time; animation time separates each ParticleMatrix
					if (t.contains("[")) {
						t = t.replace("[", "").replace("]", "");
						try {
							tick = Integer.valueOf(t);
						} catch (Exception e) {
							getLogger().warning("Invalid animation time at line " + 
									(powderConfig.getList(powders + s + ".map").indexOf(t) + 1));
							continue;
						}
						if (!(layer.getRows().isEmpty())) {
							particleMatrix.addLayer(layer);
						}
						if (particleMatrix.getLayers().isEmpty()) {
							particleMatrix.setTick(tick);
							continue;
						}
						powder.addMatrix(particleMatrix);
						// start reading a new ParticleMatrix & Layer
						particleMatrix = new ParticleMatrix();
						particleMatrix.setTick(tick);
						layer = new Layer();
						continue;
					}
					// read each Layer of the matrix; the position of the Layer separates each Layer
					if (t.contains("{")) {
						t = t.replace("{", "").replace("}", "");
						int position;
						try {
							position = Integer.valueOf(t);
						} catch (Exception e) {
							getLogger().warning("Invalid position of layer in Powder '" + powder.getName() + "' at line " +
									(powderConfig.getList(powders + s + ".map").indexOf(t) + 1) + "in " + powderConfig.getName());
							continue;
						}
						up = 0;
						if (layer.getRows().isEmpty()) {
							layer.setPosition(position);
							continue;
						}
						// start reading a new Layer if the previous Layer isn't empty
						particleMatrix.addLayer(layer);
						layer = new Layer();
						layer.setPosition(position);
						continue;
					}
					if (t.contains(":")) {

						// spacing for each matrix; if it doesn't exist, is set to given default for the Powder
						if (t.contains("spacing:")) {
							t = t.replace("spacing:", "");
							float spacing = Float.valueOf(t);
							particleMatrix.setSpacing(spacing);
							// read an image from URL/path
						} else if (t.contains("img:")) {
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
							if (particleMatrix.getTick() == 0) {
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

				// if it finished going through the rows and there's some left that aren't added to the matrix
				if (!(layer.getRows().isEmpty()) || !(particleMatrix.getLayers().contains(layer))) {
					particleMatrix.addLayer(layer);
				}

				// if the matrix is finished and hasn't been added to the Powder
				if (!(particleMatrix.getLayers().isEmpty())) {
					powder.addMatrix(particleMatrix);
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
