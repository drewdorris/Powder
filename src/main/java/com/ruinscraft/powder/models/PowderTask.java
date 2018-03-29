package com.ruinscraft.powder.models;

import java.util.List;
import java.util.UUID;

public class PowderTask {

	private UUID player;
	private List<Integer> tasks;
	private Powder map;

	public PowderTask(UUID player, List<Integer> tasks, Powder map) {
		this.player = player;
		this.tasks = tasks;
		this.map = map;
	}

	public UUID getPlayerUUID() {
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
