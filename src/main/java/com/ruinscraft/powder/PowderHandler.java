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

	private List<Powder> powders;
	private Set<PowderTask> powderTasks;
	private Map<String, String> categories;
	private boolean categoriesEnabled;

	public PowderHandler() {
		powders = new ArrayList<Powder>();
		powderTasks = new HashSet<PowderTask>();
		categories = new HashMap<String, String>();
	}

	public void clearEverything() {
		powders = null;
		clearAllTasks();
	}

	public List<Powder> getPowders() {
		return powders;
	}

	public List<Powder> getSimilarPowders(String string) {
		List<Powder> similarPowders = new ArrayList<Powder>();
		for (Powder powder : powders) {
			if (powder.getName().toLowerCase().contains(string.toLowerCase())) {
				similarPowders.add(powder);
			}
		}
		return similarPowders;
	}

	public Powder getPowder(String name) {
		for (Powder powder : powders) {
			if (powder.getName().equalsIgnoreCase(name)) {
				return powder;
			}
		}
		return null;
	}

	public void addPowder(Powder powder) {
		if (powders == null) {
			return;
		}
		powders.add(powder);
	}

	public Set<PowderTask> getPowderTasks() {
		return powderTasks;
	}

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

	public Set<PowderTask> getPowderTasks(UUID uuid) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getPlayerUUID().equals(uuid)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	public Set<PowderTask> getPowderTasks(UUID uuid, Powder powder) {
		Set<PowderTask> playerPowderTasks = new HashSet<>();
		for (PowderTask powderTask : getPowderTasks(uuid)) {
			if (powderTask.getMap().equals(powder)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	public Set<Player> getPowderTaskUsers(Powder powder) {
		Set<Player> players = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getMap().equals(powder)) {
				players.add(Bukkit.getPlayer(powderTask.getPlayerUUID()));
			}
		}
		return players;
	}

	public Set<Player> getAllPowderTaskUsers() {
		Set<Player> players = new HashSet<>();
		for (PowderTask powderTask : powderTasks) {
			players.add(Bukkit.getPlayer(powderTask.getPlayerUUID()));
		}
		return players;
	}

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

	public void addPowderTask(PowderTask powderTask) {
		powderTasks.add(powderTask);
	}

	public void removePowderTask(PowderTask powderTask) {
		for (Integer taskID : powderTask.getTaskIds()) {
			PowderPlugin.getInstance().getServer().getScheduler().cancelTask(taskID);
		}
		powderTasks.remove(powderTask);
	}

	public Map<String, String> getCategories() {
		return categories;
	}

	public String getCategory(String category) {
		for (String otherCategory : categories.keySet()) {
			if (otherCategory.equalsIgnoreCase(category)) {
				return otherCategory;
			}
		}
		return null;
	}

	public Map<String, String> getSimilarCategories(String string) {
		Map<String, String> similarPowders = new HashMap<String, String>();
		for (String category : categories.keySet()) {
			if (category.toLowerCase().contains(string.toLowerCase())) {
				similarPowders.put(category, categories.get(category));
			}
		}
		return similarPowders;
	}

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

	public void addCategory(String category, String description) {
		this.categories.put(category, PowderUtil.color(description
				.replace("{total}", String.valueOf(getPowdersFromCategory(category).size()))));
	}

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
