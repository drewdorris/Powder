package com.ruinscraft.powder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.ChatColor;

public class Powder extends JavaPlugin {
	
	private static Powder instance;
	private PowderHandler phandler;
	
	public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + 
											"Powder" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
	
	public static Powder getInstance() {
		return instance;
	}
	
	public void onEnable() {
		
		instance = this;
		
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
		    getLogger().info("config.yml not found, creating!");
		    saveDefaultConfig();
		}
		
		handleConfig();
		
		getCommand("powder").setExecutor(new PowderCommand());
		
		getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
		
	}
	
	public void onDisable() {
		
		instance = null;
		
	}
	
	public void handleConfig() {
		
		if (!(phandler == null)) {
			phandler.clearAllTasks();
		}
		phandler = new PowderHandler();
		
		List<String> effects = new ArrayList<>();
		
	    for (String s : getConfig().getConfigurationSection("effects").getKeys(false)) {
	    	
			if (Bukkit.getPluginManager().getPermission("rcp.effect." + s) == null) {
				Bukkit.getPluginManager().addPermission(new Permission("rcp.effect." + s));
			}
			
			float spacing = (float) getConfig().getDouble("effects." + s + ".spacing");
			String name = getConfig().getString("effects." + s + ".name");
			boolean ptch = getConfig().getBoolean("effects." + s + ".pitch");
			boolean repeating = getConfig().getBoolean("effects." + s + ".repeating");
			long delay = getConfig().getLong("effects." + s + ".delay");
			List<SoundEffect> sounds = new ArrayList<SoundEffect>();
			
			for (String t : getConfig().getStringList("effects." + s + ".sounds")) {
				
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
			
			for (String t : getConfig().getStringList("effects." + s + ".map")) {
				
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
			
			effects.add(name);
			PowderMap pmap = new PowderMap(name, left + 1, up, spacing, 
									smaps, sounds, ptch, repeating, delay);
			getPowderHandler().addPowderMap(pmap);
	    	
	    }
	    
	    StringBuilder msg = new StringBuilder();
	    msg.append("Loaded effects: ");
	    for (String effect : effects) {
	    	if (effects.get(effects.size() - 1).equals(effect)) {
	    		msg.append(effect + ". " + effects.size() + " total!");
	    	} else {
	    		msg.append(effect + ", ");
	    	}
	    }
	    getLogger().info(msg.toString());
		
	}
	
	public PowderHandler getPowderHandler() {
		return phandler;
	}
	
}
