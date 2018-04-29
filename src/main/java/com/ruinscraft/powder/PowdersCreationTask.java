package com.ruinscraft.powder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.Tracker;
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
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			Set<Entry<Powder, Tracker>> activePowdersInTask = powderTask.getPowders().entrySet();
			for (Entry<Powder, Tracker> activePowder : activePowdersInTask) {
				Powder powder = activePowder.getKey();
				Tracker tracker = activePowder.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getEntity() == null 
							|| entityTracker.getEntity().isDead()) {
						activePowdersInTask.remove(activePowder);
						continue;
					}
				}
				Map<PowderElement, Integer> activeElementsInPowder = powder.getPowderElements();
				if (activeElementsInPowder.isEmpty()) {
					activePowdersInTask.remove(activePowder);
					continue;
				}
				Set<PowderElement> elementsDueForCheck = activeElementsInPowder.entrySet().stream()
						.filter(e -> e.getValue() <= tick).map(Entry::getKey)
						.collect(Collectors.toSet());
				for (PowderElement dueElement : elementsDueForCheck) {
					if (dueElement.getIterations() >= dueElement.getLockedIterations()) {
						activeElementsInPowder.remove(dueElement);
						continue;
					}
					dueElement.create(tracker.getCurrentLocation());
					dueElement.iterate();
					activeElementsInPowder.put(dueElement, tick + dueElement.getRepeatTime());
				}
			}
		}
		powderHandler.getPowderTasks().removeIf(e -> e.getPowders().isEmpty());
	}

	public static int getTick() {
		return tick;
	}

}