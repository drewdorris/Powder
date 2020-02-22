package com.ruinscraft.powder.integration;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.bukkit.events.PlayerPlotTrustedEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlotClearEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlotDeleteEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlotUnlinkEvent;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.model.tracker.Tracker;

/**
 * Handles PlotSquared-related features and events
 *
 */
public class PlotSquaredHandler implements Listener {

	private PlotAPI plotAPI;
	private int maxPerPlot;

	public PlotSquaredHandler(int maxPerPlot) {
		this.plotAPI = new PlotAPI();
		this.maxPerPlot = maxPerPlot;
	}

	/**
	 * Max amount of Powders a player can place per plot (ignoring merging)
	 * @return int
	 */
	public int getMaxPerPlot() {
		return this.maxPerPlot;
	}

	/**
	 * Checks whether a player can place a Powder at their current location
	 * @param powder
	 * @param player
	 * @return if they can place a Powder
	 */
	public boolean checkLocation(Powder powder, Player player) {
		PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());

		if (!hasPermissionForPowder(plotPlayer)) return false;

		double roadDist = this.getDistanceFromRoad(plotPlayer);
		double powderWidth = powder.maxWidthDistance();
		if (powderWidth > roadDist) return false;

		Plot plot = plotPlayer.getLocation().getPlotAbs();

		List<PowderTask> userCreatedPowders = PowderPlugin.get().getPowderHandler().getCreatedPowderTasks(player);
		if (userCreatedPowders.size() > PowderPlugin.get().getMaxCreatedPowders()) return false;

		int amntInPlot = 0;
		for (PowderTask powderTask : userCreatedPowders) {
			Tracker tracker = powderTask.getTracker();

			org.bukkit.Location bukkitLoc = tracker.getCurrentLocation();
			Location location = new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(),
					bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());

			if (location.getPlotAbs().equals(plot)) amntInPlot++;
		}
		if (amntInPlot > this.maxPerPlot) return false;

		return true;
	}

	/**
	 * Get the name of a location, e.g. "Town of Paris"
	 * 
	 * Returns null if not in town
	 * @param location
	 * @return formatted string
	 */
	public String getFormattedLocation(org.bukkit.Location bukkitLoc) {
		Location location = new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(),
				bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
		Plot plot = location.getPlot();
		if (plot == null) return null;
		if (!plot.hasOwner()) return null;
		for (UUID owner : plot.getOwners()) {
			OfflinePlotPlayer player = plotAPI.getUUIDWrapper().getOfflinePlayer(owner);
			return player.getName() + "'s plot";
		}
		return null;
	}

	/**
	 * Checks if a Powder is fine to be placed here, regardless of player
	 * @param powder
	 * @param location
	 * @return if Powder can be placed here
	 */
	public boolean checkLocationWithoutPlayer(Powder powder, Location location) {
		double roadDist = this.getDistanceFromRoad(location);
		double powderWidth = powder.maxWidthDistance();
		if (powderWidth > roadDist) return false;

		return true;
	}

	/**
	 * Checks whether a player can place Powders in a plot
	 * @param player
	 * @param plot
	 * @return if they can place Powders in the plot
	 */
	public boolean hasPermissionForPowder(UUID player, org.bukkit.Location bukkitLoc) {
		Location location = new Location(bukkitLoc.getWorld().getName(), 
				bukkitLoc.getBlockX(), bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());
		return hasPermissionForPowder(player, location.getPlot());
	}

	/**
	 * Checks whether the player can place Powders in their current plot
	 * @param player
	 * @return if they can place Powders where they are
	 */
	public boolean hasPermissionForPowder(PlotPlayer player) {
		if (player.getCurrentPlot() == null) return false;
		if (player.getLocation().isPlotRoad()) return false;
		return hasPermissionForPowder(player.getUUID(), player.getCurrentPlot());
	}

	/**
	 * Checks whether a player can place Powders in a plot
	 * @param player
	 * @param plot
	 * @return if they can place Powders in the plot
	 */
	public boolean hasPermissionForPowder(UUID player, Plot plot) {
		for (UUID uuid : plot.getTrusted()) {
			if (uuid.equals(player)) return true;
		}
		for (UUID uuid : plot.getOwners()) {
			if (uuid.equals(player)) return true;
		}
		return false;
	}

	/**
	 * Roughly get the distance to the nearest road
	 * @param player
	 * @return distance in blocks to the nearest road
	 */
	public double getDistanceFromRoad(PlotPlayer player) {
		return getDistanceFromRoad(player.getLocation());
	}

	/**
	 * Roughly get the distance to the nearest road
	 * @param location
	 * @param plot
	 * @return distance in blocks to the nearest road
	 */
	public double getDistanceFromRoad(Location location) {
		Plot plot = location.getPlot();
		if (location.isPlotRoad()) return 0;
		if (location.getPlot() != plot) return 0;
		for (int i = 1; i < 5; i++) {
			Location locOne = new Location(location.getWorld(), 
					location.getX() + i, location.getY(), location.getZ() + i);
			Location locTwo = new Location(location.getWorld(), 
					location.getX() + i, location.getY(), location.getZ() - i);
			Location locThree = new Location(location.getWorld(), 
					location.getX() - i, location.getY(), location.getZ() + i);
			Location locFour = new Location(location.getWorld(), 
					location.getX() - i, location.getY(), location.getZ() - i);
			if (!isInPlot(plot, locOne, locTwo, locThree, locFour)) {
				return i;
			}
		}
		// more than 5 is whatever
		return Double.MAX_VALUE;
	}

	/**
	 * Checks if an array of locations are all in the plot
	 * @param plot
	 * @param locations
	 * @return if all the locations are in the plot
	 */
	public boolean isInPlot(Plot plot, Location... locations) {
		for (Location location : locations) {
			if (location.isPlotRoad() || location.getPlot() != plot) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Handles Powders within emptied (cleared/deleted) plots
	 * @param plot
	 */
	public void checkEmptiedPlot(Plot plot) {
		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getStationaryPowderTasks()) {
			Powder powder = powderTask.getPowder();
			Tracker tracker = powderTask.getTracker();

			org.bukkit.Location bukkitLoc = tracker.getCurrentLocation();
			Location location = new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(),
					bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());

			if (location.getPlot().equals(plot)) {
				powderTask.cancel();
			} else if (!checkLocationWithoutPlayer(powder, location)) {
				powderTask.cancel();
			}
		}
	}

	// removes Powders in a cleared plot
	@EventHandler
	public void onPlotClear(PlotClearEvent event) {
		checkEmptiedPlot(event.getPlot());
	}

	// removes Powders in a deleted/unclaimed plot
	@EventHandler
	public void onPlotDelete(PlotDeleteEvent event) {
		checkEmptiedPlot(event.getPlot());
	}

	// removes Powders if the owner of them is removed from the plot
	@EventHandler
	public void onPlayerPlotTrusted(PlayerPlotTrustedEvent event) {
		if (event.wasAdded()) return;
		UUID uuid = event.getPlayer();
		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getStationaryPowderTasks()) {
			if (powderTask.getTracker().getType() != Tracker.Type.STATIONARY) continue;
			StationaryTracker tracker = (StationaryTracker) powderTask.getTracker();
			if (!tracker.getCreator().equals(uuid)) continue;

			if (!this.hasPermissionForPowder(uuid, event.getPlot())) {
				powderTask.cancel();
			}
		}
	}

	// removes Powders that could be in the road after unlinking two plots
	@EventHandler
	public void onPlotUnlinkEvent(PlotUnlinkEvent event) {
		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getStationaryPowderTasks()) {
			if (powderTask.getTracker().getType() != Tracker.Type.STATIONARY) continue;

			StationaryTracker tracker = (StationaryTracker) powderTask.getTracker();

			org.bukkit.Location bukkitLoc = tracker.getCurrentLocation();
			Location location = new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(),
					bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());

			if (location.isPlotRoad() || location.getPlot() == null) {
				powderTask.cancel();
			}
		}
	}

}
