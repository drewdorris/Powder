package com.ruinscraft.particle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import com.ruinscraft.particle.objects.ParticleMap;
import com.ruinscraft.particle.objects.ParticleTask;

public class ParticleHandler {
	
	private Set<ParticleMap> particleMaps;
	private Set<ParticleTask> particleTasks;
	
	public ParticleHandler() {
		particleMaps = new HashSet<>();
		particleTasks = new HashSet<>();
	}
	
	public Set<ParticleMap> getParticleMaps() {
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
	
	public Set<ParticleTask> getParticleTasks() {
		return particleTasks;
	}
	
	public void clearAllTasks() {
		for (ParticleTask ptask : particleTasks) {
			RCParticle.getInstance().getServer().getScheduler().cancelTask(ptask.getTaskId());
		}
	}
	
	public void addParticleTask(ParticleTask task) {
		particleTasks.add(task);
	}
	
	public void removeParticleTask(ParticleTask task) {
		RCParticle.getInstance().getServer().getScheduler().cancelTask(task.getTaskId());
		particleTasks.remove(task);
	}
	
	public ParticleTask getParticleTask(int task) {
		for (ParticleTask ptask : particleTasks) {
			if (ptask.getTaskId() == task) {
				return ptask;
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
