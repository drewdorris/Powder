package com.ruinscraft.powder.integration;

import java.util.Map.Entry;
import java.util.UUID;

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
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.Tracker;

/**
 * Handles Towny-related features and events
 *
 */
public class TownyHandler implements Listener {

	private TownyAPI townyAPI;

	public TownyHandler() {
		this.townyAPI = TownyAPI.getInstance();
	}

	/**
	 * Checks if location is safe with Towny to put a Powder in
	 * @param player
	 * @param location
	 * @return if safe to place Powder
	 */
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

		// check if above player limit

		return true;
	}

	@EventHandler
	public void onTownUnclaimEvent(TownUnclaimEvent event) {
		WorldCoord worldCoord = event.getWorldCoord();
		Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(worldCoord.getX(), worldCoord.getZ());

		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getPowderTasks()) {
			if (powderTask.getTrackerType() != Tracker.Type.STATIONARY) continue; 
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				Location trackerSpot = tracker.getCurrentLocation();
				if (trackerSpot.getChunk().equals(chunk)) {
					powderTask.removePowder(entry.getKey());
				}
			}
		}
	}

	@EventHandler
	public void onPreDeleteTownEvent(PreDeleteTownEvent event) {
		Town town = event.getTown();

		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getCreatedPowderTasks()) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Powder powder = entry.getKey();
				Tracker tracker = entry.getValue();

				UUID townUUID = townyAPI.getTownUUID(tracker.getCurrentLocation());

				if (town.getUuid().equals(townUUID)) {
					powderTask.removePowder(powder);
				}
			}
		}
	}

	@EventHandler
	public void onTownRemoveResidentEvent(TownRemoveResidentEvent event) {
		Town town = event.getTown();
		Player player = townyAPI.getPlayer(event.getResident());

		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getCreatedPowderTasks(player)) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Powder powder = entry.getKey();
				Tracker tracker = entry.getValue();

				UUID townUUID = townyAPI.getTownUUID(tracker.getCurrentLocation());

				if (town.getUuid().equals(townUUID)) {
					powderTask.removePowder(powder);
				}
			}
		}
	}

}
