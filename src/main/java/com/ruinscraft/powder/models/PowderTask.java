package com.ruinscraft.powder.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PowderTask {

	// player associated with this PowderTask
	private UUID player;
	// Powder associated with this PowderTask
	private Powder powder;
	private long startTime;
	
	private Map<PowderElement, Long> activeElements;
	
	public PowderTask(UUID player, Powder powder) {
		this.player = player;
		startTime = System.currentTimeMillis();
		activeElements = new HashMap<PowderElement, Long>();
		this.powder = powder;
	}
	
	public PowderTask(UUID player, Powder powder, Map<PowderElement, Long> elements) {
		this.player = player;
		startTime = System.currentTimeMillis();
		activeElements = elements;
		this.powder = powder;
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public Powder getPowder() {
		return powder;
	}
	
	public Map<PowderElement, Long> getActiveElements() {
		return activeElements;
	}
	
	public void addElement(PowderElement element) {
		activeElements.put(element, startTime);
	}
	
	public void addElements(List<PowderElement> elements) {
		for (PowderElement element : elements) {
			activeElements.put(element, startTime);
		}
	}
	
	public void removeElement(PowderElement element) {
		activeElements.remove(element);
	}

}
