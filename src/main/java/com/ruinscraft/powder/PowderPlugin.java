package com.ruinscraft.powder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.ruinscraft.powder.objects.PowderParticle;
import com.ruinscraft.powder.objects.Dust;
import com.ruinscraft.powder.objects.Layer;
import com.ruinscraft.powder.objects.ParticleMatrix;
import com.ruinscraft.powder.objects.ParticleName;
import com.ruinscraft.powder.objects.Powder;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.SoundEffect;

public class PowderPlugin extends JavaPlugin {

	private static PowderPlugin instance;
	private static PowderHandler powderHandler;

	public static String PREFIX;

	private FileConfiguration config = null;
	private List<FileConfiguration> powderConfigs = null;

	public static PowderPlugin getInstance() {
		return instance;
	}

	public void onEnable() {

		instance = this;

		handleConfig();

		getCommand("powder").setExecutor(new PowderCommand());

		getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);

		PREFIX = PowderUtil.color(config.getString("prefix"));

		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
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

		instance = null;
		powderHandler.clearEverything();

	}

	public void cleanHandlers() {
		// put other handlers if those exist in the future
		if (!(powderHandler == null)) {
			powderHandler.clearEverything();
		}
		powderHandler = new PowderHandler();
	}

	public List<FileConfiguration> getPowderConfigs() {
		return powderConfigs;
	}
	
	public void loadPowderConfigs() {

		powderConfigs = new ArrayList<FileConfiguration>();

		BufferedReader reader;

		for (String urlName : config.getStringList("powderSources")) {

			FileConfiguration powderConfig;
			URL url = PowderUtil.readURL(urlName);
			File file;
			if (!urlName.contains("/")) {

				file = new File(getDataFolder(), urlName);
				if (!file.exists()) {
					getLogger().warning("Failed to load config file '" + urlName + "'.");
					continue;
				}
				powderConfig = YamlConfiguration.loadConfiguration(file);

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

	}

	@SuppressWarnings("unchecked")
	public void handleConfig() {

		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			getLogger().info("config.yml not found, creating!");
			saveDefaultConfig();
		}
		reloadConfig();
		config = getConfig();

		loadPowderConfigs();
		File defaultPowderConfig = new File(getDataFolder(), "powders.yml");
		if (!defaultPowderConfig.exists() && config.getStringList("powderSources").contains("powders.yml")) {
			getLogger().info("powders.yml not found but listed as a source, creating!");
			saveResource("powders.yml", false);
			FileConfiguration powderConfig = YamlConfiguration.loadConfiguration(defaultPowderConfig);
			powderConfigs.add(powderConfig);
		}

		cleanHandlers();

		PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {

				boolean categoriesEnabled = config.getBoolean("categoriesEnabled", false);
				powderHandler.setIfCategoriesEnabled(categoriesEnabled);
				if (categoriesEnabled) {
					for (String s : config.getConfigurationSection("categories").getKeys(false)) {
						powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
					}
					if (!powderHandler.getCategories().keySet().contains("Other")) {
						powderHandler.addCategory("Other", "Unsorted Powders");
					}
				}

				List<String> powderNames = new ArrayList<>();

				String powders = "powders.";

				for (FileConfiguration powderConfig : powderConfigs) {

					for (String s : powderConfig.getConfigurationSection("powders").getKeys(false)) {

						Powder powder = new Powder();

						powder.setName(powderConfig.getString(powders + s + ".name", null));
						powder.setDefaultSpacing((float) powderConfig.getDouble(powders + s + ".spacing", .5F));
						powder.setPitch(powderConfig.getBoolean(powders + s + ".pitch", false));
						powder.setRepeating(powderConfig.getBoolean(powders + s + ".repeating", false));
						powder.setHidden(powderConfig.getBoolean(powders + s + ".hidden", false));
						powder.setDelay(powderConfig.getLong(powders + s + ".delay", Long.MAX_VALUE));
						powder.setSoundEffects(new ArrayList<SoundEffect>());
						powder.setDusts(new ArrayList<Dust>());
						powder.setPowderParticles(new ArrayList<PowderParticle>());

						if (categoriesEnabled) {

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

						for (String t : (List<String>) powderConfig.getList(powders + s + ".sounds", new ArrayList<String>())) {

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
							t = t.substring(t.indexOf(";") + 1, t.length());
							float waitTime = Float.valueOf(t);
							powder.addSoundEffect(new SoundEffect(sound, volume, soundPitch, waitTime));

						}

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

						for (String t : (List<String>) powderConfig.getList(powders + s + ".dusts", new ArrayList<String>())) {

							String dustName = t.substring(0, t.indexOf(";"));
							PowderParticle powderParticle;
							powderParticle = powder.getPowderParticle(dustName);
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

						for (String t : (List<String>) powderConfig.getList(powders + s + ".map", new ArrayList<String>())) {

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
								particleMatrix = new ParticleMatrix();
								particleMatrix.setTick(tick);
								layer = new Layer();
								continue;
							}
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
								particleMatrix.addLayer(layer);
								layer = new Layer();
								layer.setPosition(position);
								continue;
							}
							if (t.contains(":")) {

								if (t.contains("spacing:")) {
									t = t.replace("spacing:", "");
									float spacing = Float.valueOf(t);
									particleMatrix.setSpacing(spacing);
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
									URL url = PowderUtil.readURL(urlName);
									if (url == null) {
										continue;
									} else {
										try {
											ImageUtil.getRowsFromURL(layer.getRows(), url, width, height);
										} catch (IOException io) {
											getLogger().warning("Failed to load URL: '" + urlName + "'");
											continue;
										}
									}
									up = up + height;
								} else if (t.contains("path:")) {
									String path;
									int width;
									int height;
									t = t.replace("path:", "");
									path = t.substring(0, t.indexOf(";"));
									t = t.substring(t.indexOf(";") + 1, t.length());
									width = Integer.valueOf(t.substring(0, t.indexOf(";")));
									t = t.substring(t.indexOf(";") + 1, t.length());
									height = Integer.valueOf(t);
									try {
										ImageUtil.getRowsFromPath(layer.getRows(), path, width, height);
									} catch (MalformedURLException e) {
										getLogger().warning("Unclear path: '" + path + "'");
										continue;
									} catch (IOException io) {
										getLogger().warning("Failed to load path: '" + path + "'");
										continue;
									}
									up = up + height;
								}

								continue;

							}
							if (layer.getPosition() == 0) {
								up++;
								if (t.contains("?")) {
									left = (t.indexOf("?")) + 1;
									if (particleMatrix.getTick() == 0) {
										powder.setDefaultLeft(left);
										powder.setDefaultUp(up);
									}
									particleMatrix.setPlayerLeft(left);
									particleMatrix.setPlayerUp(up);
								}
							}
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

						if (!(layer.getRows().isEmpty()) || !(particleMatrix.getLayers().contains(layer))) {
							particleMatrix.addLayer(layer);
						}

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

				// do this again to load {total}
				if (categoriesEnabled) {
					for (String s : config.getConfigurationSection("categories").getKeys(false)) {
						powderHandler.addCategory(s, config.getString("categories." + s + ".desc", ""));
					}
					if (!powderHandler.getCategories().keySet().contains("Other")) {
						powderHandler.addCategory("Other", "Unsorted Powders");
					}
				}

				StringBuilder msg = new StringBuilder();
				msg.append("Loaded Powders: ");
				for (String effect : powderNames) {
					if (powderNames.get(powderNames.size() - 1).equals(effect)) {
						msg.append(effect + ". " + powderNames.size() + " total!");
					} else {
						msg.append(effect + ", ");
					}
				}
				getLogger().info(msg.toString());

			}

		});

	}

	public PowderHandler getPowderHandler() {
		return powderHandler;
	}

}
