package com.ruinscraft.powder.models.trackers;

import org.bukkit.Location;

public interface Tracker {

	TrackerType getType();

	Location getCurrentLocation();

}
