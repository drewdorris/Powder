package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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
			for (PowderElement element : powderTask.getActiveElements().keySet()) {
				Integer lastOccurrence = powderTask.getActiveElements().get(element);
				PowderPlugin.getInstance().getLogger().info(String.valueOf(lastOccurrence));
				PowderPlugin.getInstance().getLogger().info("|     " + String.valueOf(tick));
				if (lastOccurrence + element.getRepeatTime() <= tick) {
					if (element.getIterations() >= element.getLockedIterations()) {
						elementsToRemove.add(element);
						continue;
					}
					createElement(element, powderTask);
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
	
	public void createElement(PowderElement element, PowderTask powderTask) {
		element.create(Bukkit.getPlayer(powderTask.getPlayerUUID()).getEyeLocation());
		element.iterate();
		powderTask.getActiveElements().put(element, tick);
	}
	
	public static Integer getTick() {
		return tick;
	}

}