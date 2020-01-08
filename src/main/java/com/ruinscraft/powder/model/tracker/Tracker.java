package com.ruinscraft.powder.model.tracker;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Tracker {

	Tracker.Type getType();

	void refreshLocation();

	Location getCurrentLocation();

	boolean hasControl(Player player);

	enum Type {
		ENTITY,
		STATIONARY
	}

}
