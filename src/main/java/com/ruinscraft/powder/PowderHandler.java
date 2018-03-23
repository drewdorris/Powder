package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.Powder;
import com.ruinscraft.powder.objects.PowderTask;

public class PowderHandler {

	private List<Powder> powders;
	private List<PowderTask> powderTasks;

	public PowderHandler() {
		powders = new ArrayList<>();
		powderTasks = new ArrayList<>();
	}

	public List<Powder> getPowders() {
		return powders;
	}

	public void addPowder(Powder powder) {
		powders.add(powder);
	}

	public Powder getPowder(String name) {
		for (Powder powder : powders) {
			if (powder.getName().equalsIgnoreCase(name)) {
				return powder;
			}
		}
		return null;
	}

	public List<PowderTask> getPowderTasks() {
		return powderTasks;
	}

	public void clearEverything() {
		powders = null;
		clearAllTasks();
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

	public List<PowderTask> getPowderTasks(Player player) {
		List<PowderTask> playerPowderTasks = new ArrayList<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getPlayer().equals(player)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	public List<PowderTask> getPowderTasks(Player player, Powder powder) {
		List<PowderTask> playerPowderTasks = new ArrayList<>();
		for (PowderTask powderTask : getPowderTasks(player)) {
			if (powderTask.getMap().equals(powder)) {
				playerPowderTasks.add(powderTask);
			}
		}
		return playerPowderTasks;
	}

	public List<Player> getPowderTaskUsers(Powder powder) {
		List<Player> players = new ArrayList<>();
		for (PowderTask powderTask : powderTasks) {
			if (powderTask.getMap().equals(powder)) {
				players.add(powderTask.getPlayer());
			}
		}
		return players;
	}

}
