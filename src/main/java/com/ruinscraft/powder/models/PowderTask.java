package com.ruinscraft.powder.models;

import java.util.List;
import java.util.UUID;

public class PowderTask {

	// player associated with this PowderTask
	private UUID player;
	// taskIDs associated with this PowderTask
	private List<Integer> tasks;
	// Powder associated with this PowderTask
	private Powder powder;

	public PowderTask(UUID player, List<Integer> tasks, Powder powder) {
		this.player = player;
		this.tasks = tasks;
		this.powder = powder;
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public List<Integer> getTaskIds() {
		return tasks;
	}

	public Powder getPowder() {
		return powder;
	}

	public void addTask(Integer task) {
		tasks.add(task);
	}

	public void removeTask(Integer task) {
		tasks.remove(task);
	}

}
