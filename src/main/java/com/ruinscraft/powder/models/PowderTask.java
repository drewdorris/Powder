package com.ruinscraft.powder.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.ruinscraft.powder.PowdersCreationTask;

public class PowderTask {

	// player associated with this PowderTask
	private UUID player;
	// Location associated with this PowderTask, if the player is null
	private Location location;
	// Powder associated with this PowderTask
	private Powder powder;

	// stores each active element with the tick it was last created (or first creation)
	private Map<PowderElement, Integer> activeElements;

	public PowderTask(UUID player, Powder powder) {
		this.player = player;
		this.location = null;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powder = powder;
	}

	public PowderTask(Location location, Powder powder) {
		this.player = null;
		this.location = location;
		activeElements = new HashMap<PowderElement, Integer>();
		this.powder = powder;
	}

	public PowderTask(UUID player, Powder powder, Map<PowderElement, Integer> elements) {
		this.player = player;
		this.location = null;
		activeElements = elements;
		this.powder = powder;
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
	
	public Boolean followsPlayer() {
		return player != null;
	}

	public Powder getPowder() {
		return powder;
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

}
