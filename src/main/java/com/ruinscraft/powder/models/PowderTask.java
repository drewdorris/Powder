package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.ruinscraft.powder.models.powders.Powder;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// Powder associated with this PowderTask
	private List<Powder> powders;

	public PowderTask(Powder powder) {
		this.name = null;
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}

	public PowderTask(String name, Powder powder) {
		this.name = name;
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getFirstLocation() {
		for (Powder powder : powders) {
			return powder.getCurrentLocation();
		}
		return null;
	}

	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<Location>();
		for (Powder powder : powders) {
			locations.add(powder.getCurrentLocation());
		}
		return locations;
	}

	public List<Powder> getPowders() {
		return powders;
	}

	public boolean addPowder(Powder powder, Location location) {
		this.powders.add(powder);
		return true;
	}

	public boolean removePowder(Powder powder) {
		for (Powder otherPowder : getPowders()) {
			if (powder.getName().equals(otherPowder.getName())) {
				return powders.remove(otherPowder);
			}
		}
		return false;
	}

}
