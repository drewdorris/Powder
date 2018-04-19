package com.ruinscraft.powder.models.tasks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.ruinscraft.powder.models.powders.Powder;

public class PlayerPowderTask implements PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// UUID associated with the Player this PowderTask follows
	private UUID uuid;
	// Powder associated with this PowderTask
	private Powder powder;

	public PlayerPowderTask(UUID uuid, Powder powder) {
		this.name = null;
		this.uuid = uuid;
		this.powder = powder;
	}

	public PlayerPowderTask(String name, UUID uuid, Powder powder) {
		this.name = name;
		this.uuid = uuid;
		this.powder = powder;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public Powder getPowder() {
		return powder;
	}

	@Override
	public Location getCurrentLocation() {
		return Bukkit.getPlayer(uuid).getEyeLocation();
	}

}
