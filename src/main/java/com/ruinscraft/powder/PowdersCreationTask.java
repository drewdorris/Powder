package com.ruinscraft.powder;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.powders.Powder;
import com.ruinscraft.powder.models.tasks.PowderTask;

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
		for (Iterator<PowderTask> powderTaskIterator = 
				powderHandler.getPowderTasks().iterator(); powderTaskIterator.hasNext();) {
			PowderTask powderTask = powderTaskIterator.next();
			if (powderTask.getPowders().isEmpty()) {
				powderTaskIterator.remove();
				continue;
			}
			for (Iterator<Powder> powderIterator = 
					powderTask.getPowders().iterator(); powderIterator.hasNext();) {
				Powder powder = powderIterator.next();
				Location location = powder.getCurrentLocation();
				if (powder.getPowderElements().isEmpty()) {
					powderIterator.remove();
					continue;
				}
				for (Iterator<PowderElement> elementIterator = 
						powder.getPowderElements().keySet().iterator(); powderIterator.hasNext();) {
					PowderElement element = elementIterator.next();
					if (powder.getPowderElements().get(element) + element.getRepeatTime() <= tick) {
						if (element.getIterations() >= element.getLockedIterations()) {
							elementIterator.remove();
							continue;
						}
						element.create(location);
						element.iterate();
						powder.getPowderElements().put(element, tick);
					}
				}
			}
		}
	}

	public static int getTick() {
		return tick;
	}

}