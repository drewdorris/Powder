package com.ruinscraft.powder;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.powder.objects.PowderTask;

public class PlayerLeaveEvent implements Listener {
	
	PowderHandler phandler = Powder.getInstance().getPowderHandler();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerQuitEvent event) {
		for (PowderTask ptask : phandler.getPowderTasks(event.getPlayer())) {
			phandler.removePowderTask(ptask);
		}
	}
	
}
