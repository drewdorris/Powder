package com.ruinscraft.powder;

import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderElement;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.EntityTracker;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.model.tracker.Tracker;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

		entitiesToLoad = new HashSet<UUID>();
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (entity == null) continue;
				this.entitiesToLoad.add(entity.getUniqueId());
			}
		}
		PowderPlugin.get().getServer()
		.getScheduler().runTaskTimer(PowderPlugin.get(), () -> {
			if (PowderPlugin.isLoading()) {
				return;
			}
			ConfigUtil.loadAllAttached(this.entitiesToLoad);
			this.entitiesToLoad.clear();
		}, 0L, 20L);
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
			if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
				EntityTracker entityTracker = (EntityTracker) powderTask.getTracker();
				if (entityTracker.getUUID().equals(uuid) || entityTracker.getCreator().equals(uuid)) {
					playerPowderTasks.add(powderTask);
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all current PowderTasks under a uuid & Powder
	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : this.powderTasks) {
			Tracker tracker = powderTask.getTracker();
			if (powder.getName().equals(powderTask.getPowder().getName())) {
				if (tracker.getType() == Tracker.Type.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					if (entityTracker.getUUID().equals(uuid) || entityTracker.getCreator().equals(uuid)) {
						playerPowderTasks.add(powderTask);
					}
				}
			}
		}
		return playerPowderTasks;
	}

	public List<PowderTask> getCreatedPowderTasks(Player player) {
		return getCreatedPowderTasks(player.getUniqueId());
	}

	public List<PowderTask> getCreatedPowderTasks(UUID uuid) {
		List<PowderTask> powderTasks = new ArrayList<>();
		powderTasks.addAll(this.getStationaryPowderTasks(uuid));

		for (PowderTask powderTask : this.getPowderTasks()) {
			if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
				EntityTracker tracker = (EntityTracker) powderTask.getTracker();
				if (tracker.getCreator().equals(uuid)) {
					powderTasks.add(powderTask);
					continue;
				}
			}
		}

		return powderTasks;
	}

	public List<PowderTask> getStationaryPowderTasks(Player player) {
		return getStationaryPowderTasks(player.getUniqueId());
	}

	public List<PowderTask> getStationaryPowderTasks(UUID uuid) {
		List<PowderTask> powderTasks = new ArrayList<>();

		for (PowderTask powderTask : this.getStationaryPowderTasks()) {
			if (powderTask.getTracker().getType() != Tracker.Type.STATIONARY) continue; 
			StationaryTracker stationaryTracker = (StationaryTracker) powderTask.getTracker();
			UUID trackerUUID = stationaryTracker.getCreator();
			if (trackerUUID.equals(uuid)) {
				powderTasks.add(powderTask);
			}
		}

		return powderTasks;
	}

	public List<PowderTask> getStationaryPowderTasks() {
		List<PowderTask> powderTasks = new ArrayList<>();
		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getPowderTasks()) {
			if (powderTask.getTracker().getType() == Tracker.Type.STATIONARY) {
				powderTasks.add(powderTask);
			}
		}

		return powderTasks;
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
			Location otherLocation = powderTask.getTracker().getCurrentLocation();
			if (!otherLocation.getWorld().equals(location.getWorld())) {
				continue;
			}
			int distance = (int) location.distance(otherLocation);
			if (distance < taskRange) {
				taskRange = distance;
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
			if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
				entityTracks.put(powderTask.getPowder(), (EntityTracker) powderTask.getTracker());
				break;
			}
		}
		return entityTracks;
	}

	public Map<Powder, StationaryTracker> getCurrentStationaryTracks() {
		Map<Powder, StationaryTracker> stationaryTracks = new HashMap<>();
		for (PowderTask powderTask : this.powderTasks) {
			if (powderTask.getTracker().getType() == Tracker.Type.STATIONARY) {
				stationaryTracks.put(powderTask.getPowder(), (StationaryTracker) powderTask.getTracker());
				break;
			}
		}
		return stationaryTracks;
	}

	public PowderTask getPowderTask(Powder powder, Tracker tracker) {
		for (PowderTask powderTask : this.powderTasks) {
			if (powder.equals(powderTask.getPowder()) && 
					tracker.equals(powderTask.getTracker())) return powderTask;
		}
		return null;
	}

	// ends all tasks associated with all PowderTasks
	public void clearAllTasks() {
		this.powderTasks.clear();
	}

	// adds a PowderTask
	public void runPowderTask(PowderTask powderTask) {
		if (powderTask == null || powderTask.getPowder() == null) return;
		for (PowderElement powderElement : powderTask.getPowder().getPowderElements()) {
			if (powderElement.getLockedIterations() == 0) {
				powderElement.setLockedIterations(Integer.MAX_VALUE);
			}
			powderElement.setStartingTick();
		}
		this.powderTasks.add(powderTask);
		if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
			EntityTracker tracker = (EntityTracker) powderTask.getTracker();
			if (tracker.isPlayer()) {
				PowderUtil.savePowdersForUUID(tracker.getUUID());
			} else {
				ConfigUtil.saveAttached(tracker.getEntity(), powderTask);
			}
		}

		if (!ConfigUtil.containsTask(powderTask) && powderTask.getTracker().getType() == Tracker.Type.STATIONARY) {
			ConfigUtil.saveStationaryPowder(
					PowderPlugin.get().getPlayerDataFile(), powderTask);
		}
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTask(PowderTask powderTask) {
		boolean removal = this.powderTasks.remove(powderTask);

		if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
			EntityTracker tracker = (EntityTracker) powderTask.getTracker();
			if (tracker.isPlayer()) {
				PowderUtil.savePowdersForUUID(powderTask.cancel());
			} else {
				powderTask.cancel();
				ConfigUtil.removeAttached(tracker.getUUID());
			}
		} else if (powderTask.getTracker().getType() == Tracker.Type.STATIONARY) { 
			ConfigUtil.removeStationaryPowder(powderTask);
		}
		return removal;
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTaskWithoutSaving(PowderTask powderTask) {
		ConfigUtil.removeStationaryPowder(powderTask);
		if (powderTask.getTracker().getType() == Tracker.Type.ENTITY) {
			EntityTracker tracker = (EntityTracker) powderTask.getTracker();
			ConfigUtil.removeAttached(tracker.getUUID());
		}
		return this.powderTasks.remove(powderTask);
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTasks(Collection<PowderTask> powderTasks) {
		boolean removal = this.powderTasks.removeAll(powderTasks);
		for (PowderTask powderTask : powderTasks) {
			cancelPowderTask(powderTask);
		}
		return removal;
	}

	// removes/ends a PowderTask
	public boolean cancelPowderTasksWithoutSaving(Collection<PowderTask> powderTasks) {
		boolean removal = this.powderTasks.removeAll(powderTasks);
		for (PowderTask powderTask : powderTasks) {
			cancelPowderTaskWithoutSaving(powderTask);
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
