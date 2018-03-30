package com.ruinscraft.powder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.powder.util.PowderUtil;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		Bukkit.getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
			PowderUtil.unloadPlayer(player);
		});		
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		Bukkit.getServer().getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
			PowderUtil.loadPlayer(player);
		});
	}

}
