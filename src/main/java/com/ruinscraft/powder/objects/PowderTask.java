package com.ruinscraft.powder.objects;

import java.util.List;

import org.bukkit.entity.Player;

public class PowderTask {
	
	private Player player;
	private List<Integer> tasks;
	private Powder map;
	
	public PowderTask(Player player, List<Integer> tasks, Powder map) {
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
	
	public Powder getMap() {
		return map;
	}
	
	public void addTask(Integer task) {
		tasks.add(task);
	}
	
	public void removeTask(Integer task) {
		tasks.remove(task);
	}
	
}
