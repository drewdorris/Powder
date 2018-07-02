package com.ruinscraft.powder;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderElement;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.PowderUtil;

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
			for (Entry<Powder, Tracker> activePowder : powderTask.getPowders().entrySet()) {
				Powder powder = activePowder.getKey();
				Tracker tracker = activePowder.getValue();
				if (tracker.getType() == Tracker.Type.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					Entity entity = entityTracker.getEntity();
					if (entity == null) {
						if (entityTracker.isPlayer()) {
							powderTasksToRemoveWithoutSaving.add(powderTask);
							break;
						}
						powder.getPowderElements().clear();
						continue;
					}
					if (entity.isDead()) {
						uuidsToRemove.add(entityTracker.getUUID());
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