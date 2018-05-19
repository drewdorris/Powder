package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.PlayerTracker;
import com.ruinscraft.powder.models.trackers.Tracker;
import com.ruinscraft.powder.models.trackers.TrackerType;
import com.ruinscraft.powder.util.PowderUtil;
import com.ruinscraft.powder.models.Powder;

public class PowdersCreationTask extends BukkitRunnable {

	private PowderHandler powderHandler;
	private Map<PowderTask, Integer> powderTasksProcessingUnload;
	private static int tick;

	public PowdersCreationTask() {
		powderHandler = PowderPlugin.getInstance().getPowderHandler();
		powderTasksProcessingUnload = new HashMap<PowderTask, Integer>();
		tick = 0;
	}

	@Override
	public void run() {
		if (powderHandler.getPowderTasks().isEmpty()) {
			tick = 0;
			cancel();
		}
		tick++;
		Set<UUID> uuidsToRemove = new HashSet<UUID>();
		List<PowderTask> powderTasksToRemove = new ArrayList<PowderTask>();
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			if (powderTasksProcessingUnload.keySet().contains(powderTask)) {
				if (tick - powderTasksProcessingUnload.get(powderTask) >= 15) {
					powderTasksToRemove.add(powderTask);
				}
				continue;
			}
			Set<Entry<Powder, Tracker>> activePowdersInTask = powderTask.getPowders().entrySet();
			for (Entry<Powder, Tracker> activePowder : activePowdersInTask) {
				Powder powder = activePowder.getKey();
				Tracker tracker = activePowder.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getEntity() == null 
							|| entityTracker.getEntity().isDead()) {
						activePowdersInTask.remove(activePowder);
						uuidsToRemove.add(entityTracker.getEntityUUID());
						continue;
					}
				}
				if (tracker.getType() == TrackerType.PLAYER) {
					PlayerTracker playerTracker = (PlayerTracker) tracker;
					if (Bukkit.getPlayer(playerTracker.getUUID()) == null) {
						PowderUtil.unloadUUID(playerTracker.getUUID());
						powderTasksProcessingUnload.put(powderTask, tick);
						break;
					}
				}
				List<PowderElement> activeElementsInPowder = powder.getPowderElements();
				if (activeElementsInPowder.isEmpty()) {
					activePowdersInTask.remove(activePowder);
					continue;
				}
				for (PowderElement dueElement : powder.getDuePowderElements(tick)) {
					if (dueElement.getIterations() >= dueElement.getLockedIterations()) {
						activeElementsInPowder.remove(dueElement);
						continue;
					}
					if (dueElement.getNextTick() <= tick) {
						dueElement.create(tracker.getCurrentLocation());
						dueElement.iterate();
					}
				}
			}
		}
		for (UUID uuid : uuidsToRemove) {
			PowderUtil.cancelAllPowdersAndSave(uuid);
		}
		powderHandler.getPowderTasks().removeAll(powderTasksToRemove);
		powderHandler.getPowderTasks().removeIf(t -> t.getPowders().isEmpty());
	}

	public static int getCurrentTick() {
		return tick;
	}

}