package com.ruinscraft.powder.model;

import java.util.UUID;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.ConfigUtil;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	private Tracker tracker;
	// Powder associated with this PowderTask
	private Powder powder;

	public PowderTask(String name) {
		this.name = name;
	}

	public PowderTask(String name, Powder powder, Tracker tracker) {
		this.name = name;
		this.tracker = tracker;
		this.powder = powder.clone();
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.get().getPlayerDataFile(), this);
	}

	public UUID cancel() {
		this.powder = null;
		if (this.tracker.getType() == Tracker.Type.ENTITY) {
			return ((EntityTracker) tracker).getUUID();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.get().getPlayerDataFile(), this);
	}

	public Tracker getTracker() {
		return tracker;
	}

	public Powder getPowder() {
		return powder;
	}

	public boolean hasAnyElements() {
		if (powder == null) return false;
		if (powder.getPowderElements().size() > 0) {
			return true;
		}
		return false;
	}

}
