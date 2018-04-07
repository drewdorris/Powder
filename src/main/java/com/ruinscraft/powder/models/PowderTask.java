package com.ruinscraft.powder.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

public class PowderTask {

	// player associated with this PowderTask
	private UUID player;
	// Location associated with this PowderTask, if the player is null
	private Location location;
	// Powder associated with this PowderTask
	private Powder powder;
	// when the PowderTask started
	private long startTime;

	// stores each active element with the unix time it was last created (or first creation)
	private Map<PowderElement, Long> activeElements;

	public PowderTask(UUID player, Powder powder) {
		this.player = player;
		this.location = null;
		startTime = System.currentTimeMillis();
		activeElements = new HashMap<PowderElement, Long>();
		this.powder = powder;
	}
	
	public PowderTask(Location location, Powder powder) {
		this.player = null;
		this.location = location;
		startTime = System.currentTimeMillis();
		activeElements = new HashMap<PowderElement, Long>();
		this.powder = powder;
	}

	public PowderTask(UUID player, Powder powder, Map<PowderElement, Long> elements) {
		this.player = player;
		this.location = null;
		startTime = System.currentTimeMillis();
		activeElements = elements;
		this.powder = powder;
	}

	public UUID getPlayerUUID() {
		return player;
	}
	
	public Location getLocation() {
		return location;
	}

	public Powder getPowder() {
		return powder;
	}

	public Long getStartTime() {
		return startTime;
	}

	public Map<PowderElement, Long> getActiveElements() {
		return activeElements;
	}

	public void addElement(PowderElement element) {
		activeElements.put(element, System.currentTimeMillis() + (element.getStartTime() * 50L) - (element.getRepeatTime() * 50L));
	}

	public void addElements(List<PowderElement> elements) {
		for (PowderElement element : elements) {
			activeElements.put(element, System.currentTimeMillis() + (element.getStartTime() * 50L) - (element.getRepeatTime() * 50L));
		}
	}

	public void removeElement(PowderElement element) {
		activeElements.remove(element);
	}

}
