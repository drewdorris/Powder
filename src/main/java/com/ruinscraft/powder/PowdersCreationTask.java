package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;

public class PowdersCreationTask extends BukkitRunnable {

	private PowderHandler powderHandler;
	private static int tick;

	public PowdersCreationTask() {
		powderHandler = PowderPlugin.getInstance().getPowderHandler();
		tick = 0;
	}

	@Override
	public void run() {
		if (powderHandler.getPowderTasks().isEmpty()) {
			tick = 0;
			cancel();
		}
		tick++;
		List<PowderTask> powderTasksToRemove = new ArrayList<PowderTask>();
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			if (powderTask.getActiveElements().isEmpty()) {
				powderTasksToRemove.add(powderTask);
				continue;
			}
			List<PowderElement> elementsToRemove = new ArrayList<PowderElement>();
			Location location;
			if (powderTask.getPlayerUUID() == null) {
				location = powderTask.getLocation();
			} else {
				location = Bukkit.getPlayer(powderTask.getPlayerUUID()).getEyeLocation();
			}
			for (PowderElement element : powderTask.getActiveElements().keySet()) {
				if (powderTask.getActiveElements().get(element) + element.getRepeatTime() <= tick) {
					if (element.getIterations() >= element.getLockedIterations()) {
						elementsToRemove.add(element);
						continue;
					}
					element.create(location);
					element.iterate();
					powderTask.getActiveElements().put(element, tick);
				}
			}
			for (PowderElement element : elementsToRemove) {
				powderTask.removeElement(element);
			}
		}
		for (PowderTask powderTask : powderTasksToRemove) {
			powderHandler.removePowderTask(powderTask);
		}
	}

	public static Integer getTick() {
		return tick;
	}

}