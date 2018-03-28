package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.powder.tasks.LoadPlayerFromDatabaseTask;
import com.ruinscraft.powder.tasks.PowderTask;
import com.ruinscraft.powder.tasks.SavePlayerToDatabaseTask;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();

		List<String> powdersToSave = new ArrayList<>();

		for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
			if (powderTask.getMap().isRepeating()) {
				if (!powderTask.getMap().getDusts().isEmpty()) {
					powdersToSave.add(powderTask.getMap().getName());
				}
			}

			powderHandler.removePowderTask(powderTask);
		}

		if (PowderPlugin.getInstance().useStorage()) {
			PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(
					PowderPlugin.getInstance(),
					new SavePlayerToDatabaseTask(player.getUniqueId(), powdersToSave)
					);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (PowderPlugin.getInstance().useStorage()) {
			PowderPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(
					PowderPlugin.getInstance(), 
					new LoadPlayerFromDatabaseTask(player.getUniqueId())
					);
		}
	}

}
