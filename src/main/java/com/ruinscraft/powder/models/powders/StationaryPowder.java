package com.ruinscraft.powder.models.powders;

import org.bukkit.Location;

public class StationaryPowder extends Powder {

	private Location location;

	public StationaryPowder(Powder powder) {
		super(powder);
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public Location getCurrentLocation() {
		return location;
	}

}
