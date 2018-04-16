package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.Powder;
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
			if (powderTask.getPowders().isEmpty()) {
				powderTasksToRemove.add(powderTask);
				continue;
			}
			List<Powder> powdersToRemove = new ArrayList<Powder>();
			for (Powder powder : powderTask.getPowders().keySet()) {
				if (powder.getPowderElements().isEmpty()) {
					powderTasksToRemove.add(powderTask);
					continue;
				}
				List<PowderElement> elementsToRemove = new ArrayList<PowderElement>();
				for (PowderElement element : powder.getPowderElements().keySet()) {
					if (powder.getPowderElements().get(element) + element.getRepeatTime() <= tick) {
						if (element.getIterations() >= element.getLockedIterations()) {
							elementsToRemove.add(element);
							continue;
						}
						element.create(powderTask.getPowders().get(powder));
						element.iterate();
						powder.getPowderElements().put(element, tick);
					}
				}
				for (PowderElement element : elementsToRemove) {
					powder.removePowderElement(element);
				}
			}
			for (Powder powder : powdersToRemove) {
				powderTask.removePowder(powder);
			}
		}
		for (PowderTask powderTask : powderTasksToRemove) {
			powderHandler.removePowderTask(powderTask);
		}
	}

	public static int getTick() {
		return tick;
	}

}