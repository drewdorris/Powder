package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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
		List<PowderTask> powderTasksToRemove = new ArrayList<PowderTask>();
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			Set<Entry<Powder, Tracker>> powderSet = powderTask.getPowders().entrySet();
			for (Entry<Powder, Tracker> entry : powderSet) {
				Powder powder = entry.getKey();
				Tracker tracker = entry.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getEntity() == null 
							|| entityTracker.getEntity().isDead()) {
						powderSet.remove(entry);
						continue;
					}
				}
				Set<Entry<PowderElement, Integer>> elementSet = powder.getPowderElements().entrySet();
				if (elementSet.isEmpty()) {
					powderSet.remove(entry);
					continue;
				}
				for (Entry<PowderElement, Integer> entry2 : elementSet) {
					if (entry2.getValue() <= tick) {
						PowderElement element = entry2.getKey();
						if (element.getIterations() >= element.getLockedIterations()) {
							elementSet.remove(entry2);
							continue;
						}
						element.create(tracker.getCurrentLocation());
						element.iterate();
						entry2.setValue(tick + element.getRepeatTime());
					}
				}
			}
			if (powderSet.isEmpty()) {
				powderTasksToRemove.add(powderTask);
			}
		}
		powderHandler.removePowderTasks(powderTasksToRemove);
	}

	public static int getTick() {
		return tick;
	}

}