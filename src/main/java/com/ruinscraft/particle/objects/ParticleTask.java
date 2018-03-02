package com.ruinscraft.particle.objects;

import org.bukkit.entity.Player;

public class ParticleTask {
	
	private Player player;
	private int task;
	private ParticleMap map;
	
	public ParticleTask(Player player, int task, ParticleMap map) {
		this.player = player;
		this.task = task;
		this.map = map;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getTaskId() {
		return task;
	}
	
	public ParticleMap getMap() {
		return map;
	}
	
}
