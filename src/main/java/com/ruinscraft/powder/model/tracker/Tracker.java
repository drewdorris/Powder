package com.ruinscraft.powder.model.tracker;

import org.bukkit.Location;

public interface Tracker {

	Tracker.Type getType();

	Location getCurrentLocation();

	public enum Type {
		ENTITY,
		STATIONARY
	}

}
