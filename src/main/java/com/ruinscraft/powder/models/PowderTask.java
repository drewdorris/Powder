package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;

import com.ruinscraft.powder.models.trackers.Tracker;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// Powder associated with this PowderTask
	private Map<Powder, Tracker> powders;

	public PowderTask() {
		this.powders = new HashMap<Powder, Tracker>();
	}

	public PowderTask(String name, Powder powder, Tracker tracker) {
		this.name = name;
		powders = new HashMap<Powder, Tracker>();
		powders.put(powder.clone(), tracker);
	}

	public PowderTask(Powder powder, Tracker tracker) {
		this.name = null;
		powders = new HashMap<Powder, Tracker>();
		powders.put(powder.clone(), tracker);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getFirstLocation() {
		for (Powder powder : powders.keySet()) {
			return powders.get(powder).getCurrentLocation();
		}
		return null;
	}

	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<Location>();
		for (Tracker tracker : powders.values()) {
			locations.add(tracker.getCurrentLocation());
		}
		return locations;
	}

	public Map<Powder, Tracker> getPowders() {
		return powders;
	}

	public boolean addPowder(Powder powder, Tracker tracker) {
		this.powders.put(powder.clone(), tracker);
		return true;
	}

	public boolean removePowder(Powder powder) {
		for (Powder otherPowder : getPowders().keySet()) {
			if (powder.getName().equals(otherPowder.getName())) {
				return powders.remove(otherPowder) != null;
			}
		}
		return false;
	}

}
