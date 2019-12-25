package com.ruinscraft.powder.model.tracker;

import org.bukkit.Location;

public interface Tracker {

	Tracker.Type getType();

	void refreshLocation();

	Location getCurrentLocation();

	enum Type {
		ENTITY,
		STATIONARY
	}

}
