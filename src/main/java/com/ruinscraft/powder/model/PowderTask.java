package com.ruinscraft.powder.model;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.ConfigUtil;
import org.bukkit.Location;

import java.util.*;
import java.util.Map.Entry;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// TrackerType associated with the Powders in this PowderTask
	private Tracker.Type trackerType;
	// Powder associated with this PowderTask
	private Map<Powder, Tracker> powders;

	public PowderTask(String name) {
		this.name = name;
		this.powders = new HashMap<>();
	}

	public PowderTask(String name, Powder powder, Tracker tracker) {
		this.name = name;
		this.powders = new HashMap<>();
		this.trackerType = tracker.getType();
		addPowder(powder, tracker);
	}

	public PowderTask(Powder powder, Tracker tracker) {
		this.name = null;
		this.powders = new HashMap<>();
		this.trackerType = tracker.getType();
		addPowder(powder, tracker);
	}

	public Set<UUID> getUUIDsIfExist() {
		Set<UUID> uuids = new HashSet<>();
		for (Tracker tracker : this.powders.values()) {
			if (getUUIDIfExists(tracker) != null) {
				uuids.add(getUUIDIfExists(tracker));
			}
		}
		return uuids;
	}

	public UUID getUUIDIfExists(Tracker unknownTracker) {
		if (trackerType == Tracker.Type.ENTITY) {
			EntityTracker tracker = (EntityTracker) unknownTracker;
			return tracker.getUUID();
		}
		return null;
	}

	public Set<UUID> cancel() {
		Set<UUID> uuidsToSave = getUUIDsIfExist();
		this.powders.clear();
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.get().getCreatedPowdersFile(), this);
		return uuidsToSave;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.get().getCreatedPowdersFile(), this);
	}

	public Tracker.Type getTrackerType() {
		return trackerType;
	}

	public Location getFirstLocation() {
		for (Powder powder : powders.keySet()) {
			return powders.get(powder).getCurrentLocation();
		}
		return null;
	}

	public List<Location> getLocations() {
		List<Location> locations = new ArrayList<>();
		for (Tracker tracker : powders.values()) {
			locations.add(tracker.getCurrentLocation());
		}
		return locations;
	}

	public Powder getPowderIfNotStationary() {
		for (Entry<Powder, Tracker> entry : getPowders().entrySet()) {
			if (entry.getValue().getType() == Tracker.Type.STATIONARY) {
				continue;
			}
			return entry.getKey();
		}
		return null;
	}

	public Map<Powder, Tracker> getPowders() {
		return powders;
	}

	public boolean hasAnyElements() {
		for (Powder powder : powders.keySet()) {
			if (powder.getPowderElements().size() > 0) {
				return true;
			}
		}
		return false;
	}

	public boolean addPowder(Powder powder, Tracker tracker) {
		if (this.powders.isEmpty()) {
			this.trackerType = tracker.getType();
		}
		if (!tracker.getType().equals(trackerType)) {
			return false;
		}
		this.powders.put(powder.clone(), tracker);
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.get().getCreatedPowdersFile(), this);
		return true;
	}

	public boolean removePowder(Powder powder) {
		Powder toRemove = powder;
		for (Powder otherPowder : getPowders().keySet()) {
			if (powder.getName().equals(otherPowder.getName())) {
				toRemove = otherPowder;
				break;
			}
		}
		boolean removed = powders.remove(toRemove) != null;
		if (powders.size() == 0) {
			this.cancel();
		} else {
			ConfigUtil.saveStationaryPowder(
					PowderPlugin.get().getCreatedPowdersFile(), this);
		}
		return removed;
	}

}
