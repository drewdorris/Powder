package com.ruinscraft.powder.integration;

import java.util.Map.Entry;
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

	public PlotSquaredHandler() {
		this.plotAPI = new PlotAPI();
	}

	public boolean checkLocation(Powder powder, Player player) {
		PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());

		if (!canPlacePowders(plotPlayer)) return false;

		double roadDist = this.getDistanceFromRoad(plotPlayer);
		double powderWidth = powder.maxWidthDistance();
		if (powderWidth > roadDist) return false;

		Plot plot = plotPlayer.getLocation().getPlotAbs();
		// check if player has already placed too many Powders
		
		return true;
	}

	public boolean checkLocation(Powder powder, Location location) {
		double roadDist = this.getDistanceFromRoad(location);
		double powderWidth = powder.maxWidthDistance();
		if (powderWidth > roadDist) return false;
		
		return true;
	}

	public boolean canPlacePowders(PlotPlayer player) {
		return canPlacePowdersInPlot(player, player.getCurrentPlot());
	}

	public boolean canPlacePowdersInPlot(PlotPlayer player, Plot plot) {
		for (UUID uuid : plot.getTrusted()) {
			if (uuid.equals(player.getUUID())) return true;
		}
		for (UUID uuid : plot.getOwners()) {
			if (uuid.equals(player.getUUID())) return true;
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
		return 5;
	}

	public boolean isInPlot(Plot plot, Location... locations) {
		for (Location location : locations) {
			if (location.isPlotRoad() || location.getPlot() != plot) {
				return false;
			}
		}
		return true;
	}

	@EventHandler
	public void onPlotClear(PlotClearEvent event) {
		Plot plot = event.getPlot();
		for (PowderTask powderTask : PowderPlugin.get().getPowderHandler().getCreatedPowderTasks()) {
			for (Entry<Powder, Tracker> entry : powderTask.getPowders().entrySet()) {
				Tracker tracker = entry.getValue();

				org.bukkit.Location bukkitLoc = tracker.getCurrentLocation();
				Location location = new Location(bukkitLoc.getWorld().getName(), bukkitLoc.getBlockX(),
						bukkitLoc.getBlockY(), bukkitLoc.getBlockZ());

				if (location.getPlot().equals(plot)) {
					powderTask.removePowder(entry.getKey());
				} else if (!checkLocation(entry.getKey(), location)) {
					powderTask.removePowder(entry.getKey());
				}
			}
		}
		// go through all placed powders and check if any are in the plot; delete if so
	}

	@EventHandler
	public void onPlotDelete(PlotDeleteEvent event) {
		Plot plot = event.getPlot();
		// go through all placed powders and check if any are in the plot; delete if so
	}

	@EventHandler
	public void onPlayerPlotTrusted(PlayerPlotTrustedEvent event) {
		if (event.wasAdded()) return;
		UUID uuid = event.getPlayer();
		// go through all placed powders and check if any are in the plot and owned by uuid
	}

	@EventHandler
	public void onPlotUnlinkEvent(PlotUnlinkEvent event) {
		// just go through all placed powders and check if theyre in a road
	}

}
