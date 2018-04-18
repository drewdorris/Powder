package com.ruinscraft.powder.models.powders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PlayerPowder extends Powder {

	private UUID playerUUID;

	public PlayerPowder(Powder powder) {
		super(powder);
	}

	public void setPlayer(UUID uuid) {
		this.playerUUID = uuid;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	@Override
	public Location getCurrentLocation() {
		return Bukkit.getPlayer(playerUUID).getEyeLocation();
	}

}
