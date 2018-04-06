package com.ruinscraft.powder;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;

public class PowdersCreationTask extends BukkitRunnable {

	private PowderHandler powderHandler;

	public PowdersCreationTask() {
		powderHandler = PowderPlugin.getInstance().getPowderHandler();
	}

	@Override
	public void run() {
		if (powderHandler == null) {
			cancel();
		}
		
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			for (PowderElement element : powderTask.getActiveElements().keySet()) {
				Long lastOccurrence = powderTask.getActiveElements().get(element);
				if (System.currentTimeMillis() - (lastOccurrence + (element.getRepeatTime() * 50)) >= -10) {
					element.create(Bukkit.getPlayer(powderTask.getPlayerUUID()).getLocation());
					powderTask.getActiveElements().put(element, System.currentTimeMillis());
				}
			}
		}
	}

}