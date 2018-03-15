package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.PowderTask;

public class PowderHandler {
	
	private List<PowderMap> powderMaps;
	private List<PowderTask> powderTasks;
	
	public PowderHandler() {
		powderMaps = new ArrayList<>();
		powderTasks = new ArrayList<>();
	}
	
	public List<PowderMap> getPowderMaps() {
		return powderMaps;
	}
	
	public void addPowderMap(PowderMap ps) {
		powderMaps.add(ps);
	}
	
	public PowderMap getPowderMap(String name) {
		for (PowderMap pmap : powderMaps) {
			if (pmap.getName().equalsIgnoreCase(name)) {
				return pmap;
			}
		}
		return null;
	}
	
	public List<PowderTask> getPowderTasks() {
		return powderTasks;
	}
	
	public void clearEverything() {
		powderMaps = null;
		clearAllTasks();
	}
	
	public void clearAllTasks() {
		for (PowderTask ptask : powderTasks) {
			for (Integer taskid : ptask.getTaskIds()) {
				Powder.getInstance().getServer().getScheduler().cancelTask(taskid);
			}
			ptask = null;
		}
	}
	
	public void addPowderTask(PowderTask task) {
		powderTasks.add(task);
	}
	
	public void removePowderTask(PowderTask ptask) {
		for (Integer taskid : ptask.getTaskIds()) {
			Powder.getInstance().getServer().getScheduler().cancelTask(taskid);
		}
		powderTasks.remove(ptask);
	}
	
	public PowderTask getPowderTask(int task) {
		for (PowderTask ptask : powderTasks) {
			for (Integer taskid : ptask.getTaskIds()) {
				if (taskid == task) {
					return ptask;
				}
			}
		}
		return null;
	}
	
	public List<PowderTask> getPowderTasks(Player player) {
		List<PowderTask> ptasks = new ArrayList<>();
		for (PowderTask ptask : powderTasks) {
			if (ptask.getPlayer().equals(player)) {
				ptasks.add(ptask);
			}
		}
		return ptasks;
	}
	
	public List<PowderTask> getPowderTasks(Player player, PowderMap map) {
		List<PowderTask> ptasks = new ArrayList<>();
		for (PowderTask ptask : getPowderTasks(player)) {
			if (ptask.getMap().equals(map)) {
				ptasks.add(ptask);
			}
		}
		return ptasks;
	}
	
	public List<Player> getPowderTaskUsers(PowderMap map) {
		List<Player> players = new ArrayList<>();
		for (PowderTask ptask : powderTasks) {
			if (ptask.getMap().equals(map)) {
				players.add(ptask.getPlayer());
			}
		}
		return players;
	}
	
}
