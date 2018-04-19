package com.ruinscraft.powder.models.trackers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PlayerTracker implements Tracker {

	private UUID uuid;

	public PlayerTracker(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public Location getCurrentLocation() {
		return Bukkit.getPlayer(uuid).getEyeLocation();
	}

}
