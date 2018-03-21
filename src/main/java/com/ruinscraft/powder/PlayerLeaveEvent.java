package com.ruinscraft.powder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.powder.objects.PowderTask;

public class PlayerLeaveEvent implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerQuitEvent event) {
		PowderHandler powderHandler = Powder.getInstance().getPowderHandler();
		for (PowderTask powderTask : powderHandler.getPowderTasks(event.getPlayer())) {
			powderHandler.removePowderTask(powderTask);
		}
	}

}
