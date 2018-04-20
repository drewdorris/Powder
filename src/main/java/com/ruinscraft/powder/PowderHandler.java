package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	// initialize
	public PowderHandler() {
		powders = new ArrayList<Powder>();
		powderTasks = new HashSet<PowderTask>();
		categories = new HashMap<String, String>();
	}

	// clear all Powders and end all PowderTasks
	public void clearEverything() {
		powders = null;
		clearAllTasks();
	}

	public List<Powder> getPowders() {
		return powders;
	}

	// return Powders that contain the string in their name
	public List<Powder> getSimilarPowders(String string) {
		List<Powder> similarPowders = new ArrayList<Powder>();
		for (Powder powder : powders) {
			if (powder.getName().toLowerCase().contains(string.toLowerCase())) {
				similarPowders.add(powder);
			}
		}
		return similarPowders;
	}

	// gets Powder from name
	public Powder getPowder(String name) {
		for (Powder powder : powders) {
			if (powder.getName().equalsIgnoreCase(name)) {
				return powder;
			}
		}
		return null;
	}

	// adds Powder to the complete list
	public void addPowder(Powder powder) {
		if (powders == null) {
			return;
		}
		powders.add(powder);
	}

	public Set<PowderTask> getPowderTasks() {
		return powderTasks;
	}

	// gets all current PowderTasks under a player
	public Set<PowderTask> getPowderTasks(UUID uuid) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			for (Powder powder : powderTask.getPowders().keySet()) {
				Tracker tracker = powderTask.getPowders().get(powder);
				if (tracker.getType() == TrackerType.PLAYER) {
					playerPowderTasks.add(powderTask);
					break;
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all current PowderTasks under a player & Powder
	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			for (Powder otherPowder : powderTask.getPowders().keySet()) {
				Tracker tracker = powderTask.getPowders().get(otherPowder);
				if (tracker.getType() == TrackerType.PLAYER && powder.getName().equals(otherPowder.getName())) {
					playerPowderTasks.add(powderTask);
					break;
				}
			}
		}
		return playerPowderTasks;
	}

	// gets all users who have a running PowderTask
	public Set<UUID> getAllPowderTaskUsers() {
		Set<UUID> players = new HashSet<>();
		players.addAll(getCurrentPlayerTracks().values().stream().map(PlayerTracker::getUUID).collect(Collectors.toSet()));
		return players;
	}

	public PowderTask getPowderTask(String name) {
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getName() == null) {
				continue;
			}
			if (powderTask.getName().equals(name) || powderTask.getName() == null && name == null) {
				return powderTask;
			}
		}
		return null;
	}

	public Map<PowderTask, Integer> getNearbyPowderTasks(Location location, int range) {
		Map<PowderTask, Integer> nearbyPowderTasks = new HashMap<PowderTask, Integer>();
		for (PowderTask powderTask : powderTasks) {
			int taskRange = Integer.MAX_VALUE;
			for (Powder powder : powderTask.getPowders().keySet()) {
				int distance = (int) location.distance(powderTask.getPowders().get(powder).getCurrentLocation());
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
		for (PowderTask powderTask : powderTasks) {
			for (Powder powder : powderTask.getPowders().keySet()) {
				Tracker tracker = powderTask.getPowders().get(powder);
				if (tracker.getType() == TrackerType.PLAYER) {
					PlayerTracker playerTracker = (PlayerTracker) tracker;
					playerTracks.put(powder, playerTracker);
					break;
				}
			}
		}
		return playerTracks;
	}

	public Map<Powder, EntityTracker> getCurrentEntityTracks() {
		Map<Powder, EntityTracker> entityTracks = new HashMap<Powder, EntityTracker>();
		for (PowderTask powderTask : powderTasks) {
			for (Powder powder : powderTask.getPowders().keySet()) {
				Tracker tracker = powderTask.getPowders().get(powder);
				if (tracker.getType() == TrackerType.ENTITY) {
					EntityTracker entityTracker = (EntityTracker) tracker;
					entityTracks.put(powder, entityTracker);
					break;
				}
			}
		}
		return entityTracks;
	}

	public Map<Powder, StationaryTracker> getCurrentStationaryTracks() {
		Map<Powder, StationaryTracker> stationaryTracks = new HashMap<Powder, StationaryTracker>();
		for (PowderTask powderTask : powderTasks) {
			for (Powder powder : powderTask.getPowders().keySet()) {
				Tracker tracker = powderTask.getPowders().get(powder);
				if (tracker.getType() == TrackerType.ENTITY) {
					StationaryTracker stationaryTracker = (StationaryTracker) tracker;
					stationaryTracks.put(powder, stationaryTracker);
					break;
				}
			}
		}
		return stationaryTracks;
	}

	public PowderTask getPowderTask(Powder powder, Tracker tracker) {
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getPowders().containsKey(powder) && powderTask.getPowders().containsValue(tracker)) {
				return powderTask;
			}
		}
		return null;
	}

	// ends all tasks associated with all PowderTasks
	public void clearAllTasks() {
		powderTasks.clear();
	}

	// adds a PowderTask
	public void addPowderTask(PowderTask powderTask) {
		for (Powder powder : powderTask.getPowders().keySet()) {
			for (PowderElement powderElement : powder.getPowderElements().keySet()) {
				if (powderElement.getLockedIterations() == 0) {
					powderElement.setLockedIterations(Integer.MAX_VALUE);
				}
			}
		}
		powderTasks.add(powderTask);
	}

	// removes/ends a PowderTask
	public boolean removePowderTask(PowderTask powderTask) {
		return powderTasks.remove(powderTask);
	}

	// removes/ends a PowderTask
	public boolean removePowderTasks(List<PowderTask> powderTasks) {
		return powderTasks.removeAll(powderTasks);
	}

	public Map<String, String> getCategories() {
		return categories;
	}

	// gets a category from string
	public String getCategory(String category) {
		for (String otherCategory : categories.keySet()) {
			if (otherCategory.equalsIgnoreCase(category)) {
				return otherCategory;
			}
		}
		return null;
	}

	// gets categories which contain the given string
	public Map<String, String> getSimilarCategories(String string) {
		Map<String, String> similarPowders = new HashMap<String, String>();
		for (String category : categories.keySet()) {
			if (category.toLowerCase().contains(string.toLowerCase())) {
				similarPowders.put(category, categories.get(category));
			}
		}
		return similarPowders;
	}

	// gets Powders under a given category
	public List<Powder> getPowdersFromCategory(String category) {
		for (String otherCategory : categories.keySet()) {
			if (otherCategory.toLowerCase().equals(category.toLowerCase())) {
				category = otherCategory;
				break;
			}
		}
		List<Powder> addedPowders = new ArrayList<Powder>();
		for (Powder powder : powders) {
			if (powder.getCategories().contains(category)) {
				addedPowders.add(powder);
			}
		}
		return addedPowders;
	}

	// gets unhidden Powders under a given category
	public List<Powder> getUnhiddenPowdersFromCategory(String category) {
		for (String otherCategory : categories.keySet()) {
			if (otherCategory.toLowerCase().equals(category.toLowerCase())) {
				category = otherCategory;
				break;
			}
		}
		List<Powder> addedPowders = new ArrayList<Powder>();
		for (Powder powder : powders) {
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
				.replace("{total-hidden}", String.valueOf(getUnhiddenPowdersFromCategory(category).size()))));
	}

	// sets description for a given category
	public void setDescription(String category, String description) {
		this.categories.put(category, PowderUtil.color(description
				.replace("{total}", String.valueOf(getPowdersFromCategory(category).size()))));
	}

	public boolean categoriesEnabled() {
		return categoriesEnabled;
	}

	public void setIfCategoriesEnabled(boolean categoriesEnabled) {
		this.categoriesEnabled = categoriesEnabled;
	}

}
