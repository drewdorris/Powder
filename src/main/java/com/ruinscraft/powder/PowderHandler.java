package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.models.PowderTask;
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

	// gets PowderTask from bukkit taskID
	public PowderTask getPowderTask(int taskID) {
		for (PowderTask powderTask : powderTasks) {
			for (Integer otherTaskID : powderTask.getTaskIds()) {
				if (otherTaskID == taskID) {
					return powderTask;
				}
			}
		}
		return null;
	}

	// gets all current PowderTasks under a player
	public Set<PowderTask> getPowderTasks(UUID uuid) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getPlayerUUID().equals(uuid)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	// gets all current PowderTasks under a player & Powder
	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : getPowderTasks(uuid)) {
			if (powderTask.getPowder().equals(powder)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	// gets all current players using a Powder
	public Set<Player> getPowderTaskUsers(Powder powder) {
		Set<Player> players = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getPowder().equals(powder)) {
				players.add(Bukkit.getPlayer(powderTask.getPlayerUUID()));
			}
		}
		return players;
	}

	// gets all users who have a running PowderTask
	public Set<Player> getAllPowderTaskUsers() {
		Set<Player> players = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			players.add(Bukkit.getPlayer(powderTask.getPlayerUUID()));
		}
		return players;
	}

	// ends all tasks associated with all PowderTasks
	public void clearAllTasks() {
		for (PowderTask powderTask : powderTasks) {
			for (Integer taskID : powderTask.getTaskIds()) {
				try {
					PowderPlugin.getInstance().getServer().getScheduler().cancelTask(taskID);
				} catch (Exception e) { }
			}
			powderTask = null;
		}
	}

	// adds a PowderTask
	public void addPowderTask(PowderTask powderTask) {
		powderTasks.add(powderTask);
	}

	// removes/ends a PowderTask
	public void removePowderTask(PowderTask powderTask) {
		for (Integer taskID : powderTask.getTaskIds()) {
			PowderPlugin.getInstance().getServer().getScheduler().cancelTask(taskID);
		}
		powderTasks.remove(powderTask);
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

	// adds a category
	public void addCategory(String category, String description) {
		this.categories.put(category, PowderUtil.color(description
				.replace("{total}", String.valueOf(getPowdersFromCategory(category).size()))));
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
