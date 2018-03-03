package com.ruinscraft.particle;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ruinscraft.particle.objects.ParticleMap;
import com.ruinscraft.particle.objects.ParticleTask;

public class ParticleHandler {
	
	private List<ParticleMap> particleMaps;
	private List<ParticleTask> particleTasks;
	
	public ParticleHandler() {
		particleMaps = new ArrayList<>();
		particleTasks = new ArrayList<>();
	}
	
	public List<ParticleMap> getParticleMaps() {
		return particleMaps;
	}
	
	public void addParticleMap(ParticleMap ps) {
		particleMaps.add(ps);
	}
	
	public ParticleMap getParticleMap(String name) {
		for (ParticleMap pmap : particleMaps) {
			if (pmap.getName().equalsIgnoreCase(name)) {
				return pmap;
			}
		}
		return null;
	}
	
	public List<ParticleTask> getParticleTasks() {
		return particleTasks;
	}
	
	public void clearAllTasks() {
		for (ParticleTask ptask : particleTasks) {
			for (Integer taskid : ptask.getTaskIds()) {
				RCParticle.getInstance().getServer().getScheduler().cancelTask(taskid);
			}
		}
	}
	
	public void addParticleTask(ParticleTask task) {
		List<ParticleTask> existingTasks = getParticleTasks(task.getPlayer(), task.getMap());
		if (existingTasks.isEmpty()) {
			particleTasks.add(task);
		} else {
			for (Integer taskid : task.getTaskIds()) {
				existingTasks.get(0).addTask(taskid);
			}
		}
	}
	
	public void removeParticleTask(ParticleTask ptask) {
		for (Integer taskid : ptask.getTaskIds()) {
			RCParticle.getInstance().getServer().getScheduler().cancelTask(taskid);
		}
		particleTasks.remove(ptask);
	}
	
	public ParticleTask getParticleTask(int task) {
		for (ParticleTask ptask : particleTasks) {
			for (Integer taskid : ptask.getTaskIds()) {
				if (taskid == task) {
					return ptask;
				}
			}
		}
		return null;
	}
	
	public List<ParticleTask> getParticleTasks(Player player) {
		List<ParticleTask> ptasks = new ArrayList<>();
		for (ParticleTask ptask : particleTasks) {
			if (ptask.getPlayer().equals(player)) {
				ptasks.add(ptask);
			}
		}
		return ptasks;
	}
	
	public List<ParticleTask> getParticleTasks(Player player, ParticleMap map) {
		List<ParticleTask> ptasks = new ArrayList<>();
		for (ParticleTask ptask : getParticleTasks(player)) {
			if (ptask.getMap().equals(map)) {
				ptasks.add(ptask);
			}
		}
		return ptasks;
	}
	
	public List<Player> getParticleTaskUsers(ParticleMap map) {
		List<Player> players = new ArrayList<>();
		for (ParticleTask ptask : particleTasks) {
			if (ptask.getMap().equals(map)) {
				players.add(ptask.getPlayer());
			}
		}
		return players;
	}
	
}
