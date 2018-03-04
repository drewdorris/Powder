package com.ruinscraft.powder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.ChatColor;

public class Powder extends JavaPlugin {
	
	private static Powder instance;
	private PowderHandler powderHandler;
	
	public static String PREFIX;
	
	public static Powder getInstance() {
		return instance;
	}
	
	public void onEnable() {
		
		instance = this;
		
		File config = new File(getDataFolder(), "config.yml");
		if (!config.exists()) {
		    getLogger().info("config.yml not found, creating!");
		    saveDefaultConfig();
		}
		
		handleConfig();
		
		getCommand("powder").setExecutor(new PowderCommand());
		
		getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
		
		PREFIX = color(getConfig().getString("prefix"));
		
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
		
	}
	
	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public void handleConfig() {
		
		if (!(powderHandler == null)) {
			powderHandler.clearAllTasks();
		}
		powderHandler = new PowderHandler();
		
		List<String> powderNames = new ArrayList<>();
		
		String powders = "powders.";
		
	    for (String s : getConfig().getConfigurationSection("powders").getKeys(false)) {
	    	
			if (Bukkit.getPluginManager().getPermission("powder.powder." + s) == null) {
				Bukkit.getPluginManager().addPermission(new Permission("powder.powder." + s));
			}
			
			float spacing = (float) getConfig().getDouble(powders + s + ".spacing");
			String name = getConfig().getString(powders + s + ".name");
			boolean ptch = getConfig().getBoolean(powders + s + ".pitch");
			boolean repeating = getConfig().getBoolean(powders + s + ".repeating");
			long delay = getConfig().getLong(powders + s + ".delay");
			List<SoundEffect> sounds = new ArrayList<SoundEffect>();
			
			for (String t : getConfig().getStringList(powders + s + ".sounds")) {
				
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
			
			StringBuilder sb = new StringBuilder();
			boolean main = false;
			boolean alreadyDone = false;
			int left = 0;
			int up = 0;
			List<String> smaps = new ArrayList<String>();
			
			for (String t : getConfig().getStringList(powders + s + ".map")) {
				
				if (t.contains("[")) {
					t = t.replace("[", "").replace("]", "");
					if (sb.length() == 0) {
						continue;
					}
					String smap = sb.toString();
					smaps.add(smap);
					sb.setLength(0);
					sb.append(t).append(";");
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
				if (t.contains("?")) {
					left = (t.indexOf("?"));
					main = false;
				}
				sb.append(t).append(";");
				
			}
			
			if (!(sb.length() == 0)) {
				smaps.add(sb.toString());
			}
			
			powderNames.add(name);
			PowderMap pmap = new PowderMap(name, left + 1, up, spacing, 
									smaps, sounds, ptch, repeating, delay);
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
