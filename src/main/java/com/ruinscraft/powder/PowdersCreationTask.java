package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

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
		
		List<PowderTask> powderTasksToRemove = new ArrayList<PowderTask>();
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			if (powderTask.getActiveElements().isEmpty()) {
				powderTasksToRemove.add(powderTask);
				continue;
			}
			List<PowderElement> elementsToRemove = new ArrayList<PowderElement>();
			for (PowderElement element : powderTask.getActiveElements().keySet()) {
				Long lastOccurrence = powderTask.getActiveElements().get(element);
				if (System.currentTimeMillis() - (lastOccurrence + (element.getRepeatTime() * 50)) >= -10) {
					element.create(Bukkit.getPlayer(powderTask.getPlayerUUID()).getLocation());
					if (element.getIterations() >= element.getLockedIterations()) {
						elementsToRemove.add(element);
						continue;
					}
					powderTask.getActiveElements().put(element, System.currentTimeMillis());
				}
			}
			for (PowderElement element : elementsToRemove) {
				powderTask.removeElement(element);
			}
		}
		powderHandler.getPowderTasks().removeAll(powderTasksToRemove);
	}

}