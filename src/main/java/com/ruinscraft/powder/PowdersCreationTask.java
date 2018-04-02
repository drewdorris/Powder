package com.ruinscraft.powder;

import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderTask;

public class PowdersCreationTask extends BukkitRunnable {

	private PowderHandler powderHandler;

	public PowdersCreationTask() {
		powderHandler = PowderPlugin.getInstance().getPowderHandler();
	}

	@Override
	public void run() {
		if (powderHandler == null) {
			cancel();
		}

		for (PowderTask powderTask : powderHandler.getPowderTasks()) {
			// do stuff
		}
	}

}