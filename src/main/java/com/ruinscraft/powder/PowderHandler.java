package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Location;

import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderElement;
import com.ruinscraft.powder.models.PowderTask;
import com.ruinscraft.powder.models.trackers.EntityTracker;
import com.ruinscraft.powder.models.trackers.PlayerTracker;
import com.ruinscraft.powder.models.trackers.StationaryTracker;
import com.ruinscraft.powder.models.trackers.Tracker;
import com.ruinscraft.powder.models.trackers.TrackerType;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;

public class PowderHandler {

	// list of all Powders on the server
	private List<Powder> powders;
	// set of current PowderTasks being handled
	private Set<PowderTask> powderTasks;
	// map of categories and their descriptions
	private Map<String, String> categories;
	// are categories enabled?
	private boolean categoriesEnabled;

	// every 5 seconds, all entities loaded with chunk loading are
	// checked to see if they have powders attached to them;
	// they are created if so
	private Set<UUID> entitiesToLoad;

	// initialize
	public PowderHandler() {
		powders = new ArrayList<Powder>();
		powderTasks = new HashSet<PowderTask>();
		categories = new HashMap<String, String>();

		if (PowderPlugin.getInstance().useStorage()) {
			entitiesToLoad = new HashSet<UUID>();
			PowderPlugin.getInstance().getServer()
			.getScheduler().runTaskTimer(PowderPlugin.getInstance(), () -> {
				if (PowderPlugin.isLoading()) {
					return;
				}
				PowderUtil.loadPowdersForUUIDs(new HashSet<UUID>(this.entitiesToLoad));
				this.entitiesToLoad.clear();
			}, 0L, 20L);
		}
	}

	// clear all Powders and end all PowderTasks
	public void clearEverything() {
		this.powders = null;
		clearAllTasks();
	}

	public List<Powder> getPowders() {
		return this.powders;
	}

	// return Powders that contain the string in their name
	public List<Powder> getSimilarPowders(String string) {
		List<Powder> similarPowders = new ArrayList<Powder>();
		for (Powder powder : this.powders) {
			if (powder.getName().toLowerCase().contains(string.toLowerCase())) {
				similarPowders.add(powder);
			}
		}
		return similarPowders;
	}

	// gets Powder from name
	public Powder getPowder(String name) {
		for (Powder powder : this.powders) {
			if (powder.getName().equalsIgnoreCase(name)) {
				return powder;
			}
		}
		return null;
	}

	// adds Powder to the complete list
	public void addPowder(Powder powder) {
		if (this.powders == null) {
			return;
		}
		this.powders.add(powder);
	}

	public Set<PowderTask> getPowderTasks() {
		return this.powderTasks;
	}

	// gets all current PowderTasks under a player
	public Set<PowderTask> getPowderTasks(UUID uuid) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Tracker tracker : powderTask.getPowders().values()) {
				if (tracker.getType() == TrackerType.PLAYER) {
					PlayerTracker playerTracker = (PlayerTracker) tracker;
					if (playerTracker.getUUID().equals(uuid)) {
						playerPowderTasks.add(powderTask);
						break;
					}
				} else if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getEntityUUID().equals(uuid)) {
						playerPowderTasks.add(powderTask);
						break;
					}
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all current PowderTasks under a player & Powder
	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (powder.getName().equals(entry.getKey().getName())) {
					if (tracker.getType() == TrackerType.PLAYER) {
						PlayerTracker playerTracker = (PlayerTracker) tracker;
						if (playerTracker.getUUID().equals(uuid)) {
							playerPowderTasks.add(powderTask);
							break;
						}
					} else if (tracker.getType() == TrackerType.ENTITY) {
						EntityTracker entityTracker = (EntityTracker) tracker;
						if (entityTracker.getEntityUUID().equals(uuid)) {
							playerPowderTasks.add(powderTask);
							break;
						}
					}
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all users who have a running PowderTask
	public Set<UUID> getAllPowderTaskUsers() {
		Set<UUID> players = new HashSet<>();
		players.addAll(getCurrentPlayerTracks().values().stream()
				.map(PlayerTracker::getUUID).collect(Collectors.toSet()));
		return players;
	}

	// gets all users who have a running PowderTask
	public Set<UUID> getAllPowderTaskUUIDs() {
		Set<UUID> uuids = new HashSet<>();
		uuids.addAll(getCurrentPlayerTracks().values().stream()
				.map(PlayerTracker::getUUID).collect(Collectors.toSet()));
		uuids.addAll(getCurrentEntityTracks().values().stream()
				.map(EntityTracker::getEntityUUID).collect(Collectors.toSet()));
		return uuids;
	}

	public PowderTask getPowderTask(String name) {
		for (PowderTask powderTask : this.powderTasks) {
			if (powderTask.getName() == null) {
				continue;
			}
			if (powderTask.getName().equals(name) || 
					powderTask.getName() == null && name == null) {
				return powderTask;
			}
		}
		return null;
	}

	public Map<PowderTask, Integer> getNearbyPowderTasks(Location location, int range) {
		Map<PowderTask, Integer> nearbyPowderTasks = new HashMap<PowderTask, Integer>();
		for (PowderTask powderTask : this.powderTasks) {
			int taskRange = Integer.MAX_VALUE;
			for (Powder powder : powderTask.getPowders().keySet()) {
				int distance = (int) location.distance(
						powderTask.getPowders().get(powder).getCurrentLocation());
				if (distance < taskRange) {
					taskRange = distance;
				}
			}
			if (taskRange < range) {
				nearbyPowderTasks.put(powderTask, taskRange);
			}
		}
		return nearbyPowderTasks;
	}

	public Map<Powder, PlayerTracker> getCurrentPlayerTracks() {
		Map<Powder, PlayerTracker> playerTracks = new HashMap<Powder, PlayerTracker>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (tracker.getType() == TrackerType.PLAYER) {
					playerTracks.put(entry.getKey(), (PlayerTracker) tracker);
					break;
				}
			}
		}
		return playerTracks;
	}

	public Map<Powder, EntityTracker> getCurrentEntityTracks() {
		Map<Powder, EntityTracker> entityTracks = new HashMap<Powder, EntityTracker>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					PowderPlugin.getInstance().getLogger().info("entity entry !!!");
					entityTracks.put(entry.getKey(), (EntityTracker) tracker);
					break;
				}
			}
		}
		return entityTracks;
	}

	public Map<Powder, StationaryTracker> getCurrentStationaryTracks() {
		Map<Powder, StationaryTracker> stationaryTracks = 
				new HashMap<Powder, StationaryTracker>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (tracker.getType() == TrackerType.ENTITY) {
					stationaryTracks.put(entry.getKey(), (StationaryTracker) tracker);
					break;
				}
			}
		}
		return stationaryTracks;
	}

	public PowderTask getPowderTask(Powder powder, Tracker tracker) {
		for (PowderTask powderTask : this.powderTasks) {
			if (powderTask.getPowders().containsKey(powder) && 
					powderTask.getPowders().containsValue(tracker)) {
				return powderTask;
			}
		}
		return null;
	}

	// ends all tasks associated with all PowderTasks
	public void clearAllTasks() {
		this.powderTasks.clear();
	}

	// adds a PowderTask
	public void runPowderTask(PowderTask powderTask) {
		for (Powder powder : powderTask.getPowders().keySet()) {
			for (PowderElement powderElement : powder.getPowderElements()) {
				if (powderElement.getLockedIterations() == 0) {
					powderElement.setLockedIterations(Integer.MAX_VALUE);
				}
			}
		}
		if (getPowderTasks().isEmpty()) {
			new PowdersCreationTask().runTaskTimer(PowderPlugin.getInstance(), 0L, 1L);
		}
		this.powderTasks.add(powderTask);
		if (!powderTask.getUUIDsIfExist().isEmpty()) {
			PowderUtil.savePowdersForUUIDs(powderTask.getUUIDsIfExist());
		}
		if (!ConfigUtil.containsTask(powderTask)) {
			ConfigUtil.saveStationaryPowder(
					PowderPlugin.getInstance().getCreatedPowdersFile(), powderTask);
		}
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTask(PowderTask powderTask) {
		boolean removal = this.powderTasks.remove(powderTask);
		PowderUtil.savePowdersForUUIDs(powderTask.cancel());
		ConfigUtil.removeStationaryPowder(powderTask);
		return removal;
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTaskWithoutSaving(PowderTask powderTask) {
		ConfigUtil.removeStationaryPowder(powderTask);
		return this.powderTasks.remove(powderTask);
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTasks(Collection<PowderTask> powderTasks) {
		boolean removal = this.powderTasks.removeAll(powderTasks);
		Set<UUID> uuids = new HashSet<UUID>();
		for (PowderTask powderTask : powderTasks) {
			ConfigUtil.removeStationaryPowder(powderTask);
			uuids.addAll(powderTask.cancel());
		}
		PowderUtil.savePowdersForUUIDs(uuids);
		return removal;
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTasksWithoutSaving(Collection<PowderTask> powderTasks) {
		boolean removal = this.powderTasks.removeAll(powderTasks);
		Set<UUID> uuids = new HashSet<UUID>();
		for (PowderTask powderTask : powderTasks) {
			ConfigUtil.removeStationaryPowder(powderTask);
			uuids.addAll(powderTask.cancel());
		}
		return removal;
	}

	public Map<String, String> getCategories() {
		return this.categories;
	}

	// gets a category from string
	public String getCategory(String category) {
		for (String otherCategory : this.categories.keySet()) {
			if (otherCategory.equalsIgnoreCase(category)) {
				return otherCategory;
			}
		}
		return null;
	}

	// gets categories which contain the given string
	public Map<String, String> getSimilarCategories(String string) {
		Map<String, String> similarPowders = new HashMap<String, String>();
		for (String category : this.categories.keySet()) {
			if (category.toLowerCase().contains(string.toLowerCase())) {
				similarPowders.put(category, this.categories.get(category));
			}
		}
		return similarPowders;
	}

	// gets Powders under a given category
	public List<Powder> getPowdersFromCategory(String category) {
		for (String otherCategory : this.categories.keySet()) {
			if (otherCategory.toLowerCase().equals(category.toLowerCase())) {
				category = otherCategory;
				break;
			}
		}
		List<Powder> addedPowders = new ArrayList<Powder>();
		for (Powder powder : this.powders) {
			if (powder.getCategories().contains(category)) {
				addedPowders.add(powder);
			}
		}
		return addedPowders;
	}

	// gets unhidden Powders under a given category
	public List<Powder> getUnhiddenPowdersFromCategory(String category) {
		for (String otherCategory : this.categories.keySet()) {
			if (otherCategory.toLowerCase().equals(category.toLowerCase())) {
				category = otherCategory;
				break;
			}
		}
		List<Powder> addedPowders = new ArrayList<Powder>();
		for (Powder powder : this.powders) {
			if (powder.getCategories().contains(category)) {
				if (!(powder.isHidden())) {
					addedPowders.add(powder);
				}
			}
		}
		return addedPowders;
	}

	// adds a category
	public void addCategory(String category, String description) {
		this.categories.put(category, PowderUtil.color(description
				.replace("{total}", String.valueOf(getPowdersFromCategory(category).size()))
				.replace("{total-hidden}", 
						String.valueOf(getUnhiddenPowdersFromCategory(category).size()))));
	}

	// sets description for a given category
	public void setDescription(String category, String description) {
		this.categories.put(category, PowderUtil.color(description
				.replace("{total}", String.valueOf(getPowdersFromCategory(category).size()))));
	}

	public boolean categoriesEnabled() {
		return this.categoriesEnabled;
	}

	public void setIfCategoriesEnabled(boolean categoriesEnabled) {
		this.categoriesEnabled = categoriesEnabled;
	}

	// entities to be checked in database
	public Set<UUID> getEntitiesToLoad() {
		return this.entitiesToLoad;
	}

	// adds entity to be checked in database
	public void addEntityToLoad(UUID uuid) {
		this.entitiesToLoad.add(uuid);
	}

}
