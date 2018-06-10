package com.ruinscraft.powder;

import java.util.HashSet;
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
	private static int tick;

	public PowdersCreationTask() {
		powderHandler = PowderPlugin.getInstance().getPowderHandler();
		tick = 0;
	}

	public static int getCurrentTick() {
		return tick;
	}

	@Override
	public void run() {
		if (powderHandler.getPowderTasks().isEmpty()) {
			tick = 0;
			cancel();
		}
		tick++;
		Set<UUID> uuidsToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemoveWithoutSaving = new HashSet<>();
		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			Set<Entry<Powder, Tracker>> activePowdersInTask = powderTask.getPowders().entrySet();
			for (Entry<Powder, Tracker> activePowder : activePowdersInTask) {
				Powder powder = activePowder.getKey();
				Tracker tracker = activePowder.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getEntity() == null) {
						powder.getPowderElements().clear();
						continue;
					}
					if (entityTracker.getEntity().isDead()) {
						uuidsToRemove.add(entityTracker.getEntityUUID());
						break;
					}
				}
				if (tracker.getType() == TrackerType.PLAYER) {
					PlayerTracker playerTracker = (PlayerTracker) tracker;
					if (Bukkit.getPlayer(playerTracker.getUUID()) == null) {
						powderTasksToRemoveWithoutSaving.add(powderTask);
						break;
					}
				}
				for (int i = 0; i < powder.powderElements.size(); i++) {
					PowderElement element = powder.powderElements.get(i);
					if (element.getIterations() >= element.getLockedIterations()) {
						powder.powderElements.remove(element);
						i--;
						continue;
					}
					if (element.getNextTick() <= tick) {
						element.create(tracker.getCurrentLocation());
						element.iterate();
					}
				}
			}
			if (!powderTask.hasAnyElements()) {
				powderTasksToRemove.add(powderTask);
			}
		}
		for (UUID uuid : uuidsToRemove) {
			PowderUtil.cancelAllPowdersAndSave(uuid);
		}
		powderHandler.cancelPowderTasks(powderTasksToRemove);
		powderHandler.cancelPowderTasksWithoutSaving(powderTasksToRemoveWithoutSaving);
	}

}