package com.ruinscraft.powder;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.powder.tasks.LoadPlayerFromDatabaseTask;
import com.ruinscraft.powder.tasks.PowderTask;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		
		for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
			powderHandler.removePowderTask(powderTask);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(
				PowderPlugin.getInstance(), 
				new LoadPlayerFromDatabaseTask(player.getUniqueId())
				);
	}

}
