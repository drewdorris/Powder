package com.ruinscraft.powder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
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

import net.md_5.bungee.api.ChatColor;

public class PowderPlugin extends JavaPlugin {

	private static PowderPlugin instance;
	private static PowderHandler powderHandler;

	public static String PREFIX;

	private FileConfiguration config = null;

	public static PowderPlugin getInstance() {
		return instance;
	}

	public void onEnable() {

		instance = this;

		handleConfig();

		getCommand("powder").setExecutor(new PowderCommand());

		getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);

		PREFIX = color(config.getString("prefix"));

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

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public void cleanHandlers() {
		// put other handlers if those exist in the future
		if (!(powderHandler == null)) {
			powderHandler.clearEverything();
		}
		powderHandler = new PowderHandler();
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

		cleanHandlers();

		List<String> powderNames = new ArrayList<>();

		String powders = "powders.";

		for (String s : config.getConfigurationSection("powders").getKeys(false)) {

			Powder powder = new Powder();

			powder.setName(config.getString(powders + s + ".name", null));
			powder.setDefaultSpacing((float) config.getDouble(powders + s + ".spacing", .5F));
			powder.setPitch(config.getBoolean(powders + s + ".pitch", false));
			powder.setRepeating(config.getBoolean(powders + s + ".repeating", false));
			powder.setHidden(config.getBoolean(powders + s + ".hidden", false));
			powder.setDelay(config.getLong(powders + s + ".delay", Long.MAX_VALUE));
			powder.setSoundEffects(new ArrayList<SoundEffect>());
			powder.setDusts(new ArrayList<Dust>());
			powder.setPowderParticles(new ArrayList<PowderParticle>());

			for (String t : (List<String>) config.getList(powders + s + ".sounds", new ArrayList<String>())) {

				Sound sound;
				String soundName;
				soundName = t.substring(0, t.indexOf(";"));
				sound = Sound.valueOf(soundName);
				if ((Sound.valueOf(soundName) == null)) {
					getLogger().warning("Invalid sound name '" + soundName + 
							"' for '" + powder.getName() + "' in config.yml!");
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

			for (String t : (List<String>) config.getList(powders + s + ".dusts", new ArrayList<String>())) {

				String dustName = t.substring(0, t.indexOf(";"));
				t = t.replaceFirst(dustName + ";", "");
				double radius = Double.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				double height = Float.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				long frequency = Long.valueOf(t);
				powder.addDust(new Dust(dustName, radius, height, frequency));

			}

			for (String t : (List<String>) config.getList(powders + s + ".changes", new ArrayList<String>())) {

				String enumName = t.substring(0, t.indexOf(";"));
				t = t.replaceFirst(enumName + ";", "");
				String particleName = t.substring(0, t.indexOf(";"));
				Particle particle = Particle.valueOf(particleName);
				if (particle == null) {
					getLogger().warning("Invalid particle name '" + particleName + 
							"' for " + powder.getName() + " in config.yml!");
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

			boolean definingPlayerLocation = false;
			boolean playerLocationDefined = false;
			int tick = 0;
			int left = 0;
			int up = 0;
			List<Layer> layers = new ArrayList<Layer>();
			Layer layer = new Layer();

			for (String t : (List<String>) config.getList(powders + s + ".map", new ArrayList<String>())) {

				if (t.contains("[")) {
					t = t.replace("[", "").replace("]", "");
					if (layers.isEmpty()) {
						continue;
					}
					layers.add(layer);
					powder.addMatrix(new ParticleMatrix(layers, tick, left + 1, up, 0));
					layers = new ArrayList<Layer>();
					layer = new Layer();
					playerLocationDefined = true;
					try {
						tick = Integer.valueOf(t);
					} catch (Exception e) {
						getLogger().warning("Invalid animation time at line " + 
								(config.getList(powders + s + ".map").indexOf(t) + 1));
					}
					continue;
				}
				if (t.contains("{")) {
					t = t.replace("{", "").replace("}", "");
					int position;
					try {
						position = Integer.valueOf(t);
					} catch (Exception e) {
						getLogger().warning("Invalid position of layer in Powder '" + powder.getName() + "' at line " +
								(config.getList(powders + s + ".map").indexOf(t) + 1));
						continue;
					}
					if (position == 0) {
						if (playerLocationDefined == false) {
							definingPlayerLocation = true;
						}
					}
					if (layer.getRows().isEmpty()) {
						continue;
					}
					layers.add(layer);
					layer = new Layer();
					layer.setPosition(position);
					continue;
				}
				if (definingPlayerLocation == true) {
					up++;
				}
				if (t.contains(":")) {

					if (t.contains("spacing:")) {
						t = t.replace("spacing:", "");
						float spacing = Float.valueOf(t);
						layer.setSpacing(spacing);
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
						URL url;
						try {
							url = new URL(urlName);
							ImageUtil.getRowsFromURL(layer.getRows(), url, width, height);
						} catch (MalformedURLException e) {
							getLogger().warning("Path unclear: '" + urlName + "'");
							continue;
						} catch (IOException io) {
							getLogger().warning("Failed to connect to URL '" + urlName + "'");
							continue;
						} catch (Exception e) {
							continue;
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
							continue;
						} catch (IOException io) {
							getLogger().warning("Failed to load path: '" + path + "'");
							continue;
						}
						up = up + height;
					}

					up--;

					continue;

				}
				if (t.contains("?") && definingPlayerLocation == true) {
					left = (t.indexOf("?"));
					definingPlayerLocation = false;
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

			if (!(layer.getRows().isEmpty()) || !(layers.contains(layer))) {
				layers.add(layer);
			}

			if (!(layers.isEmpty())) {
				powder.addMatrix(new ParticleMatrix(layers, tick, left + 1, up, 0));
			}

			if (powder.getMatrices().isEmpty() && powder.getSoundEffects().isEmpty() && powder.getDusts().isEmpty()) {
				getLogger().warning("Powder '" + powder.getName() + "' appears empty and was not loaded.");
				continue;
			}
			powderNames.add(powder.getName());
			getPowderHandler().addPowder(powder);

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

	public PowderHandler getPowderHandler() {
		return powderHandler;
	}

}
