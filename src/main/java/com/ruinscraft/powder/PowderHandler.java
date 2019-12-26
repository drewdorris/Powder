package com.ruinscraft.powder;

import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderElement;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PowderHandler {

	// list of all Powders on the server
	private List<Powder> powders;
	// set of current PowderTasks being handled
	private List<PowderTask> powderTasks;
	// map of categories and their descriptions
	private Map<String, String> categories;
	// are categories enabled?
	private boolean categoriesEnabled;

	// every second, all entities loaded with chunk loading are
	// checked to see if they have powders attached to them;
	// they are created if so
	private Set<UUID> entitiesToLoad;

	// initialize
	public PowderHandler() {
		powders = new ArrayList<>();
		powderTasks = new ArrayList<>();
		categories = new HashMap<>();

		if (PowderPlugin.getInstance().useStorage()) {
			entitiesToLoad = new HashSet<UUID>();
			PowderPlugin.getInstance().getServer()
			.getScheduler().runTaskTimer(PowderPlugin.getInstance(), () -> {
				if (PowderPlugin.isLoading()) {
					return;
				}
				PowderUtil.loadPowdersForUUIDs(new HashSet<>(this.entitiesToLoad));
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
		List<Powder> similarPowders = new ArrayList<>();
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
				if (PowderPlugin.getInstance().fastMode()) {
					Powder newPowder = ConfigUtil.loadPowderFromConfig(powder.getPath());
					if (newPowder == null) {
						PowderPlugin.warning("Powder was null for some reason!");
						continue;
					}
					return newPowder;
				}
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

	public List<PowderTask> getPowderTasks() {
		return this.powderTasks;
	}

	// gets all current PowderTasks under a uuid
	public Set<PowderTask> getPowderTasks(UUID uuid) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Tracker tracker : powderTask.getPowders().values()) {
				if (tracker.getType() == Tracker.Type.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getUUID().equals(uuid)) {
						playerPowderTasks.add(powderTask);
						break;
					}
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all current PowderTasks under a uuid & Powder
	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (powder.getName().equals(entry.getKey().getName())) {
					if (tracker.getType() == Tracker.Type.ENTITY) {
						EntityTracker entityTracker = (EntityTracker) tracker;
						if (entityTracker.getUUID().equals(uuid)) {
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
		for (Entry<Powder, EntityTracker> entry : getCurrentEntityTracks().entrySet()) {
			EntityTracker tracker = entry.getValue();
			Entity entity = tracker.getEntity();
			if (entity instanceof Player) {
				players.add(entity.getUniqueId());
			}
		}
		return players;
	}

	// gets all users who have a running PowderTask
	public Set<UUID> getAllPowderTaskUUIDs() {
		Set<UUID> uuids = new HashSet<>();
		uuids.addAll(getCurrentEntityTracks().values().stream()
				.map(EntityTracker::getUUID).collect(Collectors.toSet()));
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
		Map<PowderTask, Integer> nearbyPowderTasks = new HashMap<>();
		for (PowderTask powderTask : this.powderTasks) {
			int taskRange = Integer.MAX_VALUE;
			for (Powder powder : powderTask.getPowders().keySet()) {
				Location otherLocation = powderTask.getPowders().get(powder).getCurrentLocation();
				if (!otherLocation.getWorld().equals(location.getWorld())) {
					continue;
				}
				int distance = (int) location.distance(otherLocation);
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

	public Map<Powder, EntityTracker> getCurrentEntityTracks() {
		Map<Powder, EntityTracker> entityTracks = new HashMap<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (tracker.getType() == Tracker.Type.ENTITY) {
					entityTracks.put(entry.getKey(), (EntityTracker) tracker);
					break;
				}
			}
		}
		return entityTracks;
	}

	public Map<Powder, StationaryTracker> getCurrentStationaryTracks() {
		Map<Powder, StationaryTracker> stationaryTracks = new HashMap<>();
		for (PowderTask powderTask : this.powderTasks) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				if (tracker.getType() == Tracker.Type.STATIONARY) {
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
				powderElement.setStartingTick();
			}
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
		Set<UUID> uuids = new HashSet<>();
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
		for (PowderTask powderTask : powderTasks) {
			ConfigUtil.removeStationaryPowder(powderTask);
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
	public Map<String, String> getSimilarCategories(String category) {
		Map<String, String> similarPowders = new HashMap<>();
		for (String otherCategory : this.categories.keySet()) {
			if (otherCategory.toLowerCase().contains(category.toLowerCase())) {
				similarPowders.put(otherCategory, this.categories.get(otherCategory));
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
		List<Powder> addedPowders = new ArrayList<>();
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
		List<Powder> addedPowders = new ArrayList<>();
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
