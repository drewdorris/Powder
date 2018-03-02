package com.ruinscraft.particle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.particle.commands.PrtCommand;
import com.ruinscraft.particle.events.PlayerLeaveEvent;
import com.ruinscraft.particle.objects.ParticleMap;
import com.ruinscraft.particle.objects.SoundEffect;

public class RCParticle extends JavaPlugin {
	
	private static RCParticle instance;
	private ParticleHandler phandler;
	
	public static RCParticle getInstance() {
		return instance;
	}
	
	public void onEnable() {
		
		instance = this;
		
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
		    getLogger().info("config.yml not found, creating!");
		    saveDefaultConfig();
		}
		
		handleStuff();
		
		getCommand("prt").setExecutor(new PrtCommand());
		
		getServer().getPluginManager().registerEvents(new PlayerLeaveEvent(), this);
		
	}
	
	public void onDisable() {
		
		instance = null;
		
	}
	
	public void handleStuff() {
		
		if (!(phandler == null)) {
			phandler.clearAllTasks();
		}
		phandler = new ParticleHandler();
		
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
					getLogger().warning("Invalid sound name '" + soundName + "' for " + name + " in config.yml!");
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
				if (t.contains("P")) {
					left = (t.indexOf("P"));
					main = false;
				}
				sb.append(t).append(";");
				
			}
			
			if (!(sb.length() == 0)) {
				smaps.add(sb.toString());
			}
			
			getLogger().info(name);
			ParticleMap pmap = new ParticleMap(name, left + 1, up, spacing, 
									smaps, sounds, ptch, repeating, delay);
			getParticleHandler().addParticleMap(pmap);
			
			/*
			StringBuilder sb = new StringBuilder();
			boolean main = false;
			int left = 0;
			int up = 0;
			
			for (String t : getConfig().getStringList("effects." + s + ".map")) {
				
				if (main == true) {
					up++;
				}
				if (t.equals("{0}")) {
					main = true;
				}
				if (t.contains("P")) {
					left = (t.indexOf("P"));
					main = false;
				}
				sb.append(t).append(";");
				
			}
			
			ParticleMap pmap = new ParticleMap(name, left + 1, up, spacing, sb.toString(), sounds, ptch);
			getParticleHandler().addParticleMap(pmap);
			/*/
			
			/*/

			 */
	    	
	    }
		
	}
	
	public ParticleHandler getParticleHandler() {
		return phandler;
	}
	
}
