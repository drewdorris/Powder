package com.ruinscraft.powder;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderElement;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.PowderUtil;

public class PowdersCreationTask extends BukkitRunnable {

	private static int tick = 0;

	public static int getCurrentTick() {
		return tick;
	}

	@Override
	public void run() {
		tick++;
		if (PowderPlugin.getInstance().asyncMode()) {
			Bukkit.getScheduler().runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
				createElements();
			});
		} else {
			createElements();
		}
	}

	public void createElements() {
		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		if (powderHandler == null || 
				powderHandler.getPowderTasks().isEmpty()) {
			return;
		}
		Set<UUID> uuidsToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemoveWithoutSaving = new HashSet<>();
		for (int index = 0; index < powderHandler.getPowderTasks().size(); index++) {
			PowderTask powderTask = powderHandler.getPowderTasks().get(index);
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
				for (int indexTwo = 0; indexTwo < powder.powderElements.size(); indexTwo++) {
					PowderElement element = powder.powderElements.get(indexTwo);
					if (element.getIterations() >= element.getLockedIterations()) {
						powder.powderElements.remove(element);
						indexTwo--;
						continue;
					}
					if (element.getNextTick() <= tick) {
						element.create(tracker.getCurrentLocation().clone());
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