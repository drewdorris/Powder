package com.ruinscraft.powder.integration;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.PreDeleteTownEvent;
import com.palmergames.bukkit.towny.event.TownRemoveResidentEvent;
import com.palmergames.bukkit.towny.event.TownUnclaimEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.Tracker;

public class TownyHandler implements Listener {

	private TownyAPI townyAPI;

	public TownyHandler() {
		this.townyAPI = TownyAPI.getInstance();
	}

	// checks if location is safe with Towny to put a Powder in
	public boolean checkLocation(Player player, Location location) {
		TownBlock block = townyAPI.getTownBlock(location);

		if (!block.hasTown()) return false;
		Town town;
		Resident resident;
		try {
			town = block.getTown();
			resident = townyAPI.getDataSource().getResident(player.getName());
		} catch (NotRegisteredException e) {
			return false;
		}
		if (!town.hasResident(player.getName())) return false;
		if (!townyAPI.isActiveResident(resident)) return false;

		return true;
	}

	@EventHandler
	public void onTownUnclaimEvent(TownUnclaimEvent event) {
		WorldCoord worldCoord = event.getWorldCoord();
		Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(worldCoord.getX(), worldCoord.getZ());

		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getPowderTasks()) {
			if (powderTask.getTrackerType() != Tracker.Type.STATIONARY) continue; 
			for (Tracker tracker : powderTask.getPowders().values()) {
				Location trackerSpot = tracker.getCurrentLocation();
				if (trackerSpot.getChunk().equals(chunk)) {
					powderTask.cancel();
				}
			}
		}
	}

	@EventHandler
	public void onPreDeleteTownEvent(PreDeleteTownEvent event) {
		// go through chunks in the town and do checks
	}

	@EventHandler
	public void onTownRemoveResidentEvent(TownRemoveResidentEvent event) {
		// go through powderHandler.getCreatedPowderTasks() and see if any are in the town
	}

}
