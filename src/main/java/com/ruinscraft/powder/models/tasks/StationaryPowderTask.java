package com.ruinscraft.powder.models.tasks;

import org.bukkit.Location;

import com.ruinscraft.powder.models.powders.Powder;

public class StationaryPowderTask implements PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// Location associated with the location this PowderTask stays
	private Location location;
	// Powder associated with this PowderTask
	private Powder powder;

	public StationaryPowderTask(Location location, Powder powder) {
		this.name = null;
		this.location = location;
		this.powder = powder;
	}

	public StationaryPowderTask(String name, Location location, Powder powder) {
		this.name = name;
		this.location = location;
		this.powder = powder;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public Powder getPowder() {
		return powder;
	}

	@Override
	public Location getCurrentLocation() {
		return location;
	}

}
