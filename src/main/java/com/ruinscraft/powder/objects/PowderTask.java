package com.ruinscraft.particle.objects;

import java.util.List;

import org.bukkit.entity.Player;

public class ParticleTask {
	
	private Player player;
	private List<Integer> tasks;
	private ParticleMap map;
	
	public ParticleTask(Player player, List<Integer> tasks, ParticleMap map) {
		this.player = player;
		this.tasks = tasks;
		this.map = map;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public List<Integer> getTaskIds() {
		return tasks;
	}
	
	public ParticleMap getMap() {
		return map;
	}
	
	public void addTask(Integer task) {
		tasks.add(task);
	}
	
}
