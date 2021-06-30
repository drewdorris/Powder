package com.ruinscraft.powder;

import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderElement;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.PowderUtil;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PowdersCreationTask extends BukkitRunnable {

	private static int tick = 0;
	private static final int TIME_BEFORE_REFRESH = 20 * 60 * 5; // 1 hour

	public static int getCurrentTick() {
		return tick;
	}

	@Override
	public void run() {
		tick++;
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();
		if (powderHandler == null || powderHandler.getPowderTasks().isEmpty()) return;

		refreshLocations(powderHandler);

		if (PowderPlugin.get().asyncMode()) {
			CompletableFuture.runAsync(() -> {
				createElements(powderHandler);
			});
		} else {
			createElements(powderHandler);
		}
	}

	public void createElements(PowderHandler powderHandler) {
		for (int index = 0; index < powderHandler.getPowderTasks().size(); index++) {
			PowderTask powderTask = powderHandler.getPowderTasks().get(index);
			Powder powder = powderTask.getPowder();
			Tracker tracker = powderTask.getTracker();

			Location currentLocation = tracker.getCurrentLocation();

			//boolean refresh = false;
			for (int indexTwo = 0; indexTwo < powder.powderElements.size(); indexTwo++) {
				PowderElement element = powder.powderElements.get(indexTwo);

				/*
				if (element.getTimeAlive() > TIME_BEFORE_REFRESH) {
					refresh = true;
					break;
				}
				 */

				if (element.getIterations() >= element.getLockedIterations()) {
					powder.powderElements.remove(element);
					indexTwo--;
					continue;
				}

				if (element.getNextTick() <= tick) {
					element.create(currentLocation.clone());
					element.iterate();
				}
			}
			//if (refresh) PowderUtil.refreshAndRestart(powderTask);
		}
	}

	// Must be run sync!
	public void refreshLocations(PowderHandler powderHandler) {
		Set<UUID> uuidsToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemove = new HashSet<>();
		Set<PowderTask> powderTasksToRemoveWithoutSaving = new HashSet<>();

		for (int index = 0; index < powderHandler.getPowderTasks().size(); index++) {
			PowderTask powderTask = powderHandler.getPowderTasks().get(index);

			Tracker tracker = powderTask.getTracker();
			if (tracker.getType() == Tracker.Type.ENTITY) {
				EntityTracker entityTracker = (EntityTracker) tracker;
				Entity entity = entityTracker.getEntity();
				if (entity == null) {
					if (entityTracker.isPlayer()) {
						powderTasksToRemoveWithoutSaving.add(powderTask);
						break;
					}
					powderTask.cancel();
					continue;
				}
				if (entity.isDead()) {
					uuidsToRemove.add(entityTracker.getUUID());
					break;
				}
			}

			tracker.refreshLocation();

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