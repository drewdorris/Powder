package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.TrackerType;
import com.ruinscraft.powder.models.Powder;

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
			List<Powder> powdersToRemove = new ArrayList<Powder>();
			for (Powder powder : powderTask.getPowders().keySet()) {
				if (powderTask.getPowders().get(powder).getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) powderTask.getPowders().get(powder);
					if (entityTracker.getEntity() == null || entityTracker.getEntity().isDead()) {
						powdersToRemove.add(powder);
						continue;
					}
				}
				if (powder.getPowderElements().isEmpty()) {
					powdersToRemove.add(powder);
					continue;
				}
				Location location = powderTask.getPowders().get(powder).getCurrentLocation();
				List<PowderElement> elementsToRemove = new ArrayList<PowderElement>();
				for (PowderElement element : powder.getPowderElements().keySet()) {
					if (powder.getPowderElements().get(element) + element.getRepeatTime() <= tick) {
						if (element.getIterations() >= element.getLockedIterations()) {
							elementsToRemove.add(element);
							continue;
						}
						element.create(location);
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