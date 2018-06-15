package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.PlayerTracker;
import com.ruinscraft.powder.models.trackers.Tracker;
import com.ruinscraft.powder.models.trackers.TrackerType;
import com.ruinscraft.powder.util.ConfigUtil;

public class PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// TrackerType associated with the Powders in this PowderTask
	private TrackerType trackerType;
	// Powder associated with this PowderTask
	private Map<Powder, Tracker> powders;

	public PowderTask(String name) {
		this.name = name;
		this.powders = new HashMap<>();
	}

	public PowderTask(String name, Powder powder, Tracker tracker) {
		this.name = name;
		this.powders = new HashMap<>();
		this.powders.put(powder.clone(), tracker);
		trackerType = tracker.getType();
	}

	public PowderTask(Powder powder, Tracker tracker) {
		this.name = null;
		this.powders = new HashMap<>();
		this.powders.put(powder.clone(), tracker);
		trackerType = tracker.getType();
	}

	public Set<UUID> getUUIDsIfExist() {
		Set<UUID> uuids = new HashSet<>();
		for (Tracker tracker : this.powders.values()) {
			if (getUUIDIfExist(tracker) != null) {
				uuids.add(getUUIDIfExist(tracker));
			}
		}
		return uuids;
	}

	public UUID getUUIDIfExist(Tracker unknownTracker) {
		if (trackerType == TrackerType.ENTITY) {
			EntityTracker tracker = (EntityTracker) unknownTracker;
			return tracker.getEntityUUID();
		} else if (trackerType == TrackerType.PLAYER) {
			PlayerTracker tracker = (PlayerTracker) unknownTracker;
			return tracker.getUUID();
		}
		return null;
	}

	public Set<UUID> cancel() {
		Set<UUID> uuidsToSave = getUUIDsIfExist();
		cancelEffectsIfExist();
		this.powders.clear();
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.getInstance().getCreatedPowdersFile(), this);
		return uuidsToSave;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.getInstance().getCreatedPowdersFile(), this);
	}

	public TrackerType getTrackerType() {
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
				PowderPlugin.getInstance().getCreatedPowdersFile(), this);
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
		ConfigUtil.saveStationaryPowder(
				PowderPlugin.getInstance().getCreatedPowdersFile(), this);
		return removed;
	}

	public void cancelEffectsIfExist() {
		for (Entry<Powder, Tracker> entry : this.powders.entrySet()) {
			Tracker tracker = entry.getValue();
			if (tracker.getType() == TrackerType.STATIONARY) {
				continue;
			}
			UUID uuid = null;
			if (tracker.getType() == TrackerType.ENTITY) {
				EntityTracker entityTracker = (EntityTracker) tracker;
				uuid = entityTracker.getEntityUUID();
			}
			if (tracker.getType() == TrackerType.PLAYER) {
				PlayerTracker entityTracker = (PlayerTracker) tracker;
				uuid = entityTracker.getUUID();
			}
			for (PowderElement element : entry.getKey().getPowderElements()) {
				if (element instanceof Glow) {
					Glow glow = (Glow) element;
					if ((glow.getStartTime() * glow.getIterations()) + glow.getDuration() > 
					PowdersCreationTask.getCurrentTick()) {
						LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(uuid);
						livingEntity.removePotionEffect(glow.getType());
					}
				}
			}
		}
	}

}
