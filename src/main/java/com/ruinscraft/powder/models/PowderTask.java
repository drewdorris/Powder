package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.ruinscraft.powder.PowdersCreationTask;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// player associated with this PowderTask
	private UUID player;
	// Location associated with this PowderTask, if the player is null
	private Location location;
	// Powder associated with this PowderTask
	private List<Powder> powders;

	// stores each active element with the tick it was last created (or first creation)
	private Map<PowderElement, Integer> activeElements;

	public PowderTask(UUID player, List<Powder> powders) {
		this.name = null;
		this.player = player;
		this.location = null;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powders = powders;
	}
	
	public PowderTask(UUID player, Powder powder) {
		this.name = null;
		this.player = player;
		this.location = null;
		activeElements = new HashMap<PowderElement, Integer>();
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}

	public PowderTask(Location location, List<Powder> powders) {
		this.name = null;
		this.player = null;
		this.location = location;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powders = powders;
	}
	
	public PowderTask(Location location, Powder powder) {
		this.name = null;
		this.player = null;
		this.location = location;
		activeElements = new HashMap<PowderElement, Integer>();
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}


	public PowderTask(String name, UUID player, List<Powder> powders) {
		this.name = name;
		this.player = player;
		this.location = null;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powders = powders;
	}
	
	public PowderTask(String name, UUID player, Powder powder) {
		this.name = name;
		this.player = player;
		this.location = null;
		activeElements = new HashMap<PowderElement, Integer>();
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}

	public PowderTask(Location location, List<Powder> powders, String name) {
		this.name = name;
		this.player = null;
		this.location = location;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powders = powders;
	}
	
	public PowderTask(Location location, Powder powder, String name) {
		this.name = name;
		this.player = null;
		this.location = location;
		activeElements = new HashMap<PowderElement, Integer>();
		powders = new ArrayList<Powder>();
		powders.add(powder);
	}

	public PowderTask(UUID player, List<Powder> powders, Map<PowderElement, Integer> elements) {
		this.name = null;
		this.player = player;
		this.location = null;
		activeElements = elements;
		this.powders = powders;
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

	public Location getLocation() {
		return location;
	}

	public Location getCurrentLocation() {
		if (player == null) {
			return location;
		} else {
			return Bukkit.getPlayer(player).getLocation();
		}
	}

	public boolean followsPlayer() {
		return player != null;
	}

	public List<Powder> getPowders() {
		return powders;
	}

	public Map<PowderElement, Integer> getActiveElements() {
		return activeElements;
	}

	public void addElement(PowderElement element) {
		activeElements.put(element, PowdersCreationTask.getTick() + element.getStartTime());
	}

	public void addElements(List<PowderElement> elements) {
		for (PowderElement element : elements) {
			addElement(element);
		}
	}

	public void removeElement(PowderElement element) {
		activeElements.remove(element);
	}
	
	public void removeElements(Powder powder) {
		for (PowderElement element : activeElements.keySet()) { 
			if (powder.getOriginalPowderElements().equals(element)) {
				activeElements.remove(element);
			}
		}
	}

}
