package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// player associated with this PowderTask
	private UUID player;
	// Powder associated with this PowderTask
	private Map<Powder, Location> powders;
	
	public PowderTask(UUID player, Powder powder) {
		this.name = null;
		this.player = player;
		powders = new HashMap<Powder, Location>();
		powders.put(powder.clone(), null);
	}
	
	public PowderTask(String name, Location location, Powder powder) {
		this.name = null;
		this.player = null;
		powders = new HashMap<Powder, Location>();
		powders.put(powder.clone(), location);
	}
	
	public PowderTask(String name, UUID player, Powder powder) {
		this.name = name;
		this.player = player;
		powders = new HashMap<Powder, Location>();
		powders.put(powder.clone(), null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public Location getFirstLocation() {
		if (player == null) {
			for (Powder powder : powders.keySet()) {
				return powders.get(powder);
			}
			return null;
		} else {
			return Bukkit.getPlayer(player).getLocation();
		}
	}
	
	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<Location>();
		for (Location location : powders.values()) {
			locations.add(location);
		}
		return locations;
	}

	public boolean followsPlayer() {
		return player != null;
	}

	public Map<Powder, Location> getPowders() {
		return powders;
	}
	
	public boolean addPowder(Powder powder, Location location) {
		this.powders.put(powder, location);
		return true;
	}
	
	public boolean removePowder(Powder powder) {
		return this.powders.remove(powder) != null;
	}

}
