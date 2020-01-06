package com.ruinscraft.powder.integration;

import java.util.Map.Entry;
import java.util.Arrays;
import java.util.List;
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
import com.palmergames.bukkit.towny.object.TownyPermission;
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
	private int maxPerTown;

	public TownyHandler(int maxPerTown) {
		this.townyAPI = TownyAPI.getInstance();
		this.maxPerTown = maxPerTown;
	}

	/**
	 * Max amount of Powders a player can place per town
	 * @return int
	 */
	public int getMaxPerTown() {
		return this.maxPerTown;
	}

	/**
	 * Checks if location is safe with Towny to put a Powder in
	 * @param player
	 * @param location
	 * @return if safe to place Powder
	 */
	public boolean checkLocation(Player player, Location location) {
		TownBlock block = townyAPI.getTownBlock(location);

		if (block == null || !block.hasTown()) return false;
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

		if (!hasPermissionForPowder(town, location, resident)) return false;
		if (!canPlacePowdersInTown(town, location)) return false;

		// check if above player limit
		List<PowderTask> userCreatedPowders = PowderPlugin.get().getPowderHandler().getCreatedPowderTasks(player);
		if (userCreatedPowders.size() > PowderPlugin.get().getMaxCreatedPowders()) return false;

		int amntInTown = 0;
		for (PowderTask powderTask : userCreatedPowders) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();

				Location trackerLocation = tracker.getCurrentLocation();
				if (townyAPI.getTownUUID(trackerLocation) != null) {
					if (townyAPI.getTownUUID(trackerLocation).equals(town.getUuid())) {
						amntInTown++;
					}
				}

			}
		}
		if (amntInTown > this.maxPerTown) return false; 

		return true;
	}

	/**
	 * Check whether a player has permission to place Powders in this Town
	 * @param town
	 * @param player
	 * @return if a player has permission to place Powders in this Town
	 */
	public boolean hasPermissionForPowder(Town town, Location location, Resident resident) {
		if (town == null) return false;

		if (town.getMayor().equals(resident)) return true;
		for (Resident assistant : town.getAssistants()) {
			if (assistant.equals(resident)) return true;
		}

		try {
			if (checkPerms(resident, town, town.getPermissions())) return true;

			TownBlock block = townyAPI.getTownBlock(location);
			if (block == null || block.getPermissions() == null) return false;
			if (checkPerms(resident, town, block.getPermissions())) return true;
		} catch (NotRegisteredException e) {
			return false;
		}

		return false;
	}

	/**
	 * Checks if resident has permissions in this area to build
	 * @param town
	 * @param permission
	 * @param resident
	 * @return
	 * @throws NotRegisteredException
	 */
	public boolean checkPerms(Resident resident, Town town, TownyPermission permission) throws NotRegisteredException {
		if (permission.residentBuild && resident.getTown().equals(town)) return true;
		if (permission.nationBuild && resident.getTown().getNation().equals(town.getNation())) return true;
		if (permission.allyBuild && resident.isAlliedWith(town.getMayor())) return true;
		if (permission.outsiderBuild) return true;
		return false;
	}

	/**
	 * Checks if Powder is too close to the edge of the Town
	 * @param town
	 * @param location
	 * @return if the Powder is too close to the edge of the Town
	 */
	public boolean canPlacePowdersInTown(Town town, Location location) {
		// check if too close to side, like in P2Handler
		return true;
	}

	/**
	 * Checks distance from location to edge of town
	 * If > 5, returns Double.MAX_VALUE
	 * @param town
	 * @param location
	 * @return distance from location to edge of town
	 */
	public double getDistanceToEdgeOfTown(Town town, Location location) {
		TownBlock block = townyAPI.getTownBlock(location);
		if (block == null) return 0;
		if (!block.hasTown()) return 0;

		TownBlock bOne = townyAPI.getTownBlock(location.clone().add(-16, 0, 16));
		TownBlock bTwo = townyAPI.getTownBlock(location.clone().add(16, 0, 0));
		TownBlock bThree = townyAPI.getTownBlock(location.clone().add(16, 0, 0));
		TownBlock bFour = townyAPI.getTownBlock(location.clone().add(0, 0, -16));
		TownBlock bFive = townyAPI.getTownBlock(location.clone().add(0, 0, -16));
		TownBlock bSix = townyAPI.getTownBlock(location.clone().add(-16, 0, 0));
		TownBlock bSeven = townyAPI.getTownBlock(location.clone().add(-16, 0, 0));
		TownBlock bEight = townyAPI.getTownBlock(location.clone().add(0, 0, 16));
		if (isInTown(town, block, bOne, bTwo, bThree, bFour, bFive, bSix, bSeven, bEight)) {
			// more than 16 doesn't matter
			return Double.MAX_VALUE;
		} else {
			for (int i = 1; i < 5; i++) {
				TownBlock blockOne = townyAPI.getTownBlock(new Location(location.getWorld(), 
						location.getX() + i, location.getY(), location.getZ() + i));
				TownBlock blockTwo = townyAPI.getTownBlock(new Location(location.getWorld(), 
						location.getX() + i, location.getY(), location.getZ() - i));
				TownBlock blockThree = townyAPI.getTownBlock(new Location(location.getWorld(), 
						location.getX() - i, location.getY(), location.getZ() + i));
				TownBlock blockFour = townyAPI.getTownBlock(new Location(location.getWorld(), 
						location.getX() - i, location.getY(), location.getZ() - i));
				if (!isInTown(town, blockOne, blockTwo, blockThree, blockFour)) {
					return i;
				}
			}
		}
		// more than 5 doesn't matter
		return Double.MAX_VALUE;
	}

	/**
	 * Checks if an array of locations are all in the Town
	 * @param Town
	 * @param locations
	 * @return if all the locations are in the Town
	 */
	public boolean isInTown(Town town, Location... locations) {
		return isInTown(town, 
				(TownBlock[]) Arrays.asList(locations).stream().map(
						location -> townyAPI.getTownBlock(location)).toArray());
	}

	/**
	 * Checks if an array of TownBlocks are all in the Town
	 * @param Town
	 * @param townBlocks
	 * @return if all the locations are in the Town
	 */
	public boolean isInTown(Town town, TownBlock... townBlocks) {
		for (TownBlock townBlock : townBlocks) {
			if (townBlock == null) return false;
			if (!townBlock.hasTown()) return false;
			try {
				if (!townBlock.getTown().equals(town)) return false;
			} catch (NotRegisteredException e) {
				return false;
			}
		}
		return true;
	}

	// removes Powders that end up in the wilderness after a Town unclaims an area
	@EventHandler
	public void onTownUnclaimEvent(TownUnclaimEvent event) {
		WorldCoord worldCoord = event.getWorldCoord();
		Chunk chunk = Bukkit.getWorlds().get(0).getChunkAt(worldCoord.getX(), worldCoord.getZ());

		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getCreatedPowderTasks()) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();
				Location trackerSpot = tracker.getCurrentLocation();
				if (townyAPI.getTownUUID(trackerSpot) == null) {
					powderTask.removePowder(entry.getKey());
					continue;
				}
				if (trackerSpot.getChunk().equals(chunk)) {
					powderTask.removePowder(entry.getKey());
				}
			}
		}
	}

	// removes Powders that were in a Town that is now being deleted
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

	// removes Powders from a Town that removes the owner of those Powders
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
