package com.ruinscraft.powder.models.trackers;

import org.bukkit.Location;

public class StationaryTracker implements Tracker {

	Location location;

	public StationaryTracker(Location location) {
		this.location = location;
	}

	@Override
	public TrackerType getType() {
		return TrackerType.STATIONARY;
	}

	@Override
	public Location getCurrentLocation() {
		return location;
	}

}
