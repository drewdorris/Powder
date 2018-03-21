package com.ruinscraft.powder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.ruinscraft.powder.objects.ChangedParticle;
import com.ruinscraft.powder.objects.Dust;
import com.ruinscraft.powder.objects.ParticleMap;
import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.ChatColor;

public class Powder extends JavaPlugin {
	
	private static Powder instance;
	private static PowderHandler powderHandler;
	
	public static String PREFIX;
	
	private FileConfiguration config = null;
	
	public static Powder getInstance() {
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
			
			String name = config.getString(powders + s + ".name", null);
			float spacing = (float) config.getDouble(powders + s + ".spacing", .5F);
			boolean ptch = config.getBoolean(powders + s + ".pitch", false);
			boolean repeating = config.getBoolean(powders + s + ".repeating", false);
			boolean hidden = config.getBoolean(powders + s + ".hidden", false);
			long delay = config.getLong(powders + s + ".delay", Long.MAX_VALUE);
			List<SoundEffect> sounds = new ArrayList<SoundEffect>();
			List<Dust> dusts = new ArrayList<Dust>();
			List<ChangedParticle> changedParticles = new ArrayList<ChangedParticle>();
			
			for (String t : (List<String>) config.getList(powders + s + ".sounds", new ArrayList<String>())) {
				
				Sound sound;
				String soundName;
				soundName = t.substring(0, t.indexOf(";"));
				sound = Sound.valueOf(soundName);
				if ((Sound.valueOf(soundName) == null)) {
					getLogger().warning("Invalid sound name '" + soundName + 
							"' for " + name + " in config.yml!");
					continue;
				}
				t = t.replaceFirst(soundName + ";", "");
				float volume = Float.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				float pitch = Float.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				float waitTime = Float.valueOf(t);
				SoundEffect se = new SoundEffect(sound, volume, pitch, waitTime);
				sounds.add(se);
				
			}
			
			for (String t : (List<String>) config.getList(powders + s + ".dusts", new ArrayList<String>())) {
				
				String dustName = t.substring(0, t.indexOf(";"));
				t = t.replaceFirst(dustName + ";", "");
				double radius = Double.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				double height = Float.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				long frequency = Long.valueOf(t);
				Dust dust = new Dust(dustName, radius, height, frequency);
				dusts.add(dust);
				
			}
			
			for (String t : (List<String>) config.getList(powders + s + ".changes", new ArrayList<String>())) {
				
				String enumName = t.substring(0, t.indexOf(";"));
				t = t.replaceFirst(enumName + ";", "");
				String particleName = t.substring(0, t.indexOf(";"));
				Particle particle = Particle.valueOf(particleName);
				if (particle == null) {
					getLogger().warning("Invalid particle name '" + particleName + 
							"' for " + name + " in config.yml!");
					continue;
				}
				t = t.substring(t.indexOf(";") + 1, t.length());
				ChangedParticle changedParticle;
				double xOff;
				double yOff;
				double zOff;
				try {
					xOff = Double.valueOf(t.substring(0, t.indexOf(";")));
				} catch (Exception e) {
					changedParticle = new ChangedParticle(enumName, particle, 0, 0, 0);
					changedParticles.add(changedParticle);
					continue;
				}
				t = t.substring(t.indexOf(";") + 1, t.length());
				yOff = Double.valueOf(t.substring(0, t.indexOf(";")));
				t = t.substring(t.indexOf(";") + 1, t.length());
				try {
					zOff = Double.valueOf(t);
					changedParticle = new ChangedParticle(enumName, particle, xOff, yOff, zOff);
				} catch (Exception e) {
					zOff = Double.valueOf(t.substring(0, t.indexOf(";")));
					t = t.substring(t.indexOf(";") + 1, t.length());
					Object data;
					try {
						data = Double.valueOf(t);
					} catch (Exception ex) {
						data = t;
					}
					changedParticle = new ChangedParticle(enumName, particle, xOff, yOff, zOff, data);
				}
				changedParticles.add(changedParticle);
				
			}
			
			List<Object> map = new ArrayList<Object>();
			boolean main = false;
			boolean alreadyDone = false;
			int tick = 0;
			int left = 0;
			int up = 0;
			List<ParticleMap> particleMaps = new ArrayList<ParticleMap>();
			
			for (String t : (List<String>) config.getList(powders + s + ".map", new ArrayList<String>())) {
				
				if (t.contains("[")) {
					t = t.replace("[", "").replace("]", "");
					if (map.isEmpty()) {
						continue;
					}
					try {
						tick = Integer.valueOf(t);
					} catch (Exception e) {
						getLogger().warning("Invalid animation time at line " + 
								(config.getList(powders + s + ".map").indexOf(t) + 1));
					}
					ParticleMap particleMap = new ParticleMap(map, tick, left + 1, up, 0);
					particleMaps.add(particleMap);
					map = new ArrayList<Object>();
					for (char ch : t.toCharArray()) {
						map.add(String.valueOf(ch));
					}
					map.add(";");
					alreadyDone = true;
					continue;
				}
				if (main == true) {
					up++;
				}
				if (t.contains("{0}")) {
					main = true;
					if (alreadyDone == true) {
						main = false;
					}
				}
				if (t.contains("?") && main == true) {
					left = (t.indexOf("?"));
					main = false;
				}
				for (char ch : t.toCharArray()) {
					map.add(String.valueOf(ch));
				}
				map.add(";");
				
			}
			
			if (!(map.isEmpty())) {
				ParticleMap particleMap = new ParticleMap(map, tick, left + 1, up, 0);
				particleMaps.add(particleMap);
			}
			
			if (particleMaps.isEmpty() && sounds.isEmpty() && dusts.isEmpty()) {
				getLogger().warning("Powder " + name + " appears empty and was not loaded.");
				continue;
			}
			powderNames.add(name);
			final PowderMap pmap = new PowderMap(name, spacing, particleMaps, sounds, 
					dusts, changedParticles, ptch, repeating, hidden, delay);
			getPowderHandler().addPowderMap(pmap);
	    	
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
