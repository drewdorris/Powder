package com.ruinscraft.powder.model.tracker;

import org.bukkit.Location;

public class StationaryTracker implements Tracker {

	private Location location;

	public StationaryTracker(Location location) {
		this.location = location;
	}

	@Override
	public Tracker.Type getType() {
		return Tracker.Type.STATIONARY;
	}

	@Override
	public Location getCurrentLocation() {
		return location;
	}

}
