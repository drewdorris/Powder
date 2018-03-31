package com.ruinscraft.powder.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PowderTask {

	// player associated with this PowderTask
	private UUID player;
	// taskIDs associated with this PowderTask
	private Set<Integer> tasks;
	// Powder associated with this PowderTask
	private Powder powder;
	
	public PowderTask(UUID player, Powder powder) {
		this.player = player;
		this.tasks = new HashSet<Integer>();
		this.powder = powder;
	}

	public PowderTask(UUID player, Set<Integer> tasks, Powder powder) {
		this.player = player;
		this.tasks = tasks;
		this.powder = powder;
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public Set<Integer> getTaskIds() {
		return tasks;
	}

	public Powder getPowder() {
		return powder;
	}

	public void addTask(Integer task) {
		tasks.add(task);
	}
	
	public void addTasks(Set<Integer> tasks) {
		this.tasks.addAll(tasks);
	}

	public void removeTask(Integer task) {
		tasks.remove(task);
	}

}
